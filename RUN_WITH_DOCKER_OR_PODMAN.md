# Run Project with Docker or Podman

Panduan ini untuk menjalankan Split Bill API langsung dengan container runtime. Tidak perlu install Java, Maven, atau PostgreSQL di local machine karena semuanya dijalankan lewat Compose.

## Prerequisites

Pilih salah satu:

- Docker Desktop dengan Docker Compose
- Podman Desktop atau Podman CLI dengan Podman Compose

Untuk Podman di macOS, pastikan Podman machine sudah berjalan:

```bash
podman machine start
```

## Run with Docker

Dari root project, jalankan:

```bash
docker compose up --build
```

Tunggu sampai log aplikasi menunjukkan Spring Boot sudah start. API akan tersedia di:

```text
http://localhost:4110
```

Stop service:

```bash
docker compose down
```

Stop service sekaligus hapus data PostgreSQL container:

```bash
docker compose down -v
```

## Run with Podman

Dari root project, jalankan:

```bash
podman compose up --build
```

Tunggu sampai log aplikasi menunjukkan Spring Boot sudah start. API akan tersedia di:

```text
http://localhost:4110
```

Stop service:

```bash
podman compose down
```

Stop service sekaligus hapus data PostgreSQL container:

```bash
podman compose down -v
```

## Quick API Test

Setelah API berjalan, test endpoint create group:

```bash
curl --request POST http://localhost:4110/api/v1/groups \
  --header 'Content-Type: application/json' \
  --data '{
    "name": "Bali Trip",
    "participants": [
      {"name": "Bedy"},
      {"name": "Ani"},
      {"name": "Doni"}
    ]
  }'
```

Response akan berisi `id` group dan `id` masing-masing participant. Gunakan ID tersebut untuk request add expense:

```bash
curl --request POST http://localhost:4110/api/v1/groups/<group-id>/expenses \
  --header 'Content-Type: application/json' \
  --data '{
    "description": "Makan malam",
    "amount": 100000,
    "paid_by_participant_id": "<bedy-id>",
    "beneficiary_participant_ids": ["<bedy-id>", "<ani-id>", "<doni-id>"]
  }'
```

Lalu ambil settlement:

```bash
curl http://localhost:4110/api/v1/groups/<group-id>/settlements
```

Response settlement harus memuat:

```json
{
  "service_charge_pct": 4,
  "service_charge_amount": 4000
}
```

Nilai tersebut dihitung dari GitHub username `bluntswordman`.

## Test with Postman

Import collection berikut ke Postman:

```text
postman/split-bill-api.postman_collection.json
```

Pastikan collection variable `base_url` bernilai:

```text
http://localhost:4110
```

Jalankan folder `Happy Path` secara berurutan:

1. `1. Create Group`
2. `2. Add Expense`
3. `3. Get Settlement`

Collection akan menyimpan `group_id`, `bedy_id`, `ani_id`, dan `doni_id` otomatis dari response create group.

## Troubleshooting

Jika Podman menampilkan error base image untuk `arm64`, pastikan `Dockerfile` memakai image berikut:

```dockerfile
FROM eclipse-temurin:17-jdk-jammy AS builder
FROM eclipse-temurin:17-jre-jammy AS runtime
```

Jika port `4110` sudah dipakai, ubah mapping port di `compose.yaml`:

```yaml
ports:
  - "4111:4110"
```

Lalu akses API dari host lewat:

```text
http://localhost:4111
```

Jika database ingin benar-benar reset, jalankan:

```bash
docker compose down -v
```

atau:

```bash
podman compose down -v
```
