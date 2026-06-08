# Multi-Casual Android

English | [简体中文](#简体中文)

An independent Android companion app for Multica users.

This repository publishes an Android app that works with the Multica backend service. It is not maintained, endorsed, or published by the Multica project maintainers, and it should not be presented as an Android app from the upstream Multica project.

The app exists for users who already use Multica and want the core workspace workflow on Android: issue browsing, issue details, comments, projects, agents, skills, labels, and settings.

- Multica website: https://multica.ai
[![release](https://img.shields.io/badge/release-v0.1.2-E4DCC5?style=flat-square&labelColor=2B2A28)](https://github.com/park0er/multi-casual-android/releases/latest) [![cn site](https://img.shields.io/badge/cn%20site-multi--casual--china--8temdtzs.zh--cn.edgeone.cool-E4DCC5?style=flat-square&labelColor=2B2A28)](https://multi-casual-china-8temdtzs.zh-cn.edgeone.cool/) [![global site](https://img.shields.io/badge/global%20site-multi--casual--android.pages.dev-E4DCC5?style=flat-square&labelColor=2B2A28)](https://multi-casual-android.pages.dev/)

- Multica web app: https://app.multica.ai
- Service API used by this app: `https://api.multica.ai`
- Android package: `ai.multicasual.app`

## Relationship to Multica

Multica is the original product and backend service. Multi-Casual Android is an independent companion client for that service.

The name “Multi-Casual” is used here to describe this Android client and its compatibility with Multica workflows. Product ownership, trademarks, service availability, and backend behavior remain with the Multica project and its maintainers.

This project follows the public guidance shared by the Multica maintainers about keeping Android app work independent and clearly attributed. See the related discussion: https://github.com/multica-ai/multica/issues/2285#issuecomment-4414513596

## What this app does

- Sign in with the email-code flow used by the Multica service.
- Browse workspaces, inbox items, issues, projects, agents, runtimes, skills, labels, and settings.
- Create and update issues, comments, labels, projects, agents, skills, and related resources through the Multica service API.
- Bring the everyday Multica workspace loop to Android without bundling a server.

## Privacy and analytics

Analytics are opt-in. The app asks for consent before sending analytics.

After consent, the public Android build reports only the `app_open` event:

- Mainland China route: Umeng
- Global route: PostHog
- The app does not intentionally report page views, chat content, file content, email addresses, tokens, or precise location.

You can also build the app without analytics keys. In that case, analytics providers will not start.

## Download signed APK

For normal installation, download the signed release APK from GitHub Releases:

- Latest release: https://github.com/park0er/multi-casual-android/releases/latest
- Current signed APK: `Multi-Casual-Android-v0.1.2.apk`
- SHA256: `989ff62edc6af48263bf671ad79d64500cfe7a0accd9b5c8d482ca2994e89f7b`

The debug APK produced by local development commands is not intended for public distribution.

## Build

Requirements:

- Android Studio / Android SDK
- JDK 17; Android Studio's bundled JBR is recommended

Create local configuration:

```bash
cp local.properties.example local.properties
```

Optional analytics keys can be filled in locally. Do not commit real keys.

Build a local debug APK for development:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
ANDROID_HOME="$HOME/Library/Android/sdk" \
./gradlew assemblePublicDebug --console=plain
```

APK output:

```text
app/build/outputs/apk/public/debug/app-public-debug.apk
```

Install locally:

```bash
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/public/debug/app-public-debug.apk
```

## Local analytics configuration

`local.properties.example` documents optional local keys:

```properties
multicaUmengPublicAppKey=
multicaPostHogPublicApiKey=
multicaPostHogPublicHost=https://us.i.posthog.com
multicaAnalyticsRoute=auto
```

Use `multicaAnalyticsRoute=auto` for normal public builds. The app chooses the analytics route at runtime after user consent.

## Contributing

Contributions are welcome if they keep the boundaries clear:

- Keep this as an independent Android companion app for Multica users.
- Do not imply Multica maintainer endorsement.
- Do not add runtime backend switching for normal users.
- Do not commit secrets, analytics keys, private endpoints, or company-internal material.
- Keep privacy-sensitive changes conservative and easy to review.

## License

This repository is licensed under **GPL-3.0-only**. See `LICENSE` for the full license text.

If you distribute a modified APK or other binary based on this project, keep the corresponding source code available under GPL-3.0-only and preserve the third-party notices.

---

# 简体中文

这是一个面向 Multica 用户的独立 Android companion app。

本仓库发布的是一个对接 Multica 后端服务的 Android App。它并不由 Multica 项目维护者维护、背书或发布，也不应该被表述为上游 Multica 项目推出的 Android App。

这个 App 面向已经使用 Multica 的用户，把核心 workspace 工作流带到 Android 上：Issue 浏览、Issue 详情、评论、项目、Agent、Skill、Label 和设置等能力。

- Multica 网站：https://multica.ai
- Multica Web 应用：https://app.multica.ai
- 本 App 使用的服务 API：`https://api.multica.ai`
- Android 包名：`ai.multicasual.app`

## 与 Multica 的关系

Multica 是原始产品和后端服务。Multi-Casual Android 是面向该服务的独立 companion 客户端。

这里使用 “Multi-Casual” 名称，是为了说明这个 Android 客户端及其与 Multica 工作流的兼容关系。产品归属、商标、服务可用性和后端行为都仍属于 Multica 项目及其维护者。

这个项目遵循了 Multica maintainers 在公开 issue 中给出的 guidance：Android app 工作应保持独立维护、清晰署名并尊重原项目边界。相关讨论见：https://github.com/multica-ai/multica/issues/2285#issuecomment-4414513596

## App 能做什么

- 使用 Multica 服务的邮箱验证码流程登录。
- 浏览 workspace、Inbox、Issue、Project、Agent、Runtime、Skill、Label 和 Settings。
- 通过 Multica 服务 API 创建和更新 Issue、评论、标签、项目、Agent、Skill 等资源。
- 在不内置服务端的前提下，把日常 Multica workspace 闭环带到 Android。

## 隐私与 analytics

Analytics 是 opt-in。App 会先请求用户同意，然后才会上报 analytics。

同意后，公开 Android build 只上报 `app_open` 启动事件：

- 中国大陆路线：友盟
- 全球路线：PostHog
- App 不会有意上报页面访问、聊天内容、文件内容、邮箱、token 或精确位置。

如果本地不配置 analytics key，analytics provider 不会启动。

## 下载签名 APK

普通用户安装时，请从 GitHub Releases 下载签名 release APK：

- 最新 release：https://github.com/park0er/multi-casual-android/releases/latest
- 当前签名 APK：`Multi-Casual-Android-v0.1.2.apk`
- SHA256：`989ff62edc6af48263bf671ad79d64500cfe7a0accd9b5c8d482ca2994e89f7b`

本地开发命令生成的 debug APK 不用于公开分发。

## 构建

环境要求：

- Android Studio / Android SDK
- JDK 17；推荐使用 Android Studio 自带 JBR

创建本地配置：

```bash
cp local.properties.example local.properties
```

可选 analytics key 只填在本地。不要提交真实 key。

构建本地开发用 debug APK：

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
ANDROID_HOME="$HOME/Library/Android/sdk" \
./gradlew assemblePublicDebug --console=plain
```

APK 输出：

```text
app/build/outputs/apk/public/debug/app-public-debug.apk
```

本地安装：

```bash
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/public/debug/app-public-debug.apk
```

## 本地 analytics 配置

`local.properties.example` 记录了可选本地 key：

```properties
multicaUmengPublicAppKey=
multicaPostHogPublicApiKey=
multicaPostHogPublicHost=https://us.i.posthog.com
multicaAnalyticsRoute=auto
```

普通公开 build 使用 `multicaAnalyticsRoute=auto`。App 会在用户同意后自动选择 analytics 路线。

## 贡献

欢迎贡献，但需要保持边界清楚：

- 保持这是面向 Multica 用户的独立 Android companion app。
- 不暗示 Multica 维护者背书。
- 不为普通用户加入运行时后端切换。
- 不提交 secrets、analytics key、私有 endpoint 或公司内部材料。
- 涉及隐私的改动要保守、清晰、容易审阅。

## License

本仓库使用 **GPL-3.0-only** 授权。完整协议文本见 `LICENSE`。

如果你分发基于本项目修改后的 APK 或其他二进制产物，需要按 GPL-3.0-only 提供对应源码，并保留第三方依赖 notices。
