# Plan Service API

A RESTful API for managing structured JSON data with support for CRUD operations, schema validation, conditional reads/writes, Elasticsearch indexing (parent-child model), and secure access via RS256-signed Bearer tokens from Google Identity Provider (IdP).

## Base URL

```
http://localhost:8000/api/v1/plans
```

## Endpoints

### Create a Plan

```
POST /api/v1/plans/create
```

- Creates a new plan document.
- Validates against the JSON Schema.
- Requires Bearer token.

### Get a Plan by ID

```
GET /api/v1/plans/{id}
```

- Retrieves a plan by its `objectId`.
- Supports conditional reads via `If-None-Match`.

### Update a Plan

```
PATCH /api/v1/plans/{id}
```

- Partially updates a plan using merge semantics.
- Validates updated fields against the schema.
- Supports conditional writes using `If-Match`.

### Delete a Plan

```
DELETE /api/v1/plans/{id}
```

- Deletes a plan and all associated child entities.

### Search Plans by Child Field

```
GET http://localhost:8080/api/v1/plans/search?childFieldValue={query}
```

- Searches plans using Elasticsearch parent-child index based on child field values.

## JSON Validation

- Input JSON is validated against a predefined JSON Schema (`plan-schema.json`).
- Validation failures return `400 Bad Request`.

## Security

- All endpoints require an OAuth 2.0 Bearer token.
- Token must be signed using **RS256** by **Google Identity Provider (IdP)**.
- Tokens are verified using Google public keys from:

```
https://www.googleapis.com/oauth2/v3/certs
```

## Storage & Indexing

- **Key/Value Store**: Primary data persistence.
- **Elasticsearch**: Used for search with parent-child relationships.
- **Queueing**: All indexing is processed asynchronously via a queueing system.

## Example Plan JSON

```json
{
  "planCostShares": {
    "deductible": 2000,
    "_org": "example.com",
    "copay": 23,
    "objectId": "1234vxc2324sdf-501",
    "objectType": "membercostshare"
  },
  "linkedPlanServices": [
    {
      "linkedService": {
        "_org": "example.com",
        "objectId": "1234520xvc30asdf-502",
        "objectType": "service",
        "name": "Yearly physical"
      },
      "planserviceCostShares": {
        "deductible": 10,
        "_org": "example.com",
        "copay": 0,
        "objectId": "1234512xvc1314asdfs-503",
        "objectType": "membercostshare"
      },
      "_org": "example.com",
      "objectId": "27283xvx9asdff-504",
      "objectType": "planservice"
    }
  ],
  "_org": "example.com",
  "objectId": "12xvxc345ssdsds-508",
  "objectType": "plan",
  "planType": "inNetwork",
  "creationDate": "12-12-2017"
}
```

## Technologies

- Java-based backend
- Redis store
- Elasticsearch (with parent-child mappings)
- Queue system (RabbitMQ)
- Google OAuth2 configuration


# Install dependencies and run the server
npm install
npm start
```

