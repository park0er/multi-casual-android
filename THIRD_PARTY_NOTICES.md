# Third-Party Notices

This project is licensed under GPL-3.0-only. It also uses third-party open source libraries with their own licenses.

This notice is informational and does not replace the license text shipped by each dependency.

## Android runtime dependencies

| Dependency group | Representative artifacts | License |
|---|---|---|
| AndroidX / Jetpack Compose | `androidx.activity`, `androidx.compose`, `androidx.lifecycle`, `androidx.core` | Apache-2.0 |
| Kotlin / Kotlinx | `org.jetbrains.kotlin`, `org.jetbrains.kotlinx` | Apache-2.0 |
| OkHttp / Okio | `com.squareup.okhttp3`, `com.squareup.okio` | Apache-2.0 |
| Coil | `io.coil-kt:coil-compose` | Apache-2.0 |
| Lottie | `com.airbnb.android:lottie-compose` | Apache-2.0 |
| Haze | `dev.chrisbanes.haze:haze` | Apache-2.0 |
| Cupertino UI | `io.github.alexzhirkevich:cupertino` | Apache-2.0 |
| Umeng SDK | `com.umeng.umsdk:common`, `com.umeng.umsdk:asms` | Apache-2.0 |
| PostHog Android SDK | `com.posthog:posthog-android`, `com.posthog:posthog` | MIT |
| Guava listenablefuture placeholder | `com.google.guava:listenablefuture` | Apache-2.0; Maven POM has limited license metadata |

## Notes for redistributors

If you redistribute a binary build, keep this file, the GPL-3.0-only `LICENSE`, and any dependency notices required by the upstream libraries you include.

If you change dependency versions, regenerate this notice from the release dependency tree before publishing a new APK.
