# Contributing

Thanks for considering a contribution.

This is an independent Android companion app for Multica users. Contributions should keep that boundary clear and avoid implying endorsement by upstream project maintainers.

## License of contributions

By submitting a pull request or patch, you agree that your contribution is licensed under GPL-3.0-only, the same license used by this repository.

Do not submit code that you cannot license under GPL-3.0-only.

## Contribution rules

- Keep the app focused on the public Multica service workflow.
- Do not add normal-user runtime backend switching.
- Do not commit analytics keys, tokens, private endpoints, local credentials, or generated APKs.
- Keep analytics conservative: user consent first, minimal events, no content payloads.
- Preserve the public package identity: `ai.multicasual.app`.
- Keep README wording respectful and clear about the relationship to Multica.

## Development checks

Before proposing a functional Android change, run:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
ANDROID_HOME="$HOME/Library/Android/sdk" \
./gradlew testPublicDebugUnitTest assemblePublicDebug --console=plain
```

If your change touches dependencies or release packaging, update `THIRD_PARTY_NOTICES.md` and verify that the public APK still has a clear source tag and working analytics configuration.
