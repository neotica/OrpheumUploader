# Orpheum API — endpoint reference

---

## Public

### `GET /`

Health check.

Response: `200 OK` — plain text `Orpheum API v0.1.0 is online and ready to stream.`

---

## Catalog

All paginated endpoints accept `?page=1&limit=10` (page ≥ 1, limit 1–50, default 10).

### `GET /catalog/albums/new`

Newest albums first.

Response: `200 OK` — `PaginatedResponse<Album>`

### `GET /catalog/tracks/new`

Newest tracks (sorted by parent album creation date).

Response: `200 OK` — `PaginatedResponse<Track>`

### `GET /catalog/new-releases`

Alias for `/catalog/albums/new`.

Response: `200 OK` — `PaginatedResponse<Album>`

### `GET /catalog/search?q=<query>`

Search tracks by title or artist name (case-insensitive LIKE).

Response: `200 OK` — `PaginatedResponse<Track>`

Errors: `400 Bad Request` — missing `q` parameter.

### `GET /artists/{artist_id}/albums`

Albums by a specific artist.

Response: `200 OK` — `List<Album>`

Errors: `400 Bad Request` — invalid UUID format.

### `GET /albums/{album_id}`

Album detail with its tracks.

Response: `200 OK` — `AlbumWithTracks { album: Album, tracks: List<Track> }`
`404 Not Found` — album not found.

### `GET /stream/{track_id}`

Proxies audio file from SeaweedFS storage. Content-Type derived from upstream.

Response: `200 OK` — raw audio bytes (streamed via `WriteChannelContent`)
`400 Bad Request` — missing track_id
`404 Not Found` — track not in database
`502 Bad Gateway` — storage server unreachable or returned non-200

### `GET /media/{token}`

Proxies a stored asset (e.g. album cover art) from SeaweedFS. The token is an AES-encrypted internal storage path, so `storage.neotica.id` is never exposed to clients. Cover paths are resolved through the `buckets/` S3 namespace; DB values remain root-relative (pending directory normalization).

Response: `200 OK` — raw asset bytes (streamed via `WriteChannelContent`)
`400 Bad Request` — missing or invalid/expired token
`502 Bad Gateway` — storage server unreachable or returned non-200

> All `cover_url` / `album_cover` values in `Album` / `Track` responses are relative paths of the form `/media/<token>`. Resolve them against the API origin the client already uses (same as `/stream/{track_id}`) — e.g. `<NEOSERVER_URL>/media/<token>`.

---

## Playlists

### `GET /playlists/{slug}`

**Auth:** optional JWT (public playlists readable without auth; private playlists require ownership).

Errors: `403 Forbidden` — private playlist accessed by non-owner.

Response: `200 OK` — `PlaylistWithTracks { playlist: Playlist, tracks: List<Track> }`

### `GET /playlists`

**Auth:** required JWT.

Lists the authenticated user's playlists (paginated).

Response: `200 OK` — `PaginatedResponse<Playlist>`

### `POST /playlists`

**Auth:** required JWT.

Body: `CreatePlaylistRequest { title: String, description?: String, is_public?: Boolean }`

Response: `201 Created` — `Playlist`

### `POST /playlists/{slug}/tracks`

**Auth:** required JWT.

Body: `PlaylistTrackRequest { track_id: String }`

Response: `200 OK` — success message
`400 Bad Request` — failed (e.g. not owner, invalid track)
`404 Not Found` — playlist not found

### `DELETE /playlists/{slug}/tracks/{track_id}`

**Auth:** required JWT.

Response: `200 OK` — success message
`400 Bad Request` — failed

---

## Admin

All admin endpoints require **JWT auth** (via `auth-jwt` provider).

### `POST /admin/artists`

Body: `CreateArtistRequest { name: String, bio?: String, image_url?: String }`

Response: `201 Created` — `Artist` (returns existing artist if name already exists)

### `POST /admin/albums`

Body: `CreateAlbumRequest { artist_id: String, title: String, release_year: Int, cover_url?: String }`

Response: `201 Created` — `Album` (returns existing album if same artist+title exists)
`400 Bad Request` — invalid artist_id

### `POST /admin/tracks`

**Multipart form** (not JSON):

| Field | Type | Required |
|---|---|---|
| `album_id` | String | yes |
| `title` | String | yes |
| `track_number` | Int | no (default 1) |
| `duration_seconds` | Int | no (default 0) |
| \<file\> (audio) | FileItem (MPEG) | yes |

Uploads audio to SeaweedFS at `orpheum/{UUID}-{safeName}`, then creates the DB row.

Response: `201 Created` — `Track`
`400 Bad Request` — missing fields, duplicate track title, or invalid album_id
`500 Internal Server Error` — SeaweedFS rejected or unreachable

### `PUT /admin/albums/{album_id}`

Body: `EditAlbumRequest { title?: String, release_year?: Int, cover_url?: String }`

Only provided fields are updated.

Response: `200 OK` — updated `Album`
`404 Not Found` — album not found

### `DELETE /admin/albums/{album_id}`

Deletes album from DB (cascades tracks).

Response: `200 OK` — success message
`404 Not Found` — album not found

### `DELETE /admin/tracks/{track_id}`

Deletes audio file from SeaweedFS, then removes DB row.

Response: `200 OK` — success message
`404 Not Found` — track not found

---

## Response models

### `Album`

```json
{
  "id": "uuid",
  "artist_id": "uuid",
  "title": "string",
  "release_year": 2024,
  "cover_url": "/media/<token> | null",
  "created_at": "2024-12-04T14:32:45.584",
  "artist_name": "string"
}
```

### `Track`

```json
{
  "id": "uuid",
  "album_id": "uuid",
  "title": "string",
  "duration_seconds": 240,
  "file_url": "orpheum/uuid-filename.mp3",
  "track_number": 1,
  "artist_name": "string",
  "album_cover": "/media/<token> | null"
}
```

### `Artist`

```json
{
  "id": "uuid",
  "name": "string",
  "bio": "string | null",
  "image_url": "string | null",
  "created_at": "2024-12-04T14:32:45.584"
}
```

### `Playlist`

```json
{
  "id": "uuid",
  "user_id": "string",
  "slug": "my-playlist",
  "title": "string",
  "description": "string | null",
  "is_public": false,
  "created_at": "2024-12-04T14:32:45.584"
}
```

### `PaginatedResponse<T>`

```json
{
  "data": [ ... ],
  "page": 1,
  "limit": 10,
  "total_items": 42,
  "total_pages": 5
}
```