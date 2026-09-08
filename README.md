# Split Bill API

A Spring Boot REST API for recording shared expenses and calculating a deterministic settlement summary for a group. The service stores bill groups, participants, expenses, and split allocations in PostgreSQL using Flyway migrations.

## Technical Decisions

- Java 17, Spring Boot 4.1.1, and Maven
- PostgreSQL with Flyway-managed schema migrations
- `BigDecimal` with scale 0 for all IDR monetary values
- Equal split strategy with deterministic remainder allocation in request order
- Greedy settlement after balance netting, with deterministic UUID tie-breaking
- RFC Problem Details responses for validation and domain errors

The settlement algorithm removes redundant intermediate payments and produces a compact list of transfers. It is intentionally not presented as a mathematical global-minimum solver for every possible balance combination.

## Service Charge

GitHub username: **bluntswordman**

The application lowercases the configured username to `bluntswordman`, sums its character values to `1424`, and calculates `1424 % 10`. Therefore, every settlement response contains a computed `service_charge_pct` of **4**.

The `service_charge_amount` is 4% of total group expenses, rounded to whole rupiah with `HALF_UP`. This value is informational and is not added to participant debts. The username defaults to `bluntswordman` and can be overridden with the `GITHUB_USERNAME` environment variable, while the percentage itself is computed in code and not hardcoded.

## Build and Test

Prerequisites for local build and tests:

- Java 17+
- Docker-compatible runtime for PostgreSQL integration tests, such as Docker or Podman

Run all unit and PostgreSQL integration tests:

```bash
./mvnw test
```

Testcontainers starts and removes PostgreSQL automatically. If Docker or Podman is unavailable, the integration test is skipped while pure calculation tests still run.

Build the executable JAR:

```bash
./mvnw clean package
```

## Run with Docker or Podman

The project includes a multi-stage `Dockerfile` and a `compose.yaml`, so Java, Maven, and PostgreSQL do not need to be installed locally for containerized execution.

Run with Docker:

```bash
docker compose up --build
```

Run with Podman:

```bash
podman compose up --build
```

The API is available at:

```text
http://localhost:4110
```

Stop services while preserving database data:

```bash
docker compose down
```

or:

```bash
podman compose down
```

Stop services and remove PostgreSQL data:

```bash
docker compose down -v
```

or:

```bash
podman compose down -v
```

For a focused container run guide, see `RUN_WITH_DOCKER_OR_PODMAN.md`.

## Run Locally

Start only PostgreSQL:

```bash
docker compose up -d database
```

or:

```bash
podman compose up -d database
```

Then run the application:

```bash
./mvnw spring-boot:run
```

The default local connection is `jdbc:postgresql://localhost:5432/split_bill` with username and password `split_bill`. Override it with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.

## API Examples

All amounts are positive whole rupiah values. Replace the example UUID placeholders below with IDs returned by the create-group response.

You can also import the Postman collection from `postman/split-bill-api.postman_collection.json`. Run the `Happy Path` folder in order after the API is available at `http://localhost:4110`; the collection stores returned IDs automatically as collection variables.

### 1. Create a Group

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

Example response:

```json
{
  "id": "<group-id>",
  "name": "Bali Trip",
  "participants": [
    {"id": "<bedy-id>", "name": "Bedy", "created_at": "..."},
    {"id": "<ani-id>", "name": "Ani", "created_at": "..."},
    {"id": "<doni-id>", "name": "Doni", "created_at": "..."}
  ],
  "created_at": "..."
}
```

### 2. Add an Expense

The beneficiary order determines who receives any remainder. For example, 100000 split between three people is allocated as 33334, 33333, and 33333.

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

The payer may be omitted from the beneficiary list, but every referenced participant must belong to the group in the URL.

### 3. Get the Settlement

```bash
curl http://localhost:4110/api/v1/groups/<group-id>/settlements
```

Example response for the expense above:

```json
{
  "group_id": "<group-id>",
  "total_expenses": 100000,
  "service_charge_pct": 4,
  "service_charge_amount": 4000,
  "balances": [
    {"participant_id": "<ani-id>", "participant_name": "Ani", "net_balance": -33333},
    {"participant_id": "<bedy-id>", "participant_name": "Bedy", "net_balance": 66666},
    {"participant_id": "<doni-id>", "participant_name": "Doni", "net_balance": -33333}
  ],
  "settlements": [
    {
      "from_participant_id": "<ani-id>",
      "from_participant_name": "Ani",
      "to_participant_id": "<bedy-id>",
      "to_participant_name": "Bedy",
      "amount": 33333
    },
    {
      "from_participant_id": "<doni-id>",
      "from_participant_name": "Doni",
      "to_participant_id": "<bedy-id>",
      "to_participant_name": "Bedy",
      "amount": 33333
    }
  ]
}
```

A positive `net_balance` means the participant should receive money. A negative value means the participant owes money.

## Validation Rules

- A group requires a non-blank name and at least two participants.
- Participant names must be unique within a group, ignoring case and surrounding whitespace.
- An expense requires a description, a positive whole-rupiah amount, one payer, and at least one unique beneficiary.
- The amount must be at least the number of beneficiaries so every stored share is at least 1.
- Payer and beneficiaries must belong to the requested group.

## Submission Question

**What was the hardest design decision you made while building this, and what trade-off did you accept?**

The hardest decision was defining what settlement optimization should promise. I chose to net every participant's balance and use a deterministic greedy matcher because it is easy to verify, explain, and extend during an interview. This produces a compact settlement with at most `n - 1` transfers, but I accepted that it does not guarantee the mathematical minimum number of transfers for every possible balance combination. That trade-off keeps the core financial behavior predictable without introducing a combinatorial search algorithm into a small service.
