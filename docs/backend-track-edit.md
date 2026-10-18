# Backend request — track editing & reordering

**Status:** app-side implemented; backend endpoints not yet deployed. The app's
edit/reorder actions will fail (404) until these endpoints exist.

## Why

The uploader app's Album Detail screen supports:

- editing a track's title and track number
- reordering the album tracklist (up/down arrows)
- deleting a track (already shipped)

Deletion exists (`DELETE /admin/tracks/{track_id}`), but there is currently no
way to update a track or reorder tracks. The app needs the two endpoints below.

---

## 1. Update track

`PUT /admin/tracks/{track_id}`

**Auth:** JWT (same admin provider as the other `/admin/*` endpoints)

**Body** (JSON, partial update — only provided fields change):

| Field | Type | Required | Notes |
|---|---|---|---|
| `title` | String | no | New track title |
| `track_number` | Int | no | New position (1-based) |

**Response:** `200 OK` — updated `Track`

**Errors:**
- `400 Bad Request` — invalid body (e.g. blank title, non-positive `track_number`)
- `404 Not Found` — track not found

**Implementation notes:**
- If `(album_id, track_number)` has a unique constraint, a direct update can
  collide when swapping numbers (e.g. `1 → 2` while `2` exists). Either defer
  the constraint / shift numbers inside a transaction, or allow transient
  duplicates and let the reorder endpoint normalize.
- Keep `title` validation consistent with `POST /admin/tracks` (per-album
  duplicate-title handling, if any).

---

## 2. Reorder album tracks

`PUT /admin/albums/{album_id}/tracks/reorder`

**Auth:** JWT (same admin provider)

**Body** (JSON):

```json
{ "track_ids": ["uuid-1", "uuid-2", "uuid-3"] }
```

**Semantics:**
- `track_ids` must contain **exactly** the set of the album's tracks (same
  length, same IDs; array order defines the new positions).
- In a single transaction, assign `track_number = 1..N` following the array order.

**Response:** `200 OK` — success message (or the updated track list)

**Errors:**
- `400 Bad Request` — ID set does not match the album's tracks
- `404 Not Found` — album not found

---

## App-side contract summary

- `CatalogRepository.updateTrack(trackId, title?, trackNumber?)` →
  `PUT {BASE_URL}/orpheum/admin/tracks/{id}`, parses the `Track` response.
- `CatalogRepository.reorderTracks(albumId, trackIds)` →
  `PUT {BASE_URL}/orpheum/admin/albums/{id}/tracks/reorder`, treats any 2xx as success.
- Both are called with the stored JWT (Ktor `Auth` bearer plugin).

## Out of scope (for now)

- Playlist track reordering (no endpoint; `PlaylistWithTracks` exposes no
  position field).
- Editing `duration_seconds` (the app does not send it; add if needed).
