# Keycard Management — API Endpoints

## Base path: `/api/keycards`

All endpoints require a valid Bearer token (`Authorization: Bearer <token>`).

---

## Endpoints

### `GET /api/keycards`
**List keycards (paginated)**

Query parameters: `search` (card number, holder name, department), `status` (`available` | `in_use` | `disabled`), `page`, `size`

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `GET /api/keycards/{cardId}`
**Get keycard details**

Returns full details for a single keycard including holder, assignor, and possession history.

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `POST /api/keycards`
**Register new keycard**

Body: `keycardNumber`, `active`, `validUntil` (optional). Fails with 409 if the card number already exists.

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | denied (403) |
| VISITOR | denied (403) |

---

### `PUT /api/keycards/{cardId}`
**Update keycard**

Body: `keycardNumber` (optional), `active` (optional), `validUntil` (optional). Updates only the fields provided.

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | denied (403) |
| VISITOR | denied (403) |

---

### `POST /api/keycards/{cardId}/assign`
**Assign keycard to a person**

Body: `personInRoleId`, `accessPointId`, `assignedTime` (optional). Fails with 409 if the card is already assigned, inactive, or expired.

| Role | Access |
|---|---|
| ADMIN | allowed |
| SECURITY_CHIEF | allowed |
| RECEPTIONIST | allowed |
| VISITOR | denied (403) |

---

### `POST /api/keycards/{cardId}/return`
**Return keycard**

Body: `accessPointId`, `returnTime` (optional). Marks the active possession record as returned. Fails with 409 if the card is not currently assigned.

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
| `GET /api/keycards` | yes | yes | yes | no |
| `GET /api/keycards/{cardId}` | yes | yes | yes | no |
| `POST /api/keycards` | yes | yes | **no** | no |
| `PUT /api/keycards/{cardId}` | yes | yes | **no** | no |
| `POST /api/keycards/{cardId}/assign` | yes | yes | yes | no |
| `POST /api/keycards/{cardId}/return` | yes | yes | yes | no |
