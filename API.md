# API Reference

The ticketing JSON API is versioned under `/api/v1` and is served through the gateway at `http://localhost:8080`. The backend also renders the web UI; its browser routes are listed at the end of this document.

## Authentication and access

All API routes require an authenticated application user. Authentication uses the application's Spring Security session, so API clients should sign in through `/auth/login` and send the resulting session cookie on subsequent requests. POST and PATCH requests must include the session's CSRF token in the `X-CSRF-TOKEN` header. The browser UI handles form CSRF tokens automatically.

Customers can view and add notes only to their own tickets. Technicians, administrators, and system administrators can view all tickets. Ticket updates require one of those elevated roles. The gateway applies a Redis-backed token bucket to routed requests; defaults allow 10 requests per second with a burst capacity of 20 per client IP. Requests rejected by the limiter receive `429 Too Many Requests`.

## Ticket routes

| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/tickets` | Authenticated | List the caller's tickets, or all tickets for elevated users |
| `POST` | `/api/v1/tickets` | Authenticated | Create a ticket for the caller |
| `GET` | `/api/v1/tickets/{id}` | Owner or elevated role | Get a ticket and its notes |
| `PATCH` | `/api/v1/tickets/{id}` | Technician, admin, or sysadmin | Change status, importance, department, and optional assignee |
| `POST` | `/api/v1/tickets/{id}/notes` | Owner or elevated role | Add a note visible to users who can access the ticket |

### Create a ticket

```http
POST /api/v1/tickets
Content-Type: application/json
X-CSRF-TOKEN: <session-csrf-token>
Cookie: JSESSIONID=<session-id>

{
  "title": "VPN access is unavailable",
  "description": "The company VPN rejects my credentials.",
  "department": "INFORMATION_TECHNOLOGY",
  "requestor": "Alex Morgan"
}
```

Returns `201 Created`, a `Location: /api/v1/tickets/{id}` header, and the created ticket. `title`, `description`, and `requestor` must not be blank. `department` is required.

### List tickets

```http
GET /api/v1/tickets
Cookie: JSESSIONID=<session-id>
```

Returns `200 OK` and a JSON array. Customers see only their own tickets; elevated roles see all tickets.

### Get a ticket

```http
GET /api/v1/tickets/42
Cookie: JSESSIONID=<session-id>
```

Returns `200 OK` with a ticket object and its notes. A caller without access receives `403 Forbidden`; a missing ticket receives `404 Not Found`.

### Update a ticket

```http
PATCH /api/v1/tickets/42
Content-Type: application/json
X-CSRF-TOKEN: <session-csrf-token>
Cookie: JSESSIONID=<session-id>

{
  "status": "IN_PROGRESS",
  "importance": "HIGH",
  "department": "INFORMATION_TECHNOLOGY",
  "agentUsername": "atechnician001"
}
```

Returns `200 OK` with the updated ticket. `status`, `importance`, and `department` are required. Set `agentUsername` to a valid elevated user's username to assign the ticket; omit it to leave the existing assignment unchanged.

### Add a note

```http
POST /api/v1/tickets/42/notes
Content-Type: application/json
X-CSRF-TOKEN: <session-csrf-token>
Cookie: JSESSIONID=<session-id>

{
  "note": "I confirmed the VPN service is responding again."
}
```

Returns `201 Created` with the updated ticket, including the new note. The note text must not be blank.

## Ticket values

- **Status:** `OPEN`, `PENDING`, `PAUSED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`
- **Importance:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- **Department:** `INFORMATION_TECHNOLOGY`, `HUMAN_RESOURCES`, `ENGINEERING`, `MAINTENANCE`, `CLINICAL_INFORMATICS`, `SECURITY`, `MARKETING`, `FINANCE`

## Response shape

Ticket responses include `id`, `title`, `description`, `department`, `requestor`, `requestorUsername`, `agent`, `agentUsername`, `status`, `importance`, `ticketDate`, `ticketTime`, and `notes`. Each note includes its `id`, `username`, `note`, `date`, and `time`. Times are recorded in UTC.

Errors use this JSON shape:

```json
{
  "status": 404,
  "message": "Ticket not found"
}
```

Common responses are `400 Bad Request` for invalid input, `401 Unauthorized` for an unauthenticated request, `403 Forbidden` for an authenticated caller without access, `404 Not Found` for a missing resource, `429 Too Many Requests` for a gateway rate limit, and `503 Service Unavailable` when the circuit breaker cannot reach the ticketing service.

## Web UI routes

The same gateway also serves the Thymeleaf interface: `/auth/login`, `/home`, `/create`, `/tickets/{id}`, `/dashboard/`, and `/dashboard/reports`. Dashboard pages and user-management routes require elevated permissions.

The gateway exposes its health endpoint at `/actuator/health`. The backend health endpoint is kept on the internal Compose network.
