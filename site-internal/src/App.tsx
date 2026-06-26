import logoImg from "./assets/logo.png";
import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { motion, AnimatePresence, useMotionValue, useTransform, useReducedMotion } from "motion/react";
import inboxImg from "./assets/screenshots/01-inbox.jpg";
import issuesListImg from "./assets/screenshots/02-issues-list.jpg";
import issuesBoardImg from "./assets/screenshots/03-issues-board.jpg";
import issueDetailImg from "./assets/screenshots/04-issue-detail.jpg";
import issueSubImg from "./assets/screenshots/05-issue-detail-subissues.jpg";
import commentImg from "./assets/screenshots/06-comment-composer.jpg";
import mentionImg from "./assets/screenshots/07-mention-picker.jpg";
import projectsImg from "./assets/screenshots/08-projects-list.jpg";
import projectDetailImg from "./assets/screenshots/09-project-detail.jpg";
import agentsImg from "./assets/screenshots/10-agents-list.jpg";
import runtimesImg from "./assets/screenshots/11-runtimes-list.jpg";
import skillsImg from "./assets/screenshots/12-skills-list.jpg";
import skillDetailImg from "./assets/screenshots/13-skill-detail.jpg";
import settingsImg from "./assets/screenshots/14-settings.jpg";
import promoVideo from "./assets/video/multica-android-promo-draft.mp4";

const release = {
  packageName: "ai.multica.app.xiaomi",
  sha256: "13e004472d33e66aa40aa7ebd58f75e22cfde8cce3a153729481f1be4d2d1414",
  gitlabUrl: "https://git.n.xiaomi.com/zhaoxisheng/multica-xiaomi-android",
  releasesUrl: "https://github.com/park0er/multica-xiaomi-apk/releases/download/v0.1.5-xiaomi/Multica-Xiaomi-v0.1.5.apk",
  iosUrl: "https://github.com/park0er/multi-casual-ios-app",
  multicaUrl: "https://multica.ai",
  apiUrl: "http://staging-multica.ad.xiaomi.srv",
};

type Locale = "en" | "zh";

const copy: Record<Locale, {
  nav: { brand: string; download: string; trust: string; language: string };
  hero: { eyebrow: string; title: string; subtitle: string; download: string; gitlab: string; multica: string; ios: string };
  // relationship section intentionally removed for the internal build
  journey: { kicker: string; title: string; subtitle: string; cta: string; steps: Array<{ title: string; subtitle: string }> };
  workspace: { kicker: string; title: string; subtitle: string; items: Array<{ title: string; subtitle: string }> };
  video: { kicker: string; title: string; body: string };
  download: { kicker: string; title: string; package: string; api: string; sha: string; latest: string; source: string; note: string };
  trust: { kicker: string; items: string[] };
  footer: { tagline: string; multica: string; gitlab: string; ios: string; download: string };
}> = {
  en: {
    nav: { brand: "Multica Xiaomi", download: "Download", trust: "Scope", language: "中文" },
    hero: {
      eyebrow: "Internal Xiaomi build · Company-only distribution",
      title: "Multica Xiaomi for the Xiaomi workspace.",
      subtitle: "An Android app independently built from scratch by Zhao Xisheng in Xiaomi's Commercial Platform Department. Multica has not provided an official Android app to date; this internal build gives Xiaomi users a native path today.",
      download: "Download APK",
      gitlab: "GitLab",
      multica: "Visit Multica",
      ios: "iOS App",
    },
    journey: {
      kicker: "Interactive walkthrough",
      title: "Try the flow.",
      subtitle: "Click each step or scroll to see the app in action.",
      cta: "Start the walkthrough",
      steps: [
        { title: "Inbox notifications", subtitle: "Stay on top of activity — new issues, comments, and agent progress show up as they happen." },
        { title: "Browse issues", subtitle: "See all tasks in a clean list view, ready for triage. Filter, search, and tap into any issue." },
        { title: "Switch to board view", subtitle: "Tap the view toggle to switch to a kanban board that shows status flow at a glance." },
        { title: "Open issue detail", subtitle: "Tap an issue card to see the full detail: title, status, labels, assignee, and body text." },
        { title: "Explore context", subtitle: "Context guide points zoom into focus areas — title and status, body content, then subtasks and comments — and return to the full view between each." },
        { title: "Scroll for more", subtitle: "Keep scrolling to see subtasks, subscribers, and the comment thread — all the structured context stays on one page." },
        { title: "Draft a comment", subtitle: "Tap the comment box. The keyboard appears and a comment types out character by character." },
        { title: "Mention anyone", subtitle: "Type @ and a picker appears with people, agents, and squads. Choose who to notify." },
        { title: "Workspace overview", subtitle: "Switch between Projects, Agents, Runtimes, Skills, and Settings with workspace chips." },
      ],
    },
    workspace: {
      kicker: "Workspace assets",
      title: "Projects, agents, and everything in between.",
      subtitle: "Switch between workspace views to see the full picture.",
      items: [
        { title: "Projects", subtitle: "Browse projects with progress tracking" },
        { title: "Project detail", subtitle: "Task stats, status fields, and resources" },
        { title: "Agents", subtitle: "View agent status and recent activity" },
        { title: "Runtimes", subtitle: "Check runtime availability and versions" },
        { title: "Skills", subtitle: "Browse the workspace agent skill library" },
        { title: "Skill detail", subtitle: "Inspect skill structure and documentation" },
        { title: "Settings", subtitle: "Account, workspace, and preference controls" },
      ],
    },
    video: { kicker: "Product demo", title: "See it in 30 seconds.", body: "A fast walkthrough of inbox, issues, context, comments, mentions, and workspace views." },
    download: { kicker: "Internal build", title: "Download the Xiaomi internal APK from GitHub Pages.", package: "Package", api: "Service API", sha: "APK SHA256", latest: "Download APK", source: "View source", note: "Internal distribution only — not for external release." },
    trust: {
      kicker: "Internal build scope",
      items: [
        "Designed and implemented from scratch by Zhao Xisheng of Xiaomi's Commercial Platform Department, with no upstream Android app to inherit from.",
        "Multica has not provided an official Android app to date; this project is an independent Xiaomi internal Android client for Multica workflows.",
        "Company-internal distribution only. APK download is served from a separate GitHub release artifact repository after EdgeOne proved unreliable.",
        "Connects to the Xiaomi Multica service endpoint, with analytics routed through OneTrack and disabled for Umeng / PostHog.",
        "Source is kept in the private company GitLab project; the signed APK is distributed from GitHub Pages.",
      ],
    },
    footer: { tagline: "Built from scratch by Zhao Xisheng for the Xiaomi workspace. APK served via GitHub Pages.", multica: "Multica website", gitlab: "GitLab", ios: "iOS App", download: "Download APK" },
  },
  zh: {
    nav: { brand: "Multica Xiaomi", download: "下载", trust: "分发说明", language: "English" },
    hero: {
      eyebrow: "小米内部构建 · 仅限公司内部分发",
      title: "面向小米工作区的 Multica Xiaomi 内部版。",
      subtitle: "由小米商业平台部赵锡盛完全从头自研的 Android App。Multica 官方到目前为止还没有提供 Android App；这套内部构建先为小米用户补上原生移动端入口。",
      download: "下载 APK",
      gitlab: "GitLab",
      multica: "访问 Multica",
      ios: "iOS App",
    },
    journey: {
      kicker: "交互式演示",
      title: "试试这个流程。",
      subtitle: "点击每个步骤，或滚动页面查看 app 实际操作。",
      cta: "开始演示",
      steps: [
        { title: "Inbox 通知", subtitle: "新 issues、评论和 agent 进展会实时出现在收件箱，不错过任何动态。" },
        { title: "浏览 Issues", subtitle: "在列表视图中查看所有任务，快速筛选和进入任意 issue。" },
        { title: "切换看板视图", subtitle: "点击视图切换按钮，看板视图让你一眼看到任务状态流。" },
        { title: "打开 Issue 详情", subtitle: "点击 issue 卡片查看完整详情：标题、状态、标签、负责人和正文。" },
        { title: "探索上下文", subtitle: "Context 引导点依次放大标题与状态、正文内容、子任务与评论，然后缩回全貌。" },
        { title: "滚动查看更多", subtitle: "继续下滑查看子任务、订阅者和评论线程 —— 结构化上下文都在同一页。" },
        { title: "草拟评论", subtitle: "点击评论框，键盘出现，评论逐字输入。" },
        { title: "@ 提及任何人", subtitle: "输入 @ 弹出选择器，可选人、agent 或小队。" },
        { title: "Workspace 总览", subtitle: "通过 workspace chips 切换查看 Projects、Agents、Runtimes、Skills 和 Settings。" },
      ],
    },
    workspace: {
      kicker: "Workspace 资产",
      title: "项目、agents，以及它们之间的一切。",
      subtitle: "切换 workspace 视图，掌握全貌。",
      items: [
        { title: "Projects", subtitle: "浏览项目和进度追踪" },
        { title: "项目详情", subtitle: "任务统计、状态字段和资源入口" },
        { title: "Agents", subtitle: "查看 agent 状态和最近活动" },
        { title: "Runtimes", subtitle: "检查运行时可用性和版本" },
        { title: "Skills", subtitle: "浏览 workspace agent 技能库" },
        { title: "技能详情", subtitle: "查看技能结构和文档" },
        { title: "Settings", subtitle: "账号、工作区和偏好控制" },
      ],
    },
    video: { kicker: "产品演示", title: "30 秒看完核心流程。", body: "快速演示 inbox 通知、issues 看板、上下文探索、评论回复、@ 提及和 workspace 视图切换。" },
    download: { kicker: "内部构建", title: "从 GitHub Pages 下载小米内部 APK。", package: "Package", api: "Service API", sha: "APK SHA256", latest: "下载 APK", source: "查看源码", note: "仅供公司内部分发，不对外发布。" },
    trust: {
      kicker: "内部构建说明",
      items: [
        "由小米商业平台部赵锡盛完全从头设计和实现，不是基于 Multica 官方 Android 客户端改造。",
        "Multica 官方到目前为止还没有提供 Android App；本项目是面向小米内部 Multica 工作流的独立 Android 客户端。",
        "仅供公司内部分发；由于腾讯云 EdgeOne 访问不稳定，APK 下载改由单独的 GitHub Release 产物仓库托管提供。",
        "连接小米 Multica 服务端点，统计走 OneTrack，Umeng / PostHog 已关闭。",
        "源码保存在私有公司 GitLab 项目中；签名 APK 从 GitHub Pages 分发。",
      ],
    },
    footer: { tagline: "商业平台部赵锡盛从头自研，APK 由 GitHub Pages 分发。", multica: "Multica 官网", gitlab: "GitLab", ios: "iOS App", download: "下载 APK" },
  },
};

const JOURNEY_STEPS = 9;
const TYPING_TEXT = "Looks good. I'll ask the agent to check the failing case and post a follow-up.";
const MENTION_ITEMS = [
  { name: "codex", color: "#1647ff" },
  { name: "Opus", color: "#7c3aed" },
  { name: "RollieOC", color: "#059669" },
];

const WORKSPACE_SCREENSHOTS = [projectsImg, projectDetailImg, agentsImg, runtimesImg, skillsImg, skillDetailImg, settingsImg];

const fade = (reduced: boolean, delay = 0) => ({
  initial: reduced ? {} : { opacity: 0, y: 18 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true, margin: "-60px" },
  transition: { duration: 0.65, delay, ease: [0.25, 0.1, 0.25, 1] as const },
});

const reveal = (reduced: boolean) => ({
  initial: reduced ? {} : { opacity: 0, y: 20 },
  whileInView: { opacity: 1, y: 0 },
  viewport: { once: true, margin: "-80px" },
  transition: { duration: 0.7, ease: [0.25, 0.1, 0.25, 1] as const },
});

/* ── PhoneDemo ── */
function PhoneDemo({
  src,
  reduced,
  zoomScale = 1,
  zoomX = 0,
  zoomY = 0,
  children,
}: {
  src: string;
  reduced: boolean;
  zoomScale?: number;
  zoomX?: number;
  zoomY?: number;
  children?: React.ReactNode;
}) {
  return (
    <div className="phone-shell-compact">
      <div className="speaker" />
      <div className="phone-screen-compact">
        <AnimatePresence mode="wait">
          <motion.img
            key={src}
            src={src}
            alt="App screenshot"
            initial={reduced ? {} : { opacity: 0 }}
            animate={{
              opacity: 1,
              scale: zoomScale,
              x: zoomX,
              y: zoomY,
            }}
            exit={reduced ? {} : { opacity: 0 }}
            transition={{ duration: reduced ? 0.1 : 0.4, ease: [0.25, 0.1, 0.25, 1] as const }}
          />
        </AnimatePresence>
        {children}
      </div>
    </div>
  );
}

/* ── Typewriter effect ── */
function TypewriterEffect({ active, text, reduced }: { active: boolean; text: string; reduced: boolean }) {
  const charCount = useMotionValue(0);
  const chars = useMemo(() => text.split(""), [text]);
  const charRefs = useRef<(HTMLSpanElement | null)[]>([]);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // Imperatively update char opacity from motion value (no hooks in loops)
  useEffect(() => {
    const unsubscribe = charCount.on("change", (v) => {
      const count = Math.round(v);
      charRefs.current.forEach((el, i) => {
        if (el) el.style.opacity = i < count ? "1" : "0";
      });
    });
    return unsubscribe;
  }, [charCount]);

  useEffect(() => {
    if (intervalRef.current) clearInterval(intervalRef.current);
    if (!active) { charCount.set(0); charRefs.current.forEach((el) => { if (el) el.style.opacity = "0"; }); return; }
    if (reduced) {
      charCount.set(chars.length);
      charRefs.current.forEach((el, i) => { if (el) el.style.opacity = i < chars.length ? "1" : "0"; });
      return;
    }
    let i = 0;
    const interval = setInterval(() => {
      i++;
      charCount.set(i);
      if (i >= chars.length) clearInterval(interval);
    }, 42);
    intervalRef.current = interval;
    return () => clearInterval(interval);
  }, [active, reduced, chars.length]);

  return (
    <motion.div
      className="typing-line"
      animate={active ? { opacity: 1 } : { opacity: 0 }}
      transition={{ duration: 0.3 }}
    >
      {chars.map((char, i) => (
        <span
          key={i}
          ref={(el) => { charRefs.current[i] = el; }}
          style={{ opacity: 0 }}
        >
          {char === " " ? "\u00A0" : char}
        </span>
      ))}
      <motion.span
        className="typing-cursor"
        animate={active ? { opacity: [1, 0.18] } : { opacity: 0 }}
        transition={active ? { duration: 0.64, repeat: Infinity, repeatType: "reverse" } : {}}
      />
    </motion.div>
  );
}

/* ── PhoneMockup (hero) ── */
function PhoneMockup({ reduced }: { reduced: boolean }) {
  return (
    <div className="hero-phone-stack">
      <motion.img
        src={inboxImg} alt="Inbox preview"
        initial={reduced ? {} : { opacity: 0, x: 60, rotate: -14 }}
        whileInView={{ opacity: 0.72, x: 0, rotate: -8 }}
        viewport={{ once: true }}
        transition={{ duration: 0.85, delay: 0.2, ease: [0.25, 0.1, 0.25, 1] as const }}
      />
      <motion.img
        src={issuesListImg} alt="Issues list preview"
        initial={reduced ? {} : { opacity: 0, x: 80, rotate: 8 }}
        whileInView={{ opacity: 1, x: 0, rotate: 4 }}
        viewport={{ once: true }}
        transition={{ duration: 0.85, delay: 0.3, ease: [0.25, 0.1, 0.25, 1] as const }}
      />
      <motion.img
        src={issueDetailImg} alt="Issue detail preview"
        initial={reduced ? {} : { opacity: 0, x: 60, rotate: 14 }}
        whileInView={{ opacity: 0.86, x: 0, rotate: 10 }}
        viewport={{ once: true }}
        transition={{ duration: 0.85, delay: 0.4, ease: [0.25, 0.1, 0.25, 1] as const }}
      />
    </div>
  );
}

/* ── Main App ── */
export default function App() {
  const reduced = useReducedMotion() ?? false;
  const [lang, setLang] = useState<Locale>("en");
  const [step, setStep] = useState(0);
  const [activeWs, setActiveWs] = useState(0);
  const [hasInteracted, setHasInteracted] = useState(false);
  const completedRef = useRef<Set<number>>(new Set());
  const journeyRef = useRef<HTMLDivElement>(null);
  const hasAutoPlayedRef = useRef(false);
  const t = copy[lang];

  // Auto-play journey on first scroll into view
  useEffect(() => {
    if (hasInteracted || hasAutoPlayedRef.current) return;
    const el = journeyRef.current;
    if (!el) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && entry.intersectionRatio > 0.3 && !hasAutoPlayedRef.current) {
          hasAutoPlayedRef.current = true;
          let currentStep = 0;
          const advance = () => {
            currentStep++;
            if (currentStep < JOURNEY_STEPS) {
              setStep(currentStep);
              setTimeout(advance, 2200);
            }
          };
          setTimeout(advance, 2200);
        }
      },
      { threshold: 0.3 }
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, [hasInteracted]);

  const handleStepClick = useCallback((index: number) => {
    setHasInteracted(true);
    setStep(index);
  }, []);

  const handleWorkspaceClick = useCallback((index: number) => {
    setActiveWs(index);
  }, []);

  // Phone screen source based on journey step
  const phoneScreenSrc = useMemo(() => {
    if (step <= 1) return step === 0 ? inboxImg : issuesListImg;
    if (step === 2) return issuesBoardImg;
    if (step <= 5) return step === 3 ? issueDetailImg : issueSubImg;
    if (step === 6) return commentImg;
    if (step === 7) return mentionImg;
    return projectsImg;
  }, [step]);

  // Focus zoom transform for step 4 (context explore)
  const focusZones = [
    { scale: 1.6, x: 0, y: -120 },    // title/status
    { scale: 1.45, x: 0, y: -40 },    // body
    { scale: 1.5, x: 0, y: 80 },      // subtasks/comments
  ];
  const focusIdx = step === 4 ? 0 : -1; // simple: show first zone; cycle handled by auto-play
  const zoom = step === 4 ? focusZones[focusIdx] || focusZones[0] : { scale: 1, x: 0, y: 0 };

  return (
    <main lang={lang === "zh" ? "zh-CN" : "en"}>
      {/* Hero */}
      <section className="hero">
        <div className="hero-bg" />
        <motion.nav className="topbar" {...fade(reduced, 0)}>
          <div className="brand">
            <img src={logoImg} alt="Multica Xiaomi" className="brand-mark" />
            <span>{t.nav.brand}</span>
          </div>
          <div className="nav-links">
            <a href="#download">{t.nav.download}</a>
            <a href="#trust">{t.nav.trust}</a>
            <button className="language-toggle" onClick={() => setLang((l) => (l === "en" ? "zh" : "en"))}>
              {t.nav.language}
            </button>
          </div>
        </motion.nav>
        <div className="hero-grid">
          <div>
            <motion.p className="eyebrow" {...fade(reduced, 0.04)}>{t.hero.eyebrow}</motion.p>
            <motion.h1 {...fade(reduced, 0.14)}>{t.hero.title}</motion.h1>
            <motion.p className="hero-subtitle" {...fade(reduced, 0.24)}>{t.hero.subtitle}</motion.p>
            <motion.div className="cta-row" {...fade(reduced, 0.34)}>
              <motion.a href="#download" className="pill pill-android" whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}><span className="android-icon"></span>{t.hero.download}</motion.a>
              <motion.a className="button secondary" href={release.gitlabUrl} whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}><svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" style={{verticalAlign: "middle", marginRight: 6}}><path d="M22.65 14.39L12 22.13 1.35 14.39a.84.84 0 0 1-.3-.94l1.22-3.78 2.44-7.51A.42.42 0 0 1 4.82 2a.43.43 0 0 1 .58 0 .42.42 0 0 1 .11.18l2.44 7.49h8.1l2.44-7.51A.42.42 0 0 1 18.6 2a.43.43 0 0 1 .58 0 .42.42 0 0 1 .11.18l2.44 7.51L23 13.45a.84.84 0 0 1-.35.94z"/></svg>{t.hero.gitlab}</motion.a>
              <motion.a className="button ghost" href={release.multicaUrl} whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}>{t.hero.multica}</motion.a>
              <motion.a href={release.iosUrl} className="pill pill-ios" whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}><span className="apple-icon"></span>{t.hero.ios}</motion.a>
            </motion.div>
          </div>
          <PhoneMockup reduced={reduced} />
        </div>
      </section>

      {/* (Relationship section removed for the internal build.) */}

      {/* Interactive Product Journey */}
      <section ref={journeyRef} className="journey section-pad">
        <motion.div className="journey-header" {...reveal(reduced)}>
          <div className="section-kicker">{t.journey.kicker}</div>
          <h2>{t.journey.title}</h2>
          <p>{t.journey.subtitle}</p>
        </motion.div>

        <div className="journey-layout">
          <div className="journey-steps">
            {t.journey.steps.map((s, i) => (
              <motion.div
                key={i}
                className={`journey-step ${step === i ? "journey-step-active" : ""} ${i < step ? "journey-step-completed" : ""}`}
                onClick={() => handleStepClick(i)}
                initial={reduced ? {} : { opacity: 0, x: -16 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: i * 0.06 }}
              >
                <div className="journey-step-number">{i < step ? "✓" : i + 1}</div>
                <div className="journey-step-content">
                  <h3>{s.title}</h3>
                  <p>{s.subtitle}</p>
                </div>
              </motion.div>
            ))}
          </div>

          <div className="journey-phone-area">
            <div className="journey-phone-container">
              <PhoneDemo
                src={phoneScreenSrc}
                reduced={reduced}
                zoomScale={step === 4 ? zoom.scale : 1}
                zoomX={step === 4 ? zoom.x : 0}
                zoomY={step === 4 ? zoom.y : 0}
              >
                {/* Hand + tap ripple for interactive steps */}
                {step >= 1 && step <= 3 && (
                  <>
                    <motion.div
                      className="journey-hand"
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                      exit={{ opacity: 0 }}
                      style={{ top: step === 1 ? "16%" : step === 2 ? "8%" : "35%", right: step === 2 ? "18%" : "22%" }}
                    >👆</motion.div>
                    <motion.div
                      className="journey-tap-ripple"
                      animate={{ scale: [1, 1.3, 1], opacity: [0.8, 0.3, 0.8] }}
                      transition={{ duration: 1.2, repeat: Infinity }}
                      style={{ top: step === 1 ? "18%" : step === 2 ? "10%" : "37%", right: step === 2 ? "14%" : "18%" }}
                    />
                  </>
                )}

                {/* Typing effect for step 6 */}
                {step === 6 && <TypewriterEffect active text={TYPING_TEXT} reduced={reduced} />}

                {/* Mention picker overlay for step 7 */}
                <AnimatePresence>
                  {step === 7 && (
                    <motion.div
                      className="mention-overlay"
                      initial={{ y: 60, opacity: 0 }}
                      animate={{ y: 0, opacity: 1 }}
                      exit={{ y: 60, opacity: 0 }}
                      transition={{ duration: 0.35, ease: [0.25, 0.1, 0.25, 1] as const }}
                    >
                      <div className="mention-search">
                        <span>@</span> cod
                      </div>
                      <div className="mention-list">
                        {MENTION_ITEMS.map((m, i) => (
                          <motion.div
                            key={m.name}
                            className={`mention-item ${i === 0 ? "mention-item-active" : ""}`}
                            initial={{ opacity: 0, x: -12 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: 0.15 + i * 0.1 }}
                          >
                            <div className="mention-avatar" style={{ background: m.color }}>{m.name[0].toUpperCase()}</div>
                            <div>
                              <div className="mention-name">{m.name}</div>
                              <div className="mention-role">{i === 0 ? "agent" : i === 1 ? "agent" : "squad"}</div>
                            </div>
                          </motion.div>
                        ))}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>

                {/* Workspace chips overlay for step 8 */}
                {step === 8 && (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    style={{ position: "absolute", bottom: 8, left: 8, right: 8, zIndex: 6, display: "flex", gap: 4, justifyContent: "center", flexWrap: "wrap" }}
                  >
                    {["Projects", "Agents", "Runtimes", "Skills", "Settings"].map((label, i) => (
                      <motion.div
                        key={label}
                        className="workspace-chip"
                        style={{ fontSize: 10, padding: "4px 8px", borderRadius: 999 }}
                        initial={{ scale: 0 }}
                        animate={{ scale: 1 }}
                        transition={{ delay: 0.1 + i * 0.08 }}
                      >
                        {label}
                      </motion.div>
                    ))}
                  </motion.div>
                )}
              </PhoneDemo>
            </div>
          </div>
        </div>
      </section>

      {/* Workspace Assets Grid */}
      <motion.section className="workspace-section section-pad" {...reveal(reduced)}>
        <div className="workspace-header">
          <div className="section-kicker">{t.workspace.kicker}</div>
          <h2>{t.workspace.title}</h2>
          <p>{t.workspace.subtitle}</p>
        </div>

        <div className="workspace-chips">
          {t.workspace.items.map((item, i) => (
            <button
              key={i}
              className={`workspace-chip ${activeWs === i ? "workspace-chip-active" : ""}`}
              onClick={() => handleWorkspaceClick(i)}
            >
              {item.title}
            </button>
          ))}
        </div>

        <div className="workspace-phone-wrap">
          <AnimatePresence mode="wait">
            <motion.div
              key={activeWs}
              initial={reduced ? {} : { opacity: 0, scale: 0.96 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={reduced ? {} : { opacity: 0, scale: 0.96 }}
              transition={{ duration: 0.35 }}
            >
              <PhoneDemo src={WORKSPACE_SCREENSHOTS[activeWs]} reduced={reduced} />
            </motion.div>
          </AnimatePresence>
        </div>

        <motion.div className="workspace-card-grid" {...reveal(reduced)}>
          {t.workspace.items.map((item, i) => (
            <motion.div
              key={i}
              className={`workspace-card ${activeWs === i ? "workspace-card-active" : ""}`}
              onClick={() => handleWorkspaceClick(i)}
              initial={reduced ? {} : { opacity: 0, y: 16 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: i * 0.06 }}
            >
              <img src={WORKSPACE_SCREENSHOTS[i]} alt={item.title} className="workspace-card-thumb" />
              <div className="workspace-card-label">
                <strong>{item.title}</strong>
                <span>{item.subtitle}</span>
              </div>
            </motion.div>
          ))}
        </motion.div>
      </motion.section>

      {/* Video Demo */}
      <motion.section className="video-demo section-pad" {...reveal(reduced)}>
        <div className="section-kicker">{t.video.kicker}</div>
        <div className="video-copy">
          <h2>{t.video.title}</h2>
          <p>{t.video.body}</p>
        </div>
        <video className="promo-video" src={promoVideo} controls playsInline preload="metadata" />
      </motion.section>

      {/* Download */}
      <section id="download" className="download section-pad">
        <motion.div className="release-card" {...reveal(reduced)}>
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
          <pre><code>{`adb install -r Multica-Xiaomi-v0.1.5.apk`}</code></pre>
          <div className="cta-row compact">
            <a href={release.releasesUrl} className="pill pill-android"><span className="android-icon"></span>{t.download.latest}</a>
            <a className="button secondary" href={release.gitlabUrl}>{t.download.source}</a>
          </div>
          <p className="star-hint">
            {lang === "en"
              ? <>APK downloads are served by a separate GitHub release artifact repository; source lives on the company <a href={release.gitlabUrl} target="_blank" rel="noopener">GitLab</a>.</>
              : <>APK 由单独的 GitHub Release 产物仓库分发；源码保存在公司 <a href={release.gitlabUrl} target="_blank" rel="noopener">GitLab</a>。</>
            }
          </p>
        </motion.div>
      </section>

      {/* Trust */}
      <section id="trust" className="trust section-pad">
        <div className="section-kicker">{t.trust.kicker}</div>
        <div className="trust-grid">
          {t.trust.items.map((item) => (
            <motion.div key={item} className="trust-item" {...reveal(reduced)}>
              <span className="check">✓</span>
              <p>{item}</p>
            </motion.div>
          ))}
        </div>
      </section>

      {/* Footer */}
      <footer className="footer section-pad">
        <div>
          <strong>Multica Xiaomi</strong>
          <p>{t.footer.tagline}</p>
        </div>
        <div className="footer-links">
          <a href={release.multicaUrl}>{t.footer.multica}</a>
          <a href={release.gitlabUrl}>{t.footer.gitlab}</a>
          <a href={release.releasesUrl} className="pill pill-android"><span className="android-icon"></span>{t.footer.download}</a>
          <a href={release.iosUrl} className="pill pill-ios"><span className="apple-icon"></span>{t.footer.ios}</a>
        </div>
      </footer>
    </main>
  );
}
