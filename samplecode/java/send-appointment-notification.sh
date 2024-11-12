#!/bin/sh

curl -si --location --request POST 'http://localhost:8888/process-event' \
--header 'accept: */*' \
--header 'Content-Type: application/fhir+json' \
--header 'X-Hub-Signature: sha256=a7cd1196b24559701f2fb699a364845c16d3a920455abf047042eb650e2c3563' \
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
            "id": "0901fb5b-6858-33f2-994f-6680c0c6aadb",
            "eventNumber": "1",
            "timestamp": "2023-04-14T09:22:56Z",
            "focus": {
              "identifier": {
                "system": "urn:athenahealth:athenanet:appointment:195900",
                "value": "2"
              }
            }
          }
        ],
        "subscription": {
          "reference": "Subscription/62865e96-3fe4-3f6f-b476-b8eb87b90892"
        },
        "topic": "https://api.platform.athenahealth.com/fhir/r4/SubscriptionTopic/Appointment.schedule"
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
      "fullUrl": "urn:uuid:0901fb5b-6858-33f2-994f-6680c0c6aadb",
      "resource": {
        "resourceType": "AuditEvent",
        "id": "0901fb5b-6858-33f2-994f-6680c0c6aadb",
        "meta": {
          "versionId": "0"
        },
        "extension": [
          {
            "url": "https://fhir.athena.io/StructureDefinition/ah-department",
            "valueReference": {
              "reference": "Organization/a-195900.Department-1"
            }
          },
          {
            "url": "https://hl7.org/fhir/5.0/StructureDefinition/extension-AuditEvent.patient",
            "valueReference": {
              "reference": "Patient/a-195900.E-51711"
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
          "code": "Appointment.schedule"
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
              "reference": "Organization/a-195900.Department-1"
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
                "system": "urn:athenahealth:athenanet:appointment:195900",
                "value": "2"
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