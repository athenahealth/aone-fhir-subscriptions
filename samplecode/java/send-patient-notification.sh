#!/bin/sh

curl -si --location --request POST 'http://localhost:8888/process-event' \
--header 'accept: */*' \
--header 'Content-Type: application/fhir+json' \
--header 'X-Hub-Signature: sha256=3e14365681f764b028eb626e4193c4c19015f063c599f4fe2c35bc7a1c0026bc' \
--data-raw '{
  "resourceType": "Bundle",
  "id": "a0f1df2c-ede9-4528-9ac2-1d1b36df27b2",
  "meta": {
    "profile": [
      "http://hl7.org/fhir/uv/subscriptions-backport/StructureDefinition/backport-subscription-notification"
    ]
  },
  "type": "history",
  "timestamp": "2023-04-14T09:22:57.517+00:00",
  "entry": [
    {
      "fullUrl": "urn:uuid:e80b2e12-d843-4d30-ad6b-67f4bc848efb",
      "resource": {
        "resourceType": "SubscriptionStatus",
        "id": "e80b2e12-d843-4d30-ad6b-67f4bc848efb",
        "status": "active",
        "type": "event-notification",
        "eventsSinceSubscriptionStart": "1",
        "eventsInNotification": 1,
        "notificationEvent": [
          {
            "id": "ead73c9e-ab6e-4db5-9b73-1f73cb243682",
            "eventNumber": "1",
            "timestamp": "2023-04-14T09:22:56Z",
            "focus": {
              "reference": "Patient/a-195900.E-1",
              "identifier": {
                "system": "urn:athenahealth:athenanet:patient:195900",
                "value": "1"
              }
            },
            "additionalContext": [
              {
                "type": "Organization",
                "reference": "Organization/a-195900.Department-1",
                "identifier": {
                  "system": "urn:athenahealth:athenanet:department:195900",
                  "value": "1"
                }
              }
            ]
          }
        ],
        "subscription": {
          "reference": "Subscription/b9f6db63-4883-41bb-9fef-97372cbd3fcd"
        },
        "topic": "https://api.platform.athenahealth.com/fhir/r4/SubscriptionTopic/Patient.update"
      },
      "request": {
        "method": "GET",
        "url": "Subscription/b9f6db63-4883-41bb-9fef-97372cbd3fcd/$status"
      },
      "response": {
        "status": "200"
      }
    }
  ]
} 
'