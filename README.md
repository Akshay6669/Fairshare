# FairShare

[![Android CI](https://github.com/Akshay6669/Fairshare/actions/workflows/android.yml/badge.svg)](https://github.com/Akshay6669/Fairshare/actions/workflows/android.yml)

Split shared expenses without losing a cent.

Kotlin · Jetpack Compose · Room · Retrofit · Hilt · Coroutines

---

## The problem this is actually about

Splitting a bill is arithmetic until you try to do it exactly.

`$10.00` between three people is `$3.33` each, which is `$9.99`. A cent has vanished. Do that
across a weekend of expenses and the group's balances never reach zero, so the app can never
tell anyone they're settled. The usual culprit is storing money as a `Double`, where
`0.1 + 0.2 == 0.30000000000000004` and every split compounds the error.

FairShare stores every amount as a `Long` of cents and splits using the largest remainder
method, so the parts always add back up to the whole. Leftover cents are assigned
deterministically — same input, same output, on every device.

The second problem is settling up. Three people with tangled balances can owe each other in a
cycle, and paying everyone you owe directly produces O(n²) transfers. `SettlementCalculator`
repeatedly matches the largest debtor against the largest creditor, which clears at least one
person from the books each pass and finishes in at most n−1 payments.

## Architecture

```
domain/          Pure Kotlin. No Android imports, no framework types.
  model/         Money, Expense, Balance, Settlement
  split/         SplitCalculator   — divides an amount without rounding drift
  balance/       BalanceCalculator — expenses → net position per member
  settle/        SettlementCalculator — balances → minimal payment list

data/
  local/         Room entities, DAO, database. Amounts stored as Long cents.
  remote/        Retrofit API + kotlinx.serialization DTOs
  mapper/        Entity ↔ domain ↔ DTO conversion, isolated in one place
  repository/    Offline-first reconciliation

ui/group/        Compose screen + ViewModel exposing a single UiState
di/              Hilt modules
```

**Room is the single source of truth.** The UI observes DAO `Flow`s and never touches the API
directly. Adding an expense writes to Room immediately with `pendingSync = true`, then attempts
a push; if the network fails, the local write stands and syncs later. Adding an expense
underground works exactly like adding one on wifi.

**The domain layer has no Android dependencies.** That's what makes the interesting logic
testable as plain JVM unit tests — no Robolectric, no emulator, runs in about a second.

## Testing

```bash
./gradlew test
```

The test suite targets the money logic specifically:

- Every amount from `0.00` to `20.00`, split between one and seven people, is asserted to
  preserve the total exactly — no cent created or destroyed in any of those combinations.
- No two shares of the same expense may differ by more than one cent.
- Group balances are asserted to sum to zero as an invariant.
- 500 randomised balance sets are settled and replayed, asserting every member ends at zero
  and no set needs more than n−1 payments.

Splits and settlements are deterministic, so these tests don't flake.

## Running it

1. Clone and open in Android Studio (Ladybug or newer).
2. Add a base URL to `local.properties` — this file is gitignored and never committed:

   ```properties
   API_BASE_URL=https://your-backend.example.com/api/
   ```

   Without it the build uses a placeholder and the app still runs offline.
3. Run on a device or emulator with API 24+.

## Not built yet

Honest list, because a portfolio repo shouldn't imply more than it has:

- Authentication and real user accounts
- Creating and switching between multiple groups from the UI
- Editing or deleting an existing expense
- Recording that a settlement was actually paid
- Instrumented UI tests

The API layer is written against a backend contract that isn't implemented; the app is fully
functional offline.

## License

MIT
