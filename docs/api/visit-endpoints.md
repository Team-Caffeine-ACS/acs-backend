# Visit Activity Management — API Endpoints

## Base path: `/api/visits`

All endpoints require a valid Bearer token (`Authorization: Bearer <token>`).

---

## Endpoints

### `GET /api/visits`

#### List Visits (Paginated)

Query parameters: `search`, `status` (`planned` | `in_building` | `departed` | `expired`), `dateFrom`, `dateTo`, `page`, `size`

| Role | Access |
| --- | --- |
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `GET /api/visits/{visitId}`

#### Get Visit Details

Returns visitor name, personal ID code, organization, department, host name, comment, and assigned card ID.

| Role | Access |
| --- | --- |
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `POST /api/visits`

#### Record a New Visit

Body: `personId`, `accessPointId`, `keycardId` (optional), `hostPersonInRoleId` (optional), `comment`, `arrivalTime`. The authenticated user is used as the assignor.

Returns full visitor details plus keycard assignment info.

| Role | Access |
| --- | --- |
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `PUT /api/visits/{visitId}/exit`

#### Record Visitor Departure

Body: `exitTime`. Fails with 409 if the visit already has an exit time.

| Role | Access |
| --- | --- |
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `PUT /api/visits/{visitId}/edit`

#### Edit Visit Details

Body: `hostId`, `assignorId`, `accessPointId`, `entryTime`, `comment`.

Behavior:

- `hostId = null` clears the host
- `hostId` must reference an existing person who has an active `PersonInRole`
- `assignorId` must reference an existing active `PersonInRole`
- `accessPointId` must reference an existing access point
- `entryTime` is required
- `comment` may be `null` or empty, but must not exceed 1024 characters
- returns `404` when the visit, host, assignor, or access point does not exist
- returns `409` when `entryTime` is later than an already recorded `exitTime`
- updates and persists host, assignor, access point, entry time, and comment

Restricted to privileged roles only.

| Role | Access |
| --- | --- |
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | denied (403) |
| VISITOR | denied (403) |

---

### `GET /api/visits/{visitId}/timeline`

#### Get Visit Audit Timeline

Returns an ordered list of persisted visit events. The current implementation exposes
`ARRIVAL_REGISTERED` and `DEPARTURE_REGISTERED` based on stored visit timestamps.
Editing a visit does not currently create a separate persisted timeline event.

| Role | Access |
| --- | --- |
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

## Role Summary

| Endpoint | ADMIN | SECURITY_CHIEF | RECEPTIONIST | VISITOR |
| --- | :---: | :---: | :---: | :---: |
| `GET /api/visits` | yes | yes | yes | no |
| `GET /api/visits/{visitId}` | yes | yes | yes | no |
| `POST /api/visits` | yes | yes | yes | no |
| `PUT /api/visits/{visitId}/exit` | yes | yes | yes | no |
| `PUT /api/visits/{visitId}/edit` | yes | yes | **no** | no |
| `GET /api/visits/{visitId}/timeline` | yes | yes | yes | no |
