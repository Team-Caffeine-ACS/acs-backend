# Visit Activity Management — API Endpoints

## Base path: `/api/visits`

All endpoints require a valid Bearer token (`Authorization: Bearer <token>`).

---

## Endpoints

### `GET /api/visits`
**List visits (paginated)**

Query parameters: `search`, `status` (`planned` | `in_building` | `departed` | `expired`), `dateFrom`, `dateTo`, `page`, `size`

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `GET /api/visits/{visitId}`
**Get visit details**

Returns visitor name, personal ID code, organization, department, host name, comment, and assigned card ID.

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `POST /api/visits`
**Record a new visit**

Body: `personId`, `accessPointId`, `keycardId` (optional), `hostPersonInRoleId` (optional), `comment`, `arrivalTime`. The authenticated user is used as the assignor.

Returns full visitor details plus keycard assignment info.

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `PUT /api/visits/{visitId}/exit`
**Record visitor departure**

Body: `exitTime`. Fails with 409 if the visit already has an exit time.

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `PUT /api/visits/{visitId}/edit`
**Edit visit details**

Body: `hostId`, `assignorId`, `accessPointId`, `entryTime`, `comment`. Restricted to privileged roles only.

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | denied (403) |
| VISITOR | denied (403) |

---

### `GET /api/visits/{visitId}/timeline`
**Get visit audit timeline**

Returns an ordered list of events (`ARRIVAL_REGISTERED`, `DEPARTURE_REGISTERED`) with timestamps and details.

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

## Role Summary

| Endpoint | ADMIN | SECURITY_CHIEF | RECEPTIONIST | VISITOR |
|---|:---:|:---:|:---:|:---:|
| `GET /api/visits` | yes | yes | yes | no |
| `GET /api/visits/{visitId}` | yes | yes | yes | no |
| `POST /api/visits` | yes | yes | yes | no |
| `PUT /api/visits/{visitId}/exit` | yes | yes | yes | no |
| `PUT /api/visits/{visitId}/edit` | yes | yes | **no** | no |
| `GET /api/visits/{visitId}/timeline` | yes | yes | yes | no |
