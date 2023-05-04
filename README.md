# athenahealth Event Subscription Platform
*v0.5 - 2023-05-03*

## Background

The athenahealth Event Subscription Platform makes a broad collection of healthcare domain events available for clients and partners to consume as near real-time notifications.  The platform largely conforms to the [FHIR Subscriptions R5 Backport STU 1.0.0](http://hl7.org/fhir/uv/subscriptions-backport/STU1/StructureDefinition-backport-subscription.html) implementation guide, but with a few differences, primarily around [error handling](#error-handling).  At present the only supported channel type is `rest-hook` and supported payload type is `id-only`.  Resources referenced in the event notifications include both FHIR R4 and proprietary athenahealth endpoints where applicable.  See [payload](#event-payload) below for more details.

_Note: the Subscription and SubscriptionTopic endpoints referenced in this document are not yet available externally but are scheduled for alpha release in June 2023._

&nbsp;  

## Prerequisites

### Set up your Webhook

In order to consume events you will need to set up a webhook endpoint to receive FHIR Subscription notification bundles from the athenahealth Event Subscription platform.  As a best practice, we recommend keeping webhooks as lightweight as possible to ensure that messages can be acknowledged quickly and reliably.  For more on this, see [best practices](#keep-webhook-processing-fast) below.

Your webhook MUST use HTTPS with a valid and unexpired SSL certificate that is issued by a widely trusted certificate authority and it MUST be network-addressable from the public internet.  Authentication is handled by [verifying message signatures](#verifying-message-authenticity).

Included in this repo is a sample java webhook application that you can run build and run locally along with scripts to  publish sample event notification Bundles in the same format used by the athenahealth Subscription Platform.  This sample application also includes examples of how to call back to the relevant API endpoints to retrieve resource content based on the provided reference ids.

_Note: this sample application is not intended for direct production use but is provided as an illustrative example to help inform your webhook development efforts._

### Set up your Developer Portal Account

See [athenahealth API Onboarding Overview](https://docs.athenahealth.com/api/guides/onboarding-overview) for details on how to register a new athenahealth Developer Portal account and get an OAuth client ID and secret pair to access athenahealth APIs in general.

See also [Authorization Overview](https://docs.athenahealth.com/api/guides/authorization-overview) for information on how to request an OAuth token with specific scopes.  Note that only 2-Legged OAuth apps are permitted to access the Event Subscription APIs at this time.

&nbsp;  

## Subscription Management

### Topic Discovery

To discover the topics available for subscription, you can call the SubscriptionTopic search endpoint.  This endpoint requires the `system/SubscriptionTopic.read` scope.  For preliminary list of topics available for subscription, see [appendix](#subscription-topics) below.

Request:
```
curl --header 'Authorization: Bearer <token>' --request GET https://api.platform.athenahealth.com/fhir/r4/SubscriptionTopic
```

Response:
```
200 OK
{
    "resourceType": "Bundle",
    "id": "b65d8a46-4ecb-4c61-9c93-b74ea87d9af7",
    "type": "searchset",
    "entry": [
        {
            "fullUrl": "https://api.platform.athenahealth.com/fhir/r4/SubscriptionTopic/Patient.create",
            "resource": {
                "resourceType": "SubscriptionTopic",
                "id": "Patient.create",
                "description": "A new Patient record is created.",
                "resourceDescription": "Demographics and other administrative information about an individual receiving care or other health-related services.",
                "status": "active",
                "experimental": false
            }
        },
        {
            "fullUrl": "https://api.platform.athenahealth.com/fhir/r4/SubscriptionTopic/Appointment.check-in",
            "resource": {
                "resourceType": "SubscriptionTopic",
                "id": "Appointment.check-in",
                "description": "An existing appointment is checked in.",
                "resourceDescription": "Appointment resources are used to provide information about a planned meeting that may be in the future or past. The resource only describes a single meeting, a series of repeating visits would require multiple appointment resources to be created for each instance.",
                "status": "active",
                "experimental": false
            }
        },
        ...
    ]
}
```

### Creating a Subscription

You will need to create a separate Subscription per topic.  This endpoint requires the `system/Subscription.write` scope.

Request:
```
curl --request POST https://api.platform.athenahealth.com/fhir/r4/Subscription \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --header 'X-Hub-Secret: <random-shared-secret>' \
  --data-raw '{
    "resourceType": "Subscription",
    "meta": {
        "profile": [
            "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscription"
        ]
    },
    "status": "requested",
    "end": "2022-12-31T12:00:00Z",
    "reason": "For testing",
    "criteria": "https://api.platform.athenahealth.com/fhir/r4/SubscriptionTopic/Patient.update",
    "channel": {
        "type": "rest-hook",
        "endpoint": "https://example.org/your-webhook",
        "payload": "application/fhir+json",
        "_payload": {
            "extension": [
                {
                    "url": "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-payload-content",
                    "valueCode": "id-only"
                }
            ]
        }
    }
}'
```

Response:
```
201 Created
{
  "resourceType": "Subscription",
  "id": "a9c3784c-9f56-4b32-95b0-882868d39e58",
  ...
}
```

The `X-Hub-Secret` header is optional but _strongly recommended_ to allow your webhook to verify authenticity of the notification messages received and ensure that the payload originated from athenahealth.  If provided, this secret will be used to generate an HMAC signature for each outbound notification as described at [https://www.w3.org/TR/websub/#signing-content](https://www.w3.org/TR/websub/#signing-content).

### Deleting a Subscription

To unsubscribe from a topic you will need to call the `DELETE /Subscription/{id}` endpoint with the `system/Subscription.write` scope.

Request:
```
curl --header 'Authorization: Bearer <token>' --request DELETE https://api.platform.athenahealth.com/fhir/r4/Subscription/a9c3784c-9f56-4b32-95b0-882868d39e58
```

Response:
```
204 No Content
```

If you do not know your Subscription ID, you can find it in any subscription notification Bundle under `entry[0].resource.subscription.reference` or you can use the Subscription search to find it:

Request:
```
curl --header 'Authorization: Bearer <token>' --request GET https://api.platform.athenahealth.com/fhir/r4/Subscription
```

Response:
```
200 OK
{
    "resourceType": "Bundle",
    "id": "64dfa330-fd7b-4f88-a5e7-d00f5b353e18",
    "type": "searchset",
    "total": 10,
    "entry": [
        {
            "fullUrl": "https://api.platform.athenahealth.com/fhir/r4/Subscription/a9c3784c-9f56-4b32-95b0-882868d39e58",
            "resource": {
                "resourceType": "Subscription",
                "id": "a9c3784c-9f56-4b32-95b0-882868d39e58",
                ...
            }
        },
        ...
    ]
}
```

&nbsp;  

## Event Notifications

### <a name="event-payload"></a> Event Payload

As noted above, we currently support only the `id-only` payload type.  This means that events will contain a reference to the focus resource related to the event, but you will need to call back to a FHIR R4 or proprietary athenahealth API endpoint if you want to retrieve the latest content for that resource.  While this introduces an extra step, it also reduces the risk of accidental PHI exposure by keeping all access control at the athenahealth API layer.  It also helps avoid some ordering-related gotchas (see also [Event Ordering](#event-ordering) below).

In addition to the focus resource ID, some events MAY also contain additional context such as the related Patient ID and/or Department ID (see example below).

As per the Subscriptions R5 Backport IG, the first entry in each notification Bundle will always be a SubscriptionStatus resource which contains metadata about the Subscription associated with the event as well as metadata about the event(s) contained in the Bundle (see `SubscriptionStatus.notificationEvent.focus` below).  At present this will be the only entry in the Bundle since `full-resource` payload is not supported at this time.

Example request that would be sent to your webhook for a `Patient.update` event notification:
```
curl --request POST https://example.org/your-webhook \
  --header 'X-Hub-Signature: sha256=ca876e76c...' \
  --data-raw '{
    "resourceType": "Bundle",
    "id": "3945182f-d315-4dbf-9259-09d863c7e7da",
    "type": "history",
    "meta": {
      "profile": ["http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscription-notification"]
    },
    "timestamp": "2021-03-31T16:20:01.123Z", // Note: this is notification timestamp - see below for event timestamp
    "entry": [
      {
        "fullUrl": "urn:uuid:c144782b-da2f-4125-a9e2-9fa4b9085a40",
        "resource": {
          "resourceType": "SubscriptionStatus",
          "id": "c144782b-da2f-4125-a9e2-9fa4b9085a40",
          "status": "active",
          "type": "event-notification",
          "eventsSinceSubscriptionStart": "1",
          "eventsInNotification": 1,
          "subscription": {
            "reference": "Subscription/a9c3784c-9f56-4b32-95b0-882868d39e58"
          },
          "topic": "https://api.platform.athenahealth.com/fhir/r4/SubscriptionTopic/Patient.update",
          "notificationEvent": [
            {
              "eventNumber": "1",
              "id": "cb6cc377-ee38-31ba-9482-f93f821cd169", // Unique UUID for the event
              "timestamp": "2021-03-31T16:20:12.000Z", // Timestamp of the event
              "focus": {
                // FHIR reference (if available)
                "reference": "Patient/a-432.E-528595",

                // Non-FHIR proprietary API reference (if available)
                "identifier": {
                  "system": "urn:athenahealth:athenanet:patient:432",
                  "value": "528595"
                }
              },
              "additionalContext": [
                {
                  "reference": "Organization/a-432.Department-123",
                  "identifier": {
                    "system": "urn:athenahealth:athenanet:department:432",
                    "value": "123"
                  }
                }
              ]
            }
          ]
        },
        "request": {
          "method": "GET",
          "url": "Subscription/a9c3784c-9f56-4b32-95b0-882868d39e58/$status"
        },
        "response": {
          "status" : "200"
        }
      }
    ]
  }'
```

Response expected from your webhook if received successfully:
```
200 OK
```

It is important that your webhook returns a 2xx response if and only if the event is received successfully.  A timeout or non-2xx response will result in retry attempts as discussed under [error handling](#error-handling) below.  This is how we detect and address failed deliveries.

Note: also discussed under [error handling](#error-handling) below, the `eventsSinceSubscriptionStart`, `eventsInNotification`, and `notificationEvent.eventNumber` fields are handled differently from what is specified in the Subscriptions Backport IG.  In our implementation these fields are populated relative to the current notification Bundle, so if a notification Bundle contains 3 events then these fields would be set to `"3"`, `3`, and `["1","2","3"]` respectively.

### <a name="event-ordering"></a> Event Ordering

Strict event ordering is not practical to achieve with webhooks ([this article](https://www.svix.com/blog/guaranteeing-webhook-ordering/) provides a good overview of the challenges).  For this reason we recommend that you design your webhook not to rely on ordered events.  Note that because our event notifications only contain a reference to the changed resource, you will need to call back to the API to pull the resource content regardless, so you are guaranteed to get the *current* resource state whether you are calling in response to the most recent event or an earlier one.

But what if you need the specific change details to decide how to process the event?  For example, suppose you want to trigger a workflow whenever an Appointment is checked in.  You could listen to all Appointment change events and call back to look for `checked-in` status, but there is a chance that if the status quickly changes again then the Appointment might no longer be in that status by the time you call back to inflate the event.  To address this problem in a different way, we provide targeted subscription topics for important semantic events in the lifecycle of each resource.  For example, for the above use case we would recommend subscribing specifically to the `Appointment.check-in` topic.

We also provide a timestamp with each event that corresponds to the database commit time of that change.  If necessary, this timestamp can be used to reassemble events in time sequence, though please be aware that the athenahealth platform is a large distributed system that persists different types of data across multiple independent data stores so we generally do not recommend trying to order events across resource types due to clock skew, etc.

Again, our recommended best practice is to avoid relying on ordering insofar as possible.  One common use case is to use event notifications to update a cache of resources.  A good way to do this is to use a durable queue to decouple event delivery from resource inflation and event processing (see also [best practices](#keep-webhook-processing-fast) below).  If you intend to distribute the inflation and processing of messages from the queue across multiple consumer threads, you will likely want to partition the events by resource ID to ensure that multiple events for the same resource are always processed by the same consumer.  Otherwise this can lead to race conditions if two events occur in quick succession, the same resource may be inflated twice by separate consumer threads and, depending on timing, your cache could end up persisting the older resource state.

### <a name="duplicate-detection"></a> Duplicate Detection

The athenahealth Event Subscription platform is designed to provide *at least once* delivery semantics.  This means that you may, on occasion, receive duplicative notifications for the same event.  A unique ID for each event is provided in the notification Bundle as shown in [Event Payload](#event-payload) above.  Keep in mind that you may also receive distinct events (with distinct IDs) for the same focus resource.  For example, if the same Patient is updated twice in quick succession, you will receive two separate `Patient.update` events that both contain the same Patient ID reference.  These events might arrive in two separate notification Bundles or in a single notification Bundle.

&nbsp;  

## Best Practices

### <a name="error-handling"></a> Error Handling

The FHIR Subscriptions IG's approach to error handling is described at [http://hl7.org/fhir/uv/subscriptions-backport/STU1/errors.html](http://hl7.org/fhir/uv/subscriptions-backport/STU1/errors.html).  Unfortunately this approach relies on incrementing a monotonic internal event counter without any gaps, which is an expensive and impractical constraint in a distributed system that processes over 100 million events per day.

Instead we follow the message receipt acknowledgement approach recommended by the [WebSub protocol](https://www.w3.org/TR/websub/#content-distribution):  events are deemed to be delivered successfully if and only if the webhook returns a 2xx response code.  Failures (i.e. null or non-2xx responses) are retried for up to 1 hour.  Messages that fail repeatedly are retained in a dead letter queue where they can be replayed for up to 7 days if necessary.

### <a name="keep-webhook-processing-fast"></a> Keep Webhook Processing Fast

The athenahealth Event Subscription Platform expects your webhook to return a 2xx response code within a *timeout limit of 5 seconds*.  This is a hard limit and cannot be increased.  To ensure that your webhook responds quickly as well as to avoid duplicative processing in case of partial failures, we *strongly recommend* that you utilize a durable queueing mechanism to safely decouple event *delivery* from event *processing*.  For example, one good pattern is a webhook that persists events into a Kafka queue (or similar) where they can be consumed, inflated, and processed by a separate application.  This decoupling helps provide resilience to intermittent traffic spikes:  events can quickly be queued and acknowledged by the webhook even if the downstream processing of those events may require additional time.

Note: Kafka is only an example here.  Many good alternatives exist depending on your architectural preferences:  Amazon SQS, Google Cloud Pub/Sub, RabbitMQ, etc.

### <a name="verifying-message-authenticity"></a> Verifying Message Authenticity

As noted above, the `X-Hub-Signature` header can be used to verify that a message received by your webhook originated from athenahealth.  For details, please see [https://www.w3.org/TR/websub/#signature-validation](https://www.w3.org/TR/websub/#signature-validation).  At present events will be signed using the `sha256` method, though this might change in future so you should check the `method` provided in the header.

&nbsp;

## Appendix

### <a name="subscription-topics"></a> Subscription Topics

Below is the list of event topics available for subscription in the alpha phase.  Also provided is a reference to the FHIR R4 and/or athenahealth proprietary API endpoints that can be used to retrieve the current state of the focus resource referenced in the event.  Note that some resources are available in FHIR R4 format while others are not, so the `focusResource` reference within the event notification will include one or both of the following:
- a relative literal reference if resource is available as a FHIR R4 endpoint
- a logical reference (identifier) if resource is available in athenahealth proprietary format

#### Appointment

Topics:
- Appointment.cancel
- Appointment.check-in
- Appointment.check-out
- Appointment.freeze
- Appointment.reschedule
- Appointment.schedule
- Appointment.unfreeze
- Appointment.update

API endpoint(s) to retrieve resource content:
- GET /v1/{practiceid}/appointments/{appointmentid}

#### Claim

Topics:
- Claim.create
- Claim.delete
- Claim.update

API endpoint(s) to retrieve resource content:
- GET /v1/{practiceid}/claims/{claimid}

#### ClinicalEncounterDiagnosis

Topics:
- ClinicalEncounterDiagnosis.create
- ClinicalEncounterDiagnosis.delete
- ClinicalEncounterDiagnosis.update

API endpoint(s) to retrieve resource content:
- GET /fhir/r4/Condition/{logicalId}

#### Encounter

Topics:
- Encounter.check-in
- Encounter.reopen
- Encounter.signoff

API endpoint(s) to retrieve resource content:
- GET /fhir/r4/Encounter/{logicalId}
- GET /v1/{practiceid}/chart/encounter/{encounterid}

#### HistoricalMedication

Topics:
- HistoricalMedication.create
- HistoricalMedication.delete
- HistoricalMedication.update

API endpoint(s) to retrieve resource content:
- GET /fhir/r4/MedicationRequest/{logicalId}
- GET /v1/{practiceid}/chart/{patientid}/medications

#### HistoricalVaccine

Topics:
- HistoricalVaccine.create
- HistoricalVaccine.delete
- HistoricalVaccine.update

API endpoint(s) to retrieve resource content:
- GET /fhir/r4/Immunization/{logicalId}
- GET /v1/{practiceid}/chart/{patientid}/vaccines

#### ImagingResult

Topics:
- ImagingResult.close
- ImagingResult.create
- ImagingResult.delete
- ImagingResult.reopen
- ImagingResult.update

API endpoint(s) to retrieve resource content:
- GET /v1/{practiceid}/patients/{patientid}/documents/imagingresult/{imagingresultid}

#### LabResult

Topics:
- LabResult.close
- LabResult.create
- LabResult.update

API endpoint(s) to retrieve resource content:
- GET /v1/{practiceid}/patients/{patientid}/documents/labresult/{labresultid}

#### Order

Topics:
- Order.cancel
- Order.deny
- Order.perform
- Order.signoff
- Order.submit
- Order.update

API endpoint(s) to retrieve resource content:
- GET /v1/{practiceid}/patients/{patientid}/documents/order/{documentid}

#### Patient

Topics:
- Patient.create
- Patient.delete
- Patient.merge
- Patient.update

API endpoint(s) to retrieve resource content:
- GET /fhir/r4/Patient/{logicalId}
- GET /v1/{practiceid}/patients/{patientid}

#### PatientCase

Topics:
- PatientCase.add-note
- PatientCase.create
- PatientCase.update

API endpoint(s) to retrieve resource content:
- GET /v1/{practiceid}/patients/{patientid}/documents/patientcase/{documentid}

#### PatientProblem

Topics:
- PatientProblem.create
- PatientProblem.delete
- PatientProblem.update

API endpoint(s) to retrieve resource content:
- GET /fhir/r4/Condition/{logicalId}
- GET /v1/{practiceid}/chart/{patientid}/problems

#### Prescription

Topics:
- Prescription.create
- Prescription.delete
- Prescription.preapprove
- Prescription.refill-create
- Prescription.refill-update
- Prescription.reopen
- Prescription.update

API endpoint(s) to retrieve resource content:
- GET /fhir/r4/MedicationRequest/{logicalId}
- GET /v1/{practiceid}/patients/{patientid}/documents/prescription/{documentid}

#### Provider

Topics:
- Provider.create
- Provider.delete
- Provider.undelete
- Provider.update

API endpoint(s) to retrieve resource content:
- GET /fhir/r4/Practitioner/{logicalId}
- GET /v1/{practiceid}/providers/{providerid}

#### ReferringProvider

Topics:
- ReferringProvider.create
- ReferringProvider.delete
- ReferringProvider.undelete
- ReferringProvider.update

API endpoint(s) to retrieve resource content:
- GET /v1/{practiceid}/referringproviders/{referringproviderid}
