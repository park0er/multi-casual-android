import { useState } from "react";
import { motion, useReducedMotion, type MotionProps } from "motion/react";
import issueDetail from "./assets/screenshots/02-issue-detail.png";
import commentFocused from "./assets/screenshots/03-comment-focused.png";
import promoVideo from "./assets/video/multica-android-promo-funk-final.mp4";

const release = {
  packageName: "ai.multicasual.app",
  sha256: "0b34ab0d164403aec655ca9e829b3fce6a2849f4a83dc1f631336ff725299c42",
  githubUrl: "https://github.com/park0er/multi-casual-android",
  releasesUrl: "https://github.com/park0er/multi-casual-android/releases/latest",
  multicaUrl: "https://multica.ai",
  serviceUrl: "https://app.multica.ai",
  apiUrl: "https://api.multica.ai",
};

type Locale = "en" | "zh";

const copy = {
  en: {
    nav: {
      brand: "Multi-Casual Android",
      download: "Download",
      trust: "Trust",
      relationship: "Relationship",
      language: "中文",
    },
    hero: {
      eyebrow: "Independent companion app · Built for Multica workflows",
      title: "Review issues, reply faster, stay in flow on Android.",
      subtitle:
        "An independent Android companion app for people who already use Multica and want the core workspace loop on a phone.",
      download: "Download APK",
      github: "View GitHub",
      multica: "Visit Multica",
      typing: "Looks good — verified on Android.",
    },
    relationship: {
      kicker: "Relationship to Multica",
      title: "Respectful, explicit, and independent.",
      body:
        "Multi-Casual Android is an independent companion app for Multica users. It connects to Multica service APIs for compatibility, but it is not maintained, endorsed, or published by the Multica project maintainers, and it should not be presented as a Multica Android app from the upstream project.",
    },
    featuresKicker: "What it covers",
    features: [
      {
        title: "Issue workflows",
        body: "Browse inbox items, open issue details, review comments, and keep task context close while away from desktop.",
      },
      {
        title: "Mobile replies",
        body: "Tap into the comment flow, draft a response, and continue the same discussion you would normally handle on the web.",
      },
      {
        title: "Cloud-service compatible",
        body: "The Android app is designed as a companion to the Multica backend service. It does not bundle a server, and it does not add runtime backend switching for normal users.",
      },
    ],
    demo: {
      kicker: "Real Android flow",
      title: "Open an issue. Read context. Draft a comment.",
      body:
        "The demo material uses emulator screenshots from a real project. Interactive moments are animated as safe overlays, so no public test comment needs to be submitted.",
    },
    video: {
      kicker: "Promo video",
      title: "A quick walkthrough of the Android issue flow.",
      body:
        "The walkthrough shows a real emulator project, issue context, a simulated typewriter-style comment draft, and a light funk-style background track.",
    },
    download: {
      kicker: "GitHub APK",
      title: "Install from GitHub Releases.",
      package: "Package",
      api: "Service API",
      sha: "APK SHA256",
      latest: "Latest release",
      source: "Source code",
    },
    trustKicker: "Trust and privacy",
    trustItems: [
      "Independent companion app; no claim of Multica project ownership or maintainer endorsement.",
      "Published as its own Android app for Multica users, while keeping attribution and product boundaries explicit.",
      "Analytics requires user consent and is limited to an app_open event in the public Android build.",
      "No chat content, file content, email address, tokens, or precise location are intentionally reported.",
    ],
    footer: {
      tagline: "Independent Android companion app for Multica users. GitHub-distributed APK.",
      multica: "Multica website",
      github: "GitHub",
      download: "Download APK",
    },
  },
  zh: {
    nav: {
      brand: "Multi-Casual Android",
      download: "下载",
      trust: "信任说明",
      relationship: "项目关系",
      language: "English",
    },
    hero: {
      eyebrow: "独立 companion app · 面向 Multica 工作流",
      title: "在 Android 上处理 Issue。",
      subtitle: "查看任务上下文、进入评论区、草拟回复，把核心 workspace 闭环带到手机上。",
      download: "下载 APK",
      github: "查看 GitHub",
      multica: "访问 Multica",
      typing: "已在 Android 上验证，看起来没问题。",
    },
    relationship: {
      kicker: "与 Multica 的关系",
      title: "礼貌、清楚、保持独立。",
      body:
        "Multi-Casual Android 是面向 Multica 用户的独立 companion app。它为了兼容现有工作流而连接 Multica 服务 API，但并不由 Multica 项目维护者维护、背书或发布，也不应该被表述为上游项目推出的 Multica Android App。",
    },
    featuresKicker: "覆盖能力",
    features: [
      {
        title: "Issue 工作流",
        body: "查看 Inbox 条目、打开 Issue 详情、阅读评论，在离开桌面时也能保留任务上下文。",
      },
      {
        title: "手机端回复",
        body: "进入评论输入流程，草拟回复，继续原本会在 Web 端处理的同一条讨论。",
      },
      {
        title: "兼容云服务流程",
        body: "Android App 是 Multica 后端服务的 companion；不内置服务端，也不为普通用户提供运行时后端切换。",
      },
    ],
    demo: {
      kicker: "真实 Android 流程",
      title: "打开 Issue，阅读上下文，然后草拟评论。",
      body: "演示素材来自真实项目里的模拟器截图；交互部分用安全的叠加动画表现，不需要真的往公开项目里提交测试评论。",
    },
    video: {
      kicker: "宣传视频",
      title: "快速看一遍 Android 端 Issue 流程。",
      body:
        "这段 walkthrough 使用真实模拟器项目截图，展示 Issue 上下文、逐字输入感的评论草稿，以及一条轻快的 funk style 背景乐。",
    },
    download: {
      kicker: "GitHub APK",
      title: "从 GitHub Releases 安装。",
      package: "包名",
      api: "服务 API",
      sha: "APK SHA256",
      latest: "最新 Release",
      source: "源码仓库",
    },
    trustKicker: "信任与隐私",
    trustItems: [
      "这是独立 companion app，不声明拥有 Multica 项目，也不暗示维护者背书。",
      "作为独立 Android App 发布给 Multica 用户，同时明确署名和产品边界。",
      "公开 Android 版本里的 analytics 需要用户同意，并且仅限 app_open 事件。",
      "不会有意上报聊天内容、文件内容、邮箱、token 或精确位置。",
    ],
    footer: {
      tagline: "面向 Multica 用户的独立 Android companion app，通过 GitHub 分发 APK。",
      multica: "Multica 网站",
      github: "GitHub",
      download: "下载 APK",
    },
  },
} satisfies Record<Locale, unknown>;

function fade(reducedMotion: boolean | null, delay = 0): MotionProps {
  if (reducedMotion) {
    return {};
  }
  return {
    initial: { opacity: 0, y: 24 },
    animate: { opacity: 1, y: 0 },
    transition: { duration: 0.7, delay, ease: "easeOut" },
  };
}

function reveal(reducedMotion: boolean | null): MotionProps {
  if (reducedMotion) {
    return {};
  }
  return {
    initial: { opacity: 0, y: 28 },
    whileInView: { opacity: 1, y: 0 },
    viewport: { once: true, margin: "-80px" },
    transition: { duration: 0.7, ease: "easeOut" },
  };
}

function PhoneMockup({ typing }: { typing: string }) {
  const reducedMotion = useReducedMotion();
  return (
    <motion.div
      className="phone-wrap"
      animate={reducedMotion ? {} : { y: [0, -10, 0], rotate: [-1.5, -0.6, -1.5] }}
      transition={reducedMotion ? {} : { duration: 7, repeat: Infinity, ease: "easeInOut" }}
    >
      <div className="phone-shell">
        <div className="speaker" />
        <div className="phone-screen">
          <div className="phone-screen-shot" aria-label="Multi-Casual Android issue detail screen" />
          <motion.div
            className="typing-card"
            initial={reducedMotion ? false : { opacity: 0, y: 14, scale: 0.98 }}
            animate={reducedMotion ? { opacity: 1 } : { opacity: 1, y: 0, scale: 1 }}
            transition={{ delay: 1.05, duration: 0.55 }}
          >
            <span className="typing-dot" />
            {typing}
          </motion.div>
        </div>
      </div>
      <div className="tap-ring" aria-hidden="true" />
    </motion.div>
  );
}

export default function App() {
  const reducedMotion = useReducedMotion();
  const [locale, setLocale] = useState<Locale>("en");
  const t = copy[locale];

  return (
    <main lang={locale === "zh" ? "zh-CN" : "en"}>
      <section className="hero section-pad">
        <div className="hero-bg" />
        <nav className="topbar" aria-label="Primary navigation">
          <a className="brand" href="#top" aria-label="Multi-Casual Android home">
            <span className="brand-mark">M</span>
            <span>{t.nav.brand}</span>
          </a>
          <div className="nav-links">
            <a href="#download">{t.nav.download}</a>
            <a href="#trust">{t.nav.trust}</a>
            <a href="#relationship">{t.nav.relationship}</a>
            <button className="language-toggle" type="button" onClick={() => setLocale(locale === "en" ? "zh" : "en")}>
              {t.nav.language}
            </button>
          </div>
        </nav>

        <div id="top" className="hero-grid">
          <div className="hero-copy">
            <motion.p className="eyebrow" {...fade(reducedMotion, 0.05)}>
              {t.hero.eyebrow}
            </motion.p>
            <motion.h1 {...fade(reducedMotion, 0.14)}>{t.hero.title}</motion.h1>
            <motion.p className="hero-subtitle" {...fade(reducedMotion, 0.24)}>
              {t.hero.subtitle}
            </motion.p>
            <motion.div className="cta-row" {...fade(reducedMotion, 0.34)}>
              <motion.a className="button primary" href={release.releasesUrl} whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}>
                {t.hero.download}
              </motion.a>
              <motion.a className="button secondary" href={release.githubUrl} whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}>
                {t.hero.github}
              </motion.a>
              <motion.a className="button ghost" href={release.multicaUrl} whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}>
                {t.hero.multica}
              </motion.a>
            </motion.div>
          </div>
          <motion.div {...fade(reducedMotion, 0.18)}>
            <PhoneMockup typing={t.hero.typing} />
          </motion.div>
        </div>
      </section>

      <motion.section id="relationship" className="relationship section-pad" {...reveal(reducedMotion)}>
        <div className="section-kicker">{t.relationship.kicker}</div>
        <div className="two-col">
          <h2>{t.relationship.title}</h2>
          <p>{t.relationship.body}</p>
        </div>
      </motion.section>

      <section className="features section-pad">
        <div className="section-kicker">{t.featuresKicker}</div>
        <div className="feature-grid">
          {t.features.map((feature, index) => (
            <motion.article key={feature.title} className="feature-card" {...reveal(reducedMotion)} transition={{ duration: 0.7, delay: index * 0.08 }}>
              <span>{String(index + 1).padStart(2, "0")}</span>
              <h3>{feature.title}</h3>
              <p>{feature.body}</p>
            </motion.article>
          ))}
        </div>
      </section>

      <motion.section className="demo section-pad" {...reveal(reducedMotion)}>
        <div className="section-kicker">{t.demo.kicker}</div>
        <div className="demo-grid">
          <div>
            <h2>{t.demo.title}</h2>
            <p>{t.demo.body}</p>
          </div>
          <div className="screenshot-stack" aria-label="Issue comment flow screenshots">
            <img className="shot back" src={issueDetail} alt="Issue detail screenshot" />
            <img className="shot front" src={commentFocused} alt="Focused issue comment input screenshot" />
          </div>
        </div>
      </motion.section>

      <motion.section className="video-demo section-pad" {...reveal(reducedMotion)}>
        <div className="section-kicker">{t.video.kicker}</div>
        <div className="video-copy">
          <h2>{t.video.title}</h2>
          <p>{t.video.body}</p>
        </div>
        <video className="promo-video" src={promoVideo} controls playsInline preload="metadata" poster={commentFocused} />
      </motion.section>

      <section id="download" className="download section-pad">
        <motion.div className="release-card" {...reveal(reducedMotion)}>
          <div className="section-kicker">{t.download.kicker}</div>
          <h2>{t.download.title}</h2>
          <div className="release-grid">
            <div>
              <span>{t.download.package}</span>
              <code>{release.packageName}</code>
            </div>
            <div>
              <span>{t.download.api}</span>
              <code>{release.apiUrl}</code>
            </div>
            <div className="hash-cell">
              <span>{t.download.sha}</span>
              <code>{release.sha256}</code>
            </div>
          </div>
          <pre><code>{`adb install -r Multi-Casual-Android-v0.1.1.apk`}</code></pre>
          <div className="cta-row compact">
            <a className="button primary" href={release.releasesUrl}>{t.download.latest}</a>
            <a className="button secondary" href={release.githubUrl}>{t.download.source}</a>
          </div>
        </motion.div>
      </section>

      <section id="trust" className="trust section-pad">
        <div className="section-kicker">{t.trustKicker}</div>
        <div className="trust-grid">
          {t.trustItems.map((item) => (
            <motion.div key={item} className="trust-item" {...reveal(reducedMotion)}>
              <span className="check">✓</span>
              <p>{item}</p>
            </motion.div>
          ))}
        </div>
      </section>

      <footer className="footer section-pad">
        <div>
          <strong>Multi-Casual Android</strong>
          <p>{t.footer.tagline}</p>
        </div>
        <div className="footer-links">
          <a href={release.multicaUrl}>{t.footer.multica}</a>
          <a href={release.githubUrl}>{t.footer.github}</a>
          <a href={release.releasesUrl}>{t.footer.download}</a>
        </div>
      </footer>
    </main>
  );
}
