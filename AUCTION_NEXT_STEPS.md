# Next Steps for BidMart Auction Service

Based on the BidMart project specification, below are the required next steps to complete the `bidmart-auction-service`. Please implement these features sequentially.

---

## 🛠️ Git Workflow and Branching Strategy (CRITICAL)

This project strictly adheres to Test-Driven Development (TDD) and a standardized Git workflow. You **MUST** follow these conventions for all operations:

### 1. Branching Strategy
- Do **NOT** commit directly to `main` or `staging`.
- All new work must be performed on a dedicated feature or fix branch.
- Branch naming convention: `feat/<feature-name>`, `fix/<issue-name>`, etc.
- Upon completion, push the branch and open a **Pull Request (PR) to the `staging` branch**.

### 2. TDD Commit Flow
Because this project emphasizes TDD, your commits should explicitly reflect the Red-Green-Refactor cycle:
- `test: [red] <message>` — Use this when adding a failing test.
- `test: [green] <message>` — Use this when implementing the code to make the test pass.
- `refactor: <message>` — Use this when refactoring code without changing existing behavior.

### 3. Conventional Commits
For all regular non-TDD commits, use standard conventional commit prefixes:
- `feat: <message>` — For new features.
- `fix: <message>` — For bug fixes.
- `chore: <message>` — For maintenance, config updates, or dependency changes.
- `docs: <message>` — For documentation updates.

---

## 📝 Feature Implementation Steps

### 1. Bid History Endpoint (Quick Win)
**Spec Context:** *Pembeli dapat melihat riwayat penawaran pada sebuah lelang.*
- Add a new repository method in `BidRepository` to fetch bids by `auctionId`, sorted by `bidTime` descending.
- Add an endpoint `GET /api/v1/auctions/{auctionId}/bids` in the `AuctionController`.
- Return a list or paginated response of `BidResponseDTO`.

### 2. Auction Lifecycle Scheduler
**Spec Context:** *Ketika waktu lelang berakhir, sistem mengevaluasi apakah harga cadangan terpenuhi untuk menentukan transisi ke WON atau UNSOLD.*
- Create a Spring `@Scheduled` job that runs frequently (e.g., every 10-60 seconds).
- Query for auctions where `endTime` has passed (`< now()`) AND `status` is in (`ACTIVE`, `EXTENDED`).
- For each auction, evaluate if `currentHighestBid >= reservePrice`.
- If true, update status to `WON`. If false, update status to `UNSOLD`.
- Save the updated statuses to the database.

### 3. Synchronous Wallet Integration
**Spec Context:** *Sebelum menerima penawaran, modul ini harus memastikan pembeli memiliki saldo yang cukup melalui Modul Dompet. Permintaan penahanan dana harus dijawab dengan cepat...*
- In `AuctionServiceImpl.placeBid()`, implement a synchronous HTTP call (using `RestTemplate` or `WebClient`) to the Wallet Service before saving the bid.
- Request the Wallet Service to **hold** the new bidder's funds.
- If the bid is accepted and there was a prior highest bidder, make another call to the Wallet Service to **release** the previously held funds for that outbid user.
- Handle failures appropriately (e.g., if Wallet Service errors out, abort the bid).

### 4. API to Start/Initialize an Auction
**Context:** Currently, auctions are only loaded via `DataSeeder`. 
- Implement a `POST /api/v1/auctions` endpoint (or a message queue listener, depending on agreed architecture).
- This endpoint should accept initial parameters from the Catalogue Service (when a seller publishes a listing) and create the `Auction` record with a `DRAFT` or `ACTIVE` status.

### 5. Setup Asynchronous Event Publishing
**Spec Context:** *Ketika penawaran berhasil, modul lain yang berkepentingan... perlu mengetahui informasi tersebut.*
- Setup an event publishing mechanism (e.g., RabbitMQ, Kafka, or Spring Events if staying within a monolith temporarily).
- **Event 1:** Publish `BidPlacedEvent` when a new bid is successfully placed, so the Catalogue Service can update the current display price.
- **Event 2:** Publish `AuctionEndedEvent` / `WinnerDeterminedEvent` when the scheduled job closes an auction, so the Wallet service can permanently deduct the funds and Notification service can alert users.

> **Note to Agent:** Treat the Database Entity constraints and Pessimistic Locking already present in `placeBid()` as the source of truth for concurrency control. Do not remove the anti-sniping or pessimistic write locking mechanisms.
