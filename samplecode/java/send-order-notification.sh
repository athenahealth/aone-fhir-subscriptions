#!/bin/sh

curl -si --location --request POST 'http://localhost:8888/process-event' \
--header 'accept: */*' \
--header 'Content-Type: application/fhir+json' \
--header 'X-Hub-Signature: sha256=f528cc71a3e0032482465fc4b55cda0f63d230e2072cbfdceb8c2feaf6a91838' \
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
            }
          }
        ],
        "subscription": {
          "reference": "Subscription/62865e96-3fe4-3f6f-b476-b8eb87b90892"
        },
        "topic": "https://api.platform.athenahealth.com/fhir/r4/SubscriptionTopic/Order.signoff"
      },
      "request": {
        "method": "GET",
        "url": "Subscription/62865e96-3fe4-3f6f-b476-b8eb87b90892/$status"
      },
      "response": {
        "status": "200"
      }
    },
    {
      "fullUrl": "urn:uuid:b594cd05-74e6-4b5e-9205-c618667ebf75",
      "resource": {
        "resourceType": "AuditEvent",
        "id": "b594cd05-74e6-4b5e-9205-c618667ebf75",
        "meta": {
          "versionId": "0"
        },
        "extension": [
          {
            "url": "https://fhir.athena.io/StructureDefinition/ah-department",
            "valueReference": {
              "reference": "Organization/a-195900.Department-150"
            }
          },
          {
            "url": "https://hl7.org/fhir/5.0/StructureDefinition/extension-AuditEvent.patient",
            "valueReference": {
              "reference": "Patient/a-195900.E-8269"
            }
          },
          {
            "url": "https://fhir.athena.io/StructureDefinition/ah-chart-sharing-group",
            "valueReference": {
              "reference": "Organization/a-195900.CSG-42"
            }
          }
        ],
        "type": {
          "system": "https://fhir.athena.io/CodeSystem/SubscriptionTopic",
          "code": "Order.signoff"
        },
        "recorded": "2023-04-14T09:22:56Z",
        "agent": [
          {
            "who": {
              "identifier": {
                "value": "Athena"
              }
            },
            "requestor": true,
            "location": {
              "reference": "Organization/a-195900.Department-150"
            }
          }
        ],
        "source": {
          "observer": {
            "reference": "Organization/a-1.Practice-195900"
          }
        },
        "entity": [
          {
            "what": {
              "identifier": {
                "system": "urn:athenahealth:athenanet:order:195900",
                "value": "157479"
              }
            }
          }
        ]
      },
      "request": {
        "method": "GET",
        "url": "Subscription/62865e96-3fe4-3f6f-b476-b8eb87b90892/$status"
      },
      "response": {
        "status": "200"
      }
    }
  ]
} 
'