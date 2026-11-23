# Backend request — list artists

**Status:** app-side implemented; endpoint not yet deployed. The upload wizard's
artist picker shows an error when `/artists` is missing, while the "create new
artist" path keeps working.

## Why

The upload wizard lets users attach a track to an existing artist instead of
creating a new one each time. Albums are already listable
(`GET /catalog/albums/new`, `GET /artists/{id}/albums`), but there is no way to
list artists, so the picker has nothing to show.

## Endpoint

`GET /artists?page=&limit=&q=`

**Auth:** none (public, like the catalog endpoints)

**Query parameters:**

| Param | Type | Required | Notes |
|---|---|---|---|
| `page` | Int | no | >= 1, default 1 |
| `limit` | Int | no | 1–50, default 20 |
| `q` | String | no | case-insensitive "contains" filter on `name` |

**Response:** `200 OK` — `PaginatedResponse<Artist>`

```json
{
  "data": [
    {
      "id": "uuid",
      "name": "string",
      "bio": "string | null",
      "image_url": "string | null",
      "created_at": "2024-12-04T14:32:45.584"
    }
  ],
  "page": 1,
  "limit": 20,
  "total_items": 42,
  "total_pages": 3
}
```

**Semantics:**
- Ordered by `name` ascending (stable, so paging is deterministic).
- `q` matches `name` case-insensitively (same behavior as `/catalog/search`).

**Errors:**
- `400 Bad Request` — invalid `page` or `limit` (if you validate them).

## App-side contract summary

- `CatalogRepository.getArtists(page, limit, query?)` →
  `GET {BASE_URL}/orpheum/artists?page=&limit=&q=`, parses `ArtistFeedResponse`.
- The picker dialog debounces search input (~300ms) and requests
  `limit = 50` for the first page.
- `CatalogRepository.getArtistAlbums(artistId)` →
  `GET {BASE_URL}/orpheum/artists/{id}/albums` (already exists in your API,
  newly consumed by the app).
