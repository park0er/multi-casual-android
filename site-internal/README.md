# site-internal — Multica Xiaomi internal landing page

A separate Vite + React landing page for the **Multica Xiaomi** internal
Android build. The public site at [`../site/`](../site/) targets the
Multi-Casual public release; this directory is a sibling project
published under the same GitHub Pages site at `/xiaomi/`.

## Visual scope

This page is a near-clone of the public site (`site/src/`) with three
intentional differences:

1. The "Relationship to Multica" section is removed.
2. The source link points to the private company GitLab project.
   The signed APK download points to a separate artifact-only GitHub
   release repository.
3. The iOS App button is left untouched and still points at the
   public iOS repo — the same `github.com/park0er/multi-casual-ios-app`
   source-of-truth is reused on purpose.

All screenshots, the promo video, the journey / workspace interactions,
the language toggle, and the layout are reused from `site/src/assets/`
so the visual stays in sync with the public site.

## Release metadata

`src/App.tsx` points the primary APK download button at the static
GitHub release asset:

```text
https://github.com/park0er/multica-xiaomi-apk/releases/download/v0.1.5-xiaomi/Multica-Xiaomi-v0.1.5.apk
```

The APK is copied from `app/build/outputs/apk/xiaomi/release/` into
`site-internal/public/downloads/` before deployment. Current metadata:

| Field | Value |
|---|---|
| `release.sha256` | `13e004472d33e66aa40aa7ebd58f75e22cfde8cce3a153729481f1be4d2d1414` |
| `release.gitlabUrl` | `https://git.n.xiaomi.com/zhaoxisheng/multica-xiaomi-android` |
| `release.releasesUrl` | `https://github.com/park0er/multica-xiaomi-apk/releases/download/v0.1.5-xiaomi/Multica-Xiaomi-v0.1.5.apk` |

Also confirm the `release.apiUrl` matches the staging or production
Xiaomi Multica endpoint you want this build to talk to
(`http://staging-multica.ad.xiaomi.srv` is the build.gradle default).

## Build

```bash
cd site-internal
npm install
npm run build
```

Output goes to `site-internal/dist/` and is gitignored.

## Deploy

The site is hosted as a subdirectory of the public GitHub Pages site:

```
https://park0er.github.io/multi-casual-android/xiaomi/
```

GitHub Actions builds the public site into the Pages root and this
internal Xiaomi site into `dist/xiaomi/` before deploying.

### Publish flow

1. Copy `site-internal/` into the public repo.
2. Commit and push the public repo.
3. GitHub Actions deploys both `site/` and `site-internal/`.

### EdgeOne status

Tencent EdgeOne hosting was attempted first but proved unreliable for
this internal distribution. The current source of truth for downloads is
GitHub Pages.
