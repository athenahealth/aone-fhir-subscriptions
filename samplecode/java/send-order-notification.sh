#!/bin/sh

curl -si --location --request POST 'http://localhost:8888/process-event' \
--header 'accept: */*' \
--header 'Content-Type: application/fhir+json' \
--header 'X-Hub-Signature: x/dtB8ZGsJT7Vlk7JvO6uRIg7IEns+R6rfC/MGkHZCM=' \
--data-raw '{
  "resourceType": "Bundle",
  "id": "05f90103-5ce1-327b-b8fe-287225e176a8",
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
            "id": "b594cd05-74e6-4b5e-9205-c618667ebf75",
            "eventNumber": "1",
            "timestamp": "2023-04-14T09:22:56Z",
            "focus": {
              "identifier": {
                "system": "urn:athenahealth:athenanet:order:195900",
                "value": "157479"
              }
            },
            "additionalContext": [
              {
                "type": "Organization",
                "reference": "Organization/a-195900.Department-150",
                "identifier": {
                  "system": "urn:athenahealth:athenanet:department:195900",
                  "value": "150"
                }
              },
              {
                "type": "Patient",
                "reference": "Patient/a-195900.E-8269",
                "identifier": {
                  "system": "urn:athenahealth:athenanet:patient:195900",
                  "value": "8269"
                }
              }
            ]
          }
        ],
        "subscription": {
          "reference": "Subscription/62865e96-3fe4-3f6f-b476-b8eb87b90892"
        },
        "topic": "https://api.fhir.athena.io/fhir/r4/SubscriptionTopic/Order.signoff"
      },
      "request": {
        "method": "GET",
        "url": "Subscription/62865e96-3fe4-3f6f-b476-b8eb87b90892"
      },
      "response": {
        "status": "200"
      }
    },
    {
      "id": "b594cd05-74e6-4b5e-9205-c618667ebf75",
      "response": {
        "status": "200",
        "lastModified": "2023-04-14T09:22:56Z"
      }
    }
  ]
} 
'