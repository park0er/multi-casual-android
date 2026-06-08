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
  packageName: "ai.multicasual.app",
  sha256: "0b34ab0d164403aec655ca9e829b3fce6a2849f4a83dc1f631336ff725299c42",
  githubUrl: "https://github.com/park0er/multi-casual-android",
  releasesUrl: "https://github.com/park0er/multi-casual-android/releases/latest",
  multicaUrl: "https://multica.ai",
  apiUrl: "https://api.multica.ai",
};

type Locale = "en" | "zh";

const copy: Record<Locale, {
  nav: { brand: string; download: string; trust: string; language: string };
  hero: { eyebrow: string; title: string; subtitle: string; download: string; github: string; multica: string };
  relationship: { kicker: string; title: string; body1: string; body2: string };
  journey: { kicker: string; title: string; subtitle: string; cta: string; steps: Array<{ title: string; subtitle: string }> };
  workspace: { kicker: string; title: string; subtitle: string; items: Array<{ title: string; subtitle: string }> };
  video: { kicker: string; title: string; body: string };
  download: { kicker: string; title: string; package: string; api: string; sha: string; latest: string; source: string; star: string };
  trust: { kicker: string; items: string[] };
  footer: { tagline: string; multica: string; github: string; download: string };
}> = {
  en: {
    nav: { brand: "Multi-Casual Android", download: "Download", trust: "Trust", language: "中文" },
    hero: {
      eyebrow: "Independent companion app · Built for Multica workflows",
      title: "Keep your Multica workspace moving from Android.",
      subtitle: "Check inbox updates, browse issues, read full context, reply in threads, and keep agent workspace assets close — an independent Android companion app for Multica users.",
      download: "Download APK",
      github: "GitHub",
      multica: "Visit Multica",
    },
    relationship: {
      kicker: "Relationship to Multica",
      title: "Respectful, explicit, and independent.",
      body1: "Multi-Casual Android is an independent companion app for Multica users. It connects to Multica service APIs for compatibility, but it is not maintained, endorsed, or published by the Multica project maintainers.",
      body2: "This project was originally explored as a contribution path with the Multica community. The Multica maintainers later indicated that their own app direction would be led upstream, so this Android client is maintained independently, following that boundary while staying respectful of the original project.",
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
    download: { kicker: "GitHub APK", title: "Install from GitHub Releases.", package: "Package", api: "Service API", sha: "APK SHA256", latest: "Latest release", source: "Source code", star: "Like the app? Give us a star on GitHub ⭐ — it means a lot to the maintainers." },
    trust: {
      kicker: "Trust and privacy",
      items: [
        "Community-maintained independent project; not affiliated with or endorsed by Multica maintainers.",
        "Published as its own Android app for Multica users, with explicit attribution and product boundaries.",
        "Open source under GPLv3 license — inspect, fork, or contribute.",
        "No chat content, file content, email address, tokens, or precise location are collected.",
      ],
    },
    footer: { tagline: "Independent Android companion app for Multica users. GitHub-distributed APK.", multica: "Multica website", github: "GitHub", download: "Download APK" },
  },
  zh: {
    nav: { brand: "Multi-Casual Android", download: "下载", trust: "信任说明", language: "English" },
    hero: {
      eyebrow: "独立 companion app · 面向 Multica 工作流",
      title: "在 Android 上跟进你的 Multica 工作现场。",
      subtitle: "查看通知、浏览 issues、阅读完整上下文、回复评论、@ 对方 —— 面向 Multica 用户的独立 Android companion app。",
      download: "下载 APK",
      github: "GitHub",
      multica: "访问 Multica",
    },
    relationship: {
      kicker: "与 Multica 的关系",
      title: "礼貌、清楚、保持独立。",
      body1: "Multi-Casual Android 是面向 Multica 用户的独立 companion app。它为了兼容现有工作流而连接 Multica 服务 API，但并不由 Multica 项目维护者维护、背书或发布。",
      body2: "这个项目最初是作为向 Multica 社区贡献的路径来探索的。由于 Multica maintainers 后续计划推进他们自己主导的 app 体系，因此这套 Android 代码体系没有直接并入上游。我们尊重 maintainers 的边界建议，将 Multi-Casual Android 作为独立 app 继续运营和维护。",
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
    download: { kicker: "GitHub APK", title: "从 GitHub Releases 安装。", package: "Package", api: "Service API", sha: "APK SHA256", latest: "最新版本", source: "源代码", star: "喜欢我们的 app？请在 GitHub 上给项目点个 Star ⭐，这是对我们作者最大的支持。" },
    trust: {
      kicker: "信任与隐私",
      items: [
        "社区维护的独立项目；与 Multica maintainers 无隶属关系，无背书。",
        "作为面向 Multica 用户的独立 Android app 发布，明确标注产品边界。",
        "GPLv3 开源许可证 —— 可以查看、fork 或贡献。",
        "不收集聊天内容、文件内容、邮箱、tokens 或精确位置信息。",
      ],
    },
    footer: { tagline: "面向 Multica 用户的独立 Android companion app。GitHub 分发 APK。", multica: "Multica 官网", github: "GitHub", download: "下载 APK" },
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
            <img src={logoImg} alt="Multi-Casual" className="brand-mark" />
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
              <motion.a className="button primary" href={release.releasesUrl} whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}>{t.hero.download}</motion.a>
              <motion.a className="button secondary" href={release.githubUrl} whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}>{t.hero.github}</motion.a>
              <motion.a className="button ghost" href={release.multicaUrl} whileHover={{ y: -2 }} whileTap={{ scale: 0.98 }}>{t.hero.multica}</motion.a>
            </motion.div>
          </div>
          <PhoneMockup reduced={reduced} />
        </div>
      </section>

      {/* Relationship */}
      <motion.section id="relationship" className="relationship section-pad" {...reveal(reduced)}>
        <div className="section-kicker">{t.relationship.kicker}</div>
        <div className="relationship-card">
          <h2>{t.relationship.title}</h2>
          <p>{t.relationship.body1}</p>
          <p>{t.relationship.body2}</p>
          <p>
            <a href="https://github.com/multica-ai/multica/issues/2285#issuecomment-4414513596" target="_blank" rel="noopener noreferrer">
              View the discussion on GitHub →
            </a>
          </p>
        </div>
      </motion.section>

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
          <pre><code>{`adb install -r Multi-Casual-Android-v0.1.1.apk`}</code></pre>
          <div className="cta-row compact">
            <a className="button primary" href={release.releasesUrl}>{t.download.latest}</a>
            <a className="button secondary" href={release.githubUrl}>{t.download.source}</a>
          </div>
          <p className="star-hint">{t.download.star}</p>
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
