# athenahealth Event Subscription Platform

*v0.11 - 2024-11-19*

## 1 - Background

The athenahealth Event Subscription Platform makes a broad collection of healthcare domain events available for clients and partners to consume as near real-time notifications.  The platform largely conforms to the [FHIR Subscriptions R5 Backport STU 1.0.0](http://hl7.org/fhir/uv/subscriptions-backport/STU1/StructureDefinition-backport-subscription.html) implementation guide, but with a few differences, primarily around [error handling](#error-handling).  At present the only supported channel type is `rest-hook` and supported payload type is `id-only`.  Resources referenced in the event notifications include both FHIR R4 and proprietary athenahealth endpoints where applicable.  See [payload](#event-payload) below for more details.

_Note: the Subscription and SubscriptionTopic endpoints referenced in this document are only available in a limited alpha rollout at this time._

&nbsp;  

## 2 - Prerequisites

### 2.1 - Set up your Webhook

In order to consume events you will need to set up a webhook endpoint to receive FHIR Subscription notification bundles from the athenahealth Event Subscription platform.  As a best practice, we recommend keeping webhooks as lightweight as possible to ensure that messages can be acknowledged quickly and reliably.  For more on this, see [best practices](#keep-webhook-processing-fast) below.

Your webhook MUST use HTTPS with a valid and unexpired SSL certificate that is issued by a widely trusted certificate authority and it MUST be network-addressable on port 443 from the public internet.  Authentication is handled by [verifying message signatures](#verifying-message-authenticity).

Included in this repo is a sample java webhook application that you can run build and run locally along with scripts to  publish sample event notification Bundles in the same format used by the athenahealth Subscription Platform.  This sample application also includes examples of how to call back to the relevant API endpoints to retrieve resource content based on the provided reference ids.

_Note: this sample application is not intended for direct production use but is provided as an illustrative example to help inform your webhook development efforts._

### 2.2 - Set up your Developer Portal Account

See [athenahealth API Onboarding Overview](https://docs.athenahealth.com/api/guides/onboarding-overview) for details on how to register a new athenahealth Developer Portal account and get an OAuth client ID and secret pair to access athenahealth APIs in general.

See also [Authorization Overview](https://docs.athenahealth.com/api/guides/authorization-overview) for information on how to request an OAuth token with specific scopes.  Currently only 2-Legged OAuth apps are permitted to access the Event Subscription APIs.  Additionally, the Subscription APIs are *not* part of athenahealth's certified US Core FHIR R4 endpoints, therefore these scopes will not be listed in the self-service UI.  You will need to work with the athenahealth API operations team to request the following scopes in order to interact with the Event Subscription Platform:
- `system/SubscriptionTopic.read`
- `system/Subscription.write`
- `system/Subscription.read`

&nbsp;  

## 3 - Subscription Management

### 3.1 - FHIR Base URLs

The Subscription and SubscriptionTopic endpoints are exposed under the athenahealth global FHIR R4 base URLs:
- Preview: https://api.preview.platform.athenahealth.com/fhir/r4
- Production: https://api.platform.athenahealth.com/fhir/r4

### 3.2 - Topic Discovery

To discover the topics available for subscription, you can call the `GET /SubscriptionTopic` search endpoint.  This endpoint requires the `system/SubscriptionTopic.read` scope.  For the current list of topics available for subscription, see the [appendix](#subscription-topics) below.

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

### 3.3 - Creating a Subscription

You will need to create a separate Subscription per topic and per practice.  To subscribe to a topic, you can call the `POST /Subscription` endpoint.  This endpoint requires the `system/Subscription.write` scope.

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
    "_criteria": {
      "extension": [
        {
          "url": "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-filter-criteria",
          "valueString": "ah-practice=Organization/a-1.Practice-195900"
        }
      ]
    },
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

### 3.4 - Deleting a Subscription

To unsubscribe from a topic you will need to call the `DELETE /Subscription/{id}` endpoint.  This endpoint requires the `system/Subscription.write` scope.

Request:
```
curl --header 'Authorization: Bearer <token>' --request DELETE https://api.platform.athenahealth.com/fhir/r4/Subscription/a9c3784c-9f56-4b32-95b0-882868d39e58
```

Response:
```
204 No Content
```

### 3.5 - Listing your Subscriptions

If you do not know your Subscription ID, you can use the `GET /Subscription` search to find it.  This endpoint requires the `system/Subscription.read` scope.

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

Alternatively, you can also find your Subscription ID in any subscription notification Bundle under `entry[0].resource.subscription.reference` (see example [event payload](#event-payload) below).

&nbsp;  

## 4 - Event Notifications

### <a name="event-payload"></a> 4.1 - Event Payload

As noted above, we currently support only the `id-only` payload type.  This means that events will contain a reference to the focus resource related to the event, but you will need to call back to a FHIR R4 or proprietary athenahealth API endpoint if you want to retrieve the latest content for that resource.  While this introduces an extra step, it also reduces the risk of accidental PHI exposure by keeping all access control at the athenahealth API layer.  It also helps avoid some ordering-related gotchas (see also [Event Ordering](#event-ordering) below).

As per the Subscriptions R5 Backport IG, the first entry in each notification Bundle will always be a SubscriptionStatus resource which contains metadata about the Subscription associated with the event as well as metadata about the event(s) contained in the Bundle (see `entry[0].resource.notificationEvent[*].focus` below).  The `entry[0].resource.notificationEvent` array MAY contain multiple entries if the platform has batched multiple events for this SubscriptionTopic into a single notification.  The length of this array will always match the value of `entry[0].resource.eventsInNotification`.

In addition to the SubscriptionStatus resource, the Bundle will also contain one AuditEvent resource corresponding to each entry in the `entry[0].resource.notificationEvent` array.  The event ID provided in `entry[0].resource.notificationEvent[*].id` matches the `AuditEvent.id` of the corresponding AuditEvent contained in the Bundle.  These AuditEvents provide some additional metadata about the event such as the user/agent whose action triggered the event as well as topic-specific extensions (where applicable) with additional context such as the related Patient, Department, etc.  For details on the supported extension types, please see [AuditEvent Extensions](#event-extensions) below.

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
              "id": "cb6cc377-ee38-31ba-9482-f93f821cd169", // Corresponds to AuditEvent.id below
              "timestamp": "2021-03-31T16:20:12.000Z", // Timestamp of the event
              "focus": {
                // FHIR reference (if available)
                "reference": "Patient/a-432.E-528595",

                // Non-FHIR proprietary API reference (if available)
                "identifier": {
                  "system": "urn:athenahealth:athenanet:patient:432",
                  "value": "528595"
                }
              }
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
      },
      {
        "fullUrl": "urn:uuid:cb6cc377-ee38-31ba-9482-f93f821cd169",
        "resource": {
          "resourceType": "AuditEvent",
          "id": "cb6cc377-ee38-31ba-9482-f93f821cd169",
          "meta": {
            "versionId": "0"
          },
          // Extensions give extra metadata about the event.  These will vary by topic.  See more details below.
          "extension": [
            {
              "url": "https://fhir.athena.io/StructureDefinition/ah-department",
              "valueReference": {
                "reference": "Organization/a-432.Department-123"
              }
            }
          ],
          "type": {
            "system": "https://fhir.athena.io/CodeSystem/SubscriptionTopic",
            "code": "Patient.update"
          },
          "recorded": "2021-03-31T16:20:12.000Z", // Timestamp of the event
          "agent": [
            {
              "who": {
                "identifier": {
                  "value": "Athena" // username of the user who triggered the event
                }
              },
              "requestor": true,
              "location": {
                "reference": "Organization/a-432.Department-123"
              }
            }
          ],
          "source": {
            "observer": {
              "reference": "Organization/a-1.Practice-432"
            }
          },
          "entity": [
            {
              "what": {
                // The following information is available in the focus reference as well
                // FHIR reference (if available)
                "reference": "Patient/a-432.E-528595",

                // Non-FHIR proprietary API reference (if available)
                "identifier": {
                  "system": "urn:athenahealth:athenanet:patient:432",
                  "value": "528595"
                }
              }
            }
          ]
        },
        "request": {
          "method": "GET",
          "url": "Subscription/a9c3784c-9f56-4b32-95b0-882868d39e58/$status"
        },
        "response": {
          "status": "200"
        }
      }
    ]
  }'
```

Response expected from your webhook if received successfully:
```
200 OK
```

Your webhook SHOULD return a 2xx response if and only if the event is received successfully.  A timeout or non-2xx response will result in retry attempts as discussed under [error handling](#error-handling) below.  This is how we detect and address failed deliveries.

Note: also discussed under [error handling](#error-handling) below, the `eventsSinceSubscriptionStart`, `eventsInNotification`, and `notificationEvent.eventNumber` fields are handled differently from what is specified in the Subscriptions Backport IG.  In our implementation these fields are populated relative to the current notification Bundle, so if a notification Bundle contains 3 events then these fields would be set to `"3"`, `3`, and `["1","2","3"]` respectively.

### <a name="event-ordering"></a> 4.2 - Event Ordering

Strict event ordering is not practical to achieve with webhooks ([this article](https://www.svix.com/blog/guaranteeing-webhook-ordering/) provides a good overview of the challenges).  For this reason we recommend that you design your webhook not to rely on ordered events.  Note that because our event notifications only contain a reference to the changed resource, you will need to call back to the API to pull the resource content regardless, so you are guaranteed to get the *current* resource state whether you are calling in response to the most recent event or an earlier one.

But what if you need the specific change details to decide how to process the event?  For example, suppose you want to trigger a workflow whenever an Appointment is checked in.  You could listen to all Appointment change events and call back to look for `checked-in` status, but there is a chance that if the status quickly changes again then the Appointment might no longer be in that status by the time you call back to inflate the event.  To address this problem in a different way, we provide targeted subscription topics for important semantic events in the lifecycle of each resource.  For the above use case we would recommend subscribing specifically to the `Appointment.check-in` topic.

We also provide a timestamp with each event that corresponds to the database commit time of that change.  If necessary, this timestamp can be used to reassemble events in time sequence, though please be aware that the athenahealth platform is a large distributed system that persists different types of data across multiple independent data stores so we generally do not recommend trying to order events across resource types due to clock skew.

Our recommended best practice is to avoid relying on ordering insofar as possible.  One common use case is to use event notifications to update a cache of resources.  A good way to do this is to use a durable queue to decouple event delivery from resource inflation and event processing (see also [best practices](#keep-webhook-processing-fast) below).  If you intend to distribute the inflation and processing of messages from the queue across multiple consumer threads, you will likely want to partition the events by resource ID to ensure that multiple events for the same resource are always processed by the same consumer.  Otherwise, this can lead to race conditions if two events occur in quick succession, the same resource may be inflated twice by separate consumer threads and, depending on timing, your cache could end up persisting the older resource state.

### <a name="duplicate-detection"></a> 4.3 - Duplicate Detection

The athenahealth Event Subscription platform is designed to provide *at least once* delivery semantics.  This means that you may, on occasion, receive duplicative notifications for the same event.  A unique ID for each event is provided in the notification Bundle in `entry[0].resource.notificationEvent[*].id` as shown in [Event Payload](#event-payload) above.  Keep in mind that you may also receive distinct events (with distinct IDs) for the same focus resource.  For example, if the same Patient is updated twice in quick succession, you will receive two separate `Patient.update` events that both contain the same Patient ID reference.  These events might arrive in two separate notification Bundles or in a single notification Bundle.

### <a name="event-extensions"></a> 4.4 - AuditEvent Extensions

As discussed in [Event Payload](#event-payload) above, each notification Bundle includes a reference to the updated resource(s) but additionally contains an AuditEvent resource with supplementary metadata about the event.  The specific extensions supported vary by topic, but some common examples are described below.

#### Patient

A reference to the Patient whose data is the subject of the event.

```
{
  "url": "http://hl7.org/fhir/5.0/StructureDefinition/extension-AuditEvent.patient",
  "valueReference": {
    "reference": "Patient/a-{practiceid}.E-{patientid}"
  }
}
```

#### Department

Used to indicate the athenaNet department within which the event occurred or is related.

```
{
  "url": "https://fhir.athena.io/StructureDefinition/ah-department",
  "valueReference": {
    "reference": "Organization/a-{practiceid}.Department-{departmentid}"
  }
}
```

#### Chart Sharing Group

Used to indicate the athenaNet chart sharing group to which the event is related.

```
{
  "url": "https://fhir.athena.io/StructureDefinition/ah-chart-sharing-group",
  "valueReference": {
    "reference": "Organization/a-{practiceid}.CSG-{chartgroupid}"
  }
}
```

&nbsp;  

## 5 - Best Practices

### <a name="error-handling"></a> 5.1 - Error Handling

The FHIR Subscriptions IG's approach to error handling is described at [http://hl7.org/fhir/uv/subscriptions-backport/STU1/errors.html](http://hl7.org/fhir/uv/subscriptions-backport/STU1/errors.html).  Unfortunately, this approach relies on incrementing a monotonic internal event counter without any gaps, which is an expensive and impractical constraint in a distributed system that processes over 100 million events per day.

Instead we follow the message receipt acknowledgement approach recommended by the [WebSub protocol](https://www.w3.org/TR/websub/#content-distribution):  events are deemed to be delivered successfully if and only if the webhook returns a 2xx response code.  Failures (i.e., null or non-2xx responses) are retried for up to 1 hour.  Messages that fail repeatedly are retained in a dead letter queue where they can be replayed for up to 7 days if necessary.

### <a name="keep-webhook-processing-fast"></a> 5.2 - Keep Webhook Processing Fast

The athenahealth Event Subscription Platform expects your webhook to return a 2xx response code within a *timeout limit of 2 seconds*.  This is a hard limit and cannot be increased.  To ensure that your webhook responds quickly as well as to avoid duplicative processing in case of partial failures, we *strongly recommend* that you utilize a durable queuing mechanism to safely decouple event *delivery* from event *processing*.  For example, one good pattern is a webhook that persists events into a durable message queue where they can be consumed, inflated, and processed by a separate application.  This decoupling helps provide resilience to intermittent traffic spikes:  events can quickly be queued and acknowledged by the webhook even if the downstream processing of those events may require additional time.

Many robust message queue implementations exist, including but not limited to:  Apache Kafka, Amazon SQS, Google Cloud Pub/Sub, RabbitMQ, etc.

### <a name="verifying-message-authenticity"></a> 5.3 - Verifying Message Authenticity

As noted above, the `X-Hub-Signature` header can be used to verify that a message received by your webhook originated from athenahealth.  For details, please see [https://www.w3.org/TR/websub/#signature-validation](https://www.w3.org/TR/websub/#signature-validation).  At present, events will be signed using the `sha256` method, though this may change in future so you should check the `method` provided in the header.

&nbsp;

## 6 - Appendix

### <a name="subscription-topics"></a> 6.1 - Subscription Topics

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
- [GET /v1/{practiceid}/appointments/{appointmentid}](https://docs.athenahealth.com/api/api-ref/appointment#Get-appointment-details)

#### Claim

Topics:
- Claim.create
- Claim.delete
- Claim.update

API endpoint(s) to retrieve resource content:
- [GET /v1/{practiceid}/claims/{claimid}](https://docs.athenahealth.com/api/api-ref/claim#Get-individual-claim-details)

#### ClinicalEncounterDiagnosis

Topics:
- ClinicalEncounterDiagnosis.create
- ClinicalEncounterDiagnosis.delete
- ClinicalEncounterDiagnosis.update

API endpoint(s) to retrieve resource content:
- [GET /fhir/r4/Condition/{logicalId}](https://docs.athenahealth.com/api/fhir-r4/condition#READ_6)

#### Encounter

Topics:
- Encounter.check-in
- Encounter.reopen
- Encounter.signoff

API endpoint(s) to retrieve resource content:
- [GET /fhir/r4/Encounter/{logicalId}](https://docs.athenahealth.com/api/fhir-r4/encounter#READ_6)
- [GET /v1/{practiceid}/chart/encounter/{encounterid}](https://docs.athenahealth.com/api/api-ref/encounter-chart#Get-encounter-information)

#### HistoricalMedication

Topics:
- HistoricalMedication.create
- HistoricalMedication.delete
- HistoricalMedication.update

API endpoint(s) to retrieve resource content:
- [GET /fhir/r4/MedicationRequest/{logicalId}](https://docs.athenahealth.com/api/fhir-r4/medication-request#READ_6)
- [GET /v1/{practiceid}/chart/{patientid}/medications](https://docs.athenahealth.com/api/api-ref/medication#Get-patient's-medication-list)

#### HistoricalVaccine

Topics:
- HistoricalVaccine.create
- HistoricalVaccine.delete
- HistoricalVaccine.update

API endpoint(s) to retrieve resource content:
- [GET /fhir/r4/Immunization/{logicalId}](https://docs.athenahealth.com/api/fhir-r4/immunization#READ_6)
- [GET /v1/{practiceid}/chart/{patientid}/vaccines](https://docs.athenahealth.com/api/api-ref/vaccines#Get-list-of-patient's-vaccines)

#### ImagingResult

Topics:
- ImagingResult.close
- ImagingResult.create
- ImagingResult.delete
- ImagingResult.reopen
- ImagingResult.update

API endpoint(s) to retrieve resource content:
- [GET /v1/{practiceid}/patients/{patientid}/documents/imagingresult/{imagingresultid}](https://docs.athenahealth.com/api/api-ref/document-type-imaging-result#Get-patient's-imaging-result-document)

#### LabResult

Topics:
- LabResult.close
- LabResult.create
- LabResult.update

API endpoint(s) to retrieve resource content:
- [GET /v1/{practiceid}/patients/{patientid}/documents/labresult/{labresultid}](https://docs.athenahealth.com/api/api-ref/document-type-lab-result#Get-patient's-lab-result-document)

#### Order

Topics:
- Order.cancel
- Order.deny
- Order.perform
- Order.signoff
- Order.submit
- Order.update

API endpoint(s) to retrieve resource content:
- [GET /v1/{practiceid}/patients/{patientid}/documents/order/{documentid}](https://docs.athenahealth.com/api/api-ref/document-type-order#Get-patient's-order-document)

#### Patient

Topics:
- Patient.create
- Patient.delete
- Patient.merge
- Patient.update

API endpoint(s) to retrieve resource content:
- [GET /fhir/r4/Patient/{logicalId}](https://docs.athenahealth.com/api/fhir-r4/patient#READ_4)
- [GET /v1/{practiceid}/patients/{patientid}](https://docs.athenahealth.com/api/api-ref/patient#Get-specific-patient-record)

#### PatientCase

Topics:
- PatientCase.add-note
- PatientCase.create
- PatientCase.update

API endpoint(s) to retrieve resource content:
- [GET /v1/{practiceid}/patients/{patientid}/documents/patientcase/{documentid}](https://docs.athenahealth.com/api/api-ref/document-type-patient-case#Get-patient-case-document-for-a-patient)

#### PatientProblem

Topics:
- PatientProblem.create
- PatientProblem.delete
- PatientProblem.update

API endpoint(s) to retrieve resource content:
- [GET /fhir/r4/Condition/{logicalId}](https://docs.athenahealth.com/api/fhir-r4/condition#READ_6)
- [GET /v1/{practiceid}/chart/{patientid}/problems](https://docs.athenahealth.com/api/api-ref/problems#Get-patient's-problem-list)

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
- [GET /fhir/r4/MedicationRequest/{logicalId}](https://docs.athenahealth.com/api/fhir-r4/medication-request#READ_6)
- [GET /v1/{practiceid}/patients/{patientid}/documents/prescription/{documentid}](https://docs.athenahealth.com/api/api-ref/document-type-prescription#Get-specific-prescription-document-for-given-patient)

#### Provider

Topics:
- Provider.create
- Provider.delete
- Provider.undelete
- Provider.update

API endpoint(s) to retrieve resource content:
- [GET /fhir/r4/Practitioner/{logicalId}](https://docs.athenahealth.com/api/fhir-r4/practitioner#READ_6)
- [GET /v1/{practiceid}/providers/{providerid}](https://docs.athenahealth.com/api/api-ref/provider#Get-information-of-given-provider)

#### ReferringProvider

Topics:
- ReferringProvider.create
- ReferringProvider.delete
- ReferringProvider.undelete
- ReferringProvider.update

API endpoint(s) to retrieve resource content:
- [GET /v1/{practiceid}/referringproviders/{referringproviderid}](https://docs.athenahealth.com/api/api-ref/referring-provider#Get-information-of-given-referring-provider)
