# FairShare - working notes

Kotlin / Jetpack Compose / Room / Retrofit / Hilt. Min SDK 24, target 35, JVM target 17.

## Conventions

- Money is never a Double. Use `domain.model.Money`, which wraps Long cents.
- Room stores raw `Long` cents; conversion to `Money` happens in `data/mapper`.
- The domain package has no Android imports and no framework dependencies. Keep it that way -
  it is the part that is unit tested on the JVM without Robolectric.
- Room is the single source of truth. The UI observes Flows from the DAO only, never the API.
- Writes go to Room first with `pendingSync = true`, then attempt the network.

## Before committing

    ./gradlew test

Any change to split, balance, or settlement logic needs a test that fails without it.

## Things deliberately left out

- Authentication, multi-group navigation, and expense editing.
- Instrumented tests. The logic worth testing is in `domain`, which is JVM-testable.
