package ai.multica.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public final class MainActivity extends Activity {
    private static final int BLUE = 0xFF2563EB;
    private static final int GREEN = 0xFF16A34A;
    private static final int RED = 0xFFDC2626;
    private static final int TEXT = 0xFF111827;
    private static final int MUTED = 0xFF6B7280;
    private static final int BORDER = 0xFFE5E7EB;
    private static final int SURFACE = 0xFFFFFFFF;
    private static final int APP_BG = 0xFFF8FAFC;
    private static final int SOFT_BLUE = 0xFFEFF6FF;
    private static final int SOFT_GRAY = 0xFFF3F4F6;

    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler main = new Handler(Looper.getMainLooper());
    private AuthStore authStore;
    private ApiClient api;
    private Models.User currentUser;
    private Models.Workspace currentWorkspace;
    private List<Models.Workspace> workspaces = new ArrayList<>();
    private List<Models.Project> projectCache = new ArrayList<>();
    private List<Models.Member> memberCache = new ArrayList<>();
    private List<Models.Agent> agentCache = new ArrayList<>();
    private List<Models.Squad> squadCache = new ArrayList<>();
    private List<Models.Runtime> runtimeCache = new ArrayList<>();
    private List<Models.Skill> skillCache = new ArrayList<>();
    private List<Models.Autopilot> autopilotCache = new ArrayList<>();
    private List<Models.IssueLabel> labelCache = new ArrayList<>();
    private List<Models.ChatSession> chatSessionCache = new ArrayList<>();
    private List<Models.InboxItem> demoInbox = new ArrayList<>();
    private List<Models.Issue> demoIssues = new ArrayList<>();
    private final Map<String, List<Models.Comment>> demoCommentsByIssueId = new HashMap<>();
    private final Map<String, List<Models.AgentTask>> demoRunsByIssueId = new HashMap<>();
    private final Map<String, List<Models.ChatMessage>> demoChatMessagesBySessionId = new HashMap<>();
    private FrameLayout content;
    private LinearLayout shell;
    private String tab = "inbox";
    private boolean boardMode = false;
    private boolean issuesDescending = true;
    private boolean zh = false;
    private boolean demoMode = false;

    interface Success<T> { void accept(T value); }
    interface Failure { void accept(Exception error); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authStore = new AuthStore(this);
        api = new ApiClient(authStore);
        zh = authStore.isChinese();
        demoMode = getIntent() != null && getIntent().getBooleanExtra("demo", false);
        if (getIntent() != null && getIntent().getData() != null) handleDeepLink(getIntent().getData());
        if (demoMode) {
            setupDemoData();
            showShell();
            return;
        }
        if (authStore.token() == null) showLogin();
        else restoreSession();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getData() != null) handleDeepLink(intent.getData());
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void restoreSession() {
        showLoading(t("loading"));
        run(() -> {
            SessionData data = new SessionData();
            data.user = api.me();
            data.workspaces = api.workspaces();
            return data;
        }, data -> {
            currentUser = data.user;
            workspaces = data.workspaces;
            currentWorkspace = chooseWorkspace(data.workspaces);
            showShell();
        }, error -> {
            authStore.clearToken();
            toast(t("sessionExpired"));
            showLogin();
        });
    }

    private Models.Workspace chooseWorkspace(List<Models.Workspace> list) {
        if (list.isEmpty()) return null;
        String saved = authStore.workspaceId();
        for (Models.Workspace ws : list) {
            if (ws.id.equals(saved)) return ws;
        }
        authStore.saveWorkspaceId(list.get(0).id);
        return list.get(0);
    }

    private void showLogin() {
        LinearLayout root = vertical();
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(40), dp(24), dp(24));
        TextView mark = label("⚡", 54, BLUE, true);
        TextView title = label(t("signIn"), 26, TEXT, true);
        TextView subtitle = label(t("signInHint"), 15, MUTED, false);
        EditText email = input("you@example.com");
        email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        Button continueButton = button(t("continue"), BLUE, 0xFFFFFFFF);

        root.addView(spacer(30));
        root.addView(mark);
        root.addView(title);
        root.addView(subtitle);
        root.addView(spacer(20));
        root.addView(email, matchWrap());
        root.addView(spacer(12));
        root.addView(continueButton, matchWrap());

        continueButton.setOnClickListener(v -> {
            String value = email.getText().toString().trim();
            if (value.isEmpty()) {
                toast(t("emailRequired"));
                return;
            }
            continueButton.setEnabled(false);
            run(() -> {
                api.sendCode(value);
                return value;
            }, sentEmail -> showOtp(sentEmail), error -> {
                continueButton.setEnabled(true);
                toast(t("sendCodeFailed"));
            });
        });
        setContentView(root);
    }

    private void showOtp(String email) {
        LinearLayout root = vertical();
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(40), dp(24), dp(24));
        TextView title = label(t("otpTitle"), 24, TEXT, true);
        TextView subtitle = label(email, 14, MUTED, false);
        EditText code = input("123456");
        code.setInputType(InputType.TYPE_CLASS_NUMBER);
        Button verify = button(t("verify"), BLUE, 0xFFFFFFFF);
        Button back = button(t("back"), 0xFFE5E7EB, TEXT);
        root.addView(spacer(70));
        root.addView(title);
        root.addView(subtitle);
        root.addView(spacer(18));
        root.addView(code, matchWrap());
        root.addView(spacer(12));
        root.addView(verify, matchWrap());
        root.addView(spacer(8));
        root.addView(back, matchWrap());
        verify.setOnClickListener(v -> {
            String value = code.getText().toString().trim();
            if (value.length() < 4) {
                toast(t("otpRequired"));
                return;
            }
            verify.setEnabled(false);
            run(() -> {
                String token = api.verifyCode(email, value);
                authStore.saveToken(token);
                SessionData data = new SessionData();
                data.user = api.me();
                data.workspaces = api.workspaces();
                return data;
            }, data -> {
                currentUser = data.user;
                workspaces = data.workspaces;
                currentWorkspace = chooseWorkspace(data.workspaces);
                showShell();
            }, error -> {
                verify.setEnabled(true);
                toast(t("otpFailed"));
            });
        });
        back.setOnClickListener(v -> showLogin());
        setContentView(root);
    }

    private void showShell() {
        shell = vertical();
        shell.setBackgroundColor(APP_BG);
        shell.addView(header(), matchWrap());
        content = new FrameLayout(this);
        content.setBackgroundColor(APP_BG);
        shell.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        shell.addView(bottomNav(), matchWrap());
        setContentView(shell);
        renderTab();
    }

    private View header() {
        LinearLayout header = horizontal();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(12), dp(14), dp(10));
        header.setBackgroundColor(0xFFFFFFFF);
        TextView title = label("Multi-Casual", 22, TEXT, true);
        header.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        if (!workspaces.isEmpty()) {
            Spinner spinner = new Spinner(this);
            List<String> names = new ArrayList<>();
            int selected = 0;
            for (int i = 0; i < workspaces.size(); i++) {
                Models.Workspace ws = workspaces.get(i);
                names.add(ws.name);
                if (currentWorkspace != null && currentWorkspace.id.equals(ws.id)) selected = i;
            }
            spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
            spinner.setSelection(selected, false);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Models.Workspace ws = workspaces.get(position);
                    if (currentWorkspace == null || !currentWorkspace.id.equals(ws.id)) {
                        currentWorkspace = ws;
                        authStore.saveWorkspaceId(ws.id);
                        projectCache = new ArrayList<>();
                        memberCache = new ArrayList<>();
                        agentCache = new ArrayList<>();
                        squadCache = new ArrayList<>();
                        runtimeCache = new ArrayList<>();
                        skillCache = new ArrayList<>();
                        autopilotCache = new ArrayList<>();
                        labelCache = new ArrayList<>();
                        chatSessionCache = new ArrayList<>();
                        renderTab();
                    }
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
            header.addView(spinner, new LinearLayout.LayoutParams(dp(150), ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        Button lang = pillButton(zh ? "EN" : "中文", SOFT_BLUE, BLUE);
        lang.setOnClickListener(v -> {
            zh = !zh;
            authStore.setChinese(zh);
            showShell();
        });
        header.addView(lang);
        return header;
    }

    private View bottomNav() {
        LinearLayout nav = horizontal();
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(10), dp(8), dp(10), dp(8));
        nav.setBackground(roundedStroke(0xFFFFFFFF, BORDER, 0, 0));
        addNav(nav, "inbox", t("inbox"));
        addNav(nav, "issues", t("issues"));
        addNav(nav, "myIssues", t("myIssues"));
        addNav(nav, "projects", t("projects"));
        addNav(nav, "settings", t("settings"));
        return nav;
    }

    private void addNav(LinearLayout nav, String key, String label) {
        boolean selected = key.equals(tab);
        TextView item = label(label, 12, selected ? BLUE : MUTED, true);
        item.setGravity(Gravity.CENTER);
        item.setSingleLine(true);
        item.setEllipsize(TextUtils.TruncateAt.END);
        item.setPadding(dp(4), dp(8), dp(4), dp(8));
        if (selected) item.setBackground(rounded(SOFT_BLUE, 18));
        item.setOnClickListener(v -> {
            tab = key;
            showShell();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(dp(3), 0, dp(3), 0);
        nav.addView(item, params);
    }

    private void renderTab() {
        if (content == null) return;
        if ("issues".equals(tab)) showIssues(false);
        else if ("myIssues".equals(tab)) showIssues(true);
        else if ("projects".equals(tab)) showProjects();
        else if ("agents".equals(tab)) showAgents();
        else if ("settings".equals(tab)) showSettings();
        else showInbox();
    }

    private void showInbox() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            renderInbox(demoInbox);
            return;
        }
        setContent(loadingView(t("loadingInbox")));
        run(() -> api.inbox(currentWorkspace.id, 50, 0), page -> {
            renderInbox(page.items);
        }, error -> showError(t("loadFailed"), this::showInbox));
    }

    private void renderInbox(List<Models.InboxItem> items) {
        LinearLayout body = vertical();
        body.setPadding(dp(18), dp(14), dp(18), dp(18));
        body.setBackgroundColor(APP_BG);
        LinearLayout tools = horizontal();
        tools.setGravity(Gravity.CENTER_VERTICAL);
        tools.addView(screenTitle(t("inbox")), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button chat = pillButton(t("chat"), SOFT_BLUE, BLUE);
        chat.setOnClickListener(v -> showChatSessions());
        tools.addView(chat);
        body.addView(tools);
        if (items.isEmpty()) body.addView(empty(t("emptyInbox")));
        for (Models.InboxItem item : items) {
            LinearLayout row = card();
            String displayIdentifier = item.issueIdentifier == null || item.issueIdentifier.isEmpty() || item.issueIdentifier.equals(item.issueId) ? "" : item.issueIdentifier;
            String meta = displayIdentifier.isEmpty() ? humanizeType(item.type) : displayIdentifier + " · " + humanizeType(item.type);
            if (item.issueStatus != null && !item.issueStatus.isEmpty()) meta += " · " + Models.statusLabel(item.issueStatus, zh);
            TextView id = label(meta, 12, MUTED, true);
            TextView title = label(item.issueTitle, 16, TEXT, !item.read);
            title.setMaxLines(2);
            title.setEllipsize(TextUtils.TruncateAt.END);
            row.addView(id);
            row.addView(title);
            String bodyText = cleanDisplayText(item.body);
            if (!bodyText.isEmpty()) {
                TextView preview = label(bodyText, 14, MUTED, false);
                preview.setMaxLines(3);
                preview.setEllipsize(TextUtils.TruncateAt.END);
                preview.setPadding(0, dp(4), 0, 0);
                row.addView(preview);
            }
            row.setOnClickListener(v -> showIssueDetail(item.issueId));
            body.addView(row);
        }
        setContent(scroll(body));
    }

    private void showIssues() {
        showIssues(false);
    }

    private void showIssues(boolean assignedToMe) {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            List<Models.Issue> issues = new ArrayList<>();
            for (Models.Issue issue : demoIssues) {
                if (!assignedToMe || currentUser != null && currentUser.id.equals(issue.assigneeId)) {
                    issues.add(issue);
                }
            }
            renderIssues(issues, assignedToMe);
            return;
        }
        setContent(loadingView(t("loadingIssues")));
        run(() -> {
            IssuesData data = new IssuesData();
            String assigneeId = assignedToMe && currentUser != null ? currentUser.id : null;
            data.issues = IssueBuckets.loadAllConcurrent((status, limit, offset) ->
                    api.issues(currentWorkspace.id, limit, offset, assigneeId, status), 100);
            data.projects = safeProjects();
            data.agents = safeAgents();
            return data;
        }, data -> {
            projectCache = data.projects;
            agentCache = data.agents;
            Collections.sort(data.issues, (a, b) -> issuesDescending ? b.updatedAt.compareTo(a.updatedAt) : a.updatedAt.compareTo(b.updatedAt));
            renderIssues(data.issues, assignedToMe);
        }, error -> showError(t("loadFailed") + "\n" + error.getMessage(), () -> showIssues(assignedToMe)));
    }

    private void renderIssues(List<Models.Issue> issues, boolean assignedToMe) {
        Collections.sort(issues, (a, b) -> issuesDescending ? b.updatedAt.compareTo(a.updatedAt) : a.updatedAt.compareTo(b.updatedAt));
        LinearLayout body = vertical();
        body.setPadding(dp(18), dp(14), dp(18), dp(18));
        body.setBackgroundColor(APP_BG);
        LinearLayout tools = horizontal();
        tools.setGravity(Gravity.CENTER_VERTICAL);
        tools.addView(screenTitle(assignedToMe ? t("myIssues") : t("issues")), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button sort = pillButton(issuesDescending ? "↓" : "↑", SOFT_BLUE, BLUE);
        sort.setOnClickListener(v -> {
            issuesDescending = !issuesDescending;
            showIssues(assignedToMe);
        });
        Button mode = pillButton(boardMode ? t("list") : t("board"), SOFT_GRAY, TEXT);
        mode.setOnClickListener(v -> {
            boardMode = !boardMode;
            showIssues(assignedToMe);
        });
        Button add = pillButton("+", BLUE, 0xFFFFFFFF);
        add.setOnClickListener(v -> showIssueEditor(null, () -> showIssues(assignedToMe)));
        tools.addView(sort);
        tools.addView(mode);
        tools.addView(add);
        body.addView(tools);
        if (boardMode) body.addView(issueBoard(issues));
        else body.addView(issueGroupedList(issues));
        setContent(scroll(body));
    }

    private View issueGroupedList(List<Models.Issue> issues) {
        LinearLayout list = vertical();
        Map<String, List<Models.Issue>> grouped = new HashMap<>();
        for (Models.Issue issue : issues) grouped.computeIfAbsent(issue.status, k -> new ArrayList<>()).add(issue);
        for (String status : Models.STATUS_VALUES) {
            List<Models.Issue> bucket = grouped.get(status);
            if (bucket == null) bucket = Models.emptyList();
            TextView header = label(Models.statusLabel(status, zh) + " (" + bucket.size() + ")", 13, MUTED, true);
            header.setPadding(0, dp(16), 0, dp(6));
            list.addView(header);
            for (Models.Issue issue : bucket) list.addView(issueRow(issue));
        }
        return list;
    }

    private View issueBoard(List<Models.Issue> issues) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout columns = horizontal();
        Map<String, List<Models.Issue>> grouped = new HashMap<>();
        for (Models.Issue issue : issues) grouped.computeIfAbsent(issue.status, k -> new ArrayList<>()).add(issue);
        for (String status : Models.STATUS_VALUES) {
            if ("cancelled".equals(status)) continue;
            LinearLayout col = vertical();
            col.setPadding(dp(8), dp(8), dp(8), dp(8));
            col.setBackground(roundedStroke(0xFFF3F4F6, BORDER, 14, 1));
            List<Models.Issue> bucket = grouped.get(status);
            if (bucket == null) bucket = Models.emptyList();
            col.addView(label(Models.statusLabel(status, zh) + " (" + bucket.size() + ")", 13, TEXT, true));
            for (Models.Issue issue : bucket) col.addView(issueRow(issue));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(260), ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, dp(8), dp(12), dp(8));
            columns.addView(col, params);
        }
        scroll.addView(columns);
        return scroll;
    }

    private View issueRow(Models.Issue issue) {
        LinearLayout row = card();
        TextView identifier = label(issue.identifier, 12, MUTED, true);
        TextView title = label(issue.title, 16, TEXT, false);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        TextView meta = label(Models.statusLabel(issue.status, zh) + " · " + Models.priorityLabel(issue.priority, zh) + assigneeSuffix(issue), 12, MUTED, false);
        meta.setPadding(0, dp(4), 0, 0);
        row.addView(identifier);
        row.addView(title);
        row.addView(meta);
        row.setOnClickListener(v -> showIssueDetail(issue.id));
        return row;
    }

    private String assigneeSuffix(Models.Issue issue) {
        if (issue.assigneeId == null) return "";
        return " · " + assigneeName(issue.assigneeId, issue.assigneeType);
    }

    private String cleanDisplayText(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        return "null".equalsIgnoreCase(trimmed) ? "" : trimmed;
    }

    private String humanizeType(String value) {
        String cleaned = cleanDisplayText(value);
        if (cleaned.isEmpty()) return "";
        String[] parts = cleaned.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (builder.length() > 0) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) builder.append(part.substring(1));
        }
        return builder.length() == 0 ? cleaned : builder.toString();
    }

    private void showIssueDetail(String issueId) {
        if (demoMode) {
            renderDemoIssueDetail(issueId);
            return;
        }
        setContent(loadingView(t("loadingIssue")));
        tab = "issues";
        run(() -> IssueDetailLoader.load(issueId, currentWorkspace == null ? null : currentWorkspace.id, new IssueDetailLoader.Fetcher() {
            @Override
            public Models.Issue issue(String requestedIssueId, String workspaceId) throws Exception {
                return api.issue(requestedIssueId, workspaceId);
            }

            @Override
            public List<Models.Comment> comments(String requestedIssueId, String workspaceId) throws Exception {
                return api.comments(requestedIssueId, workspaceId, 100, 0).items;
            }

            @Override
            public List<Models.AgentTask> runs(String requestedIssueId, String workspaceId) throws Exception {
                return api.agentRuns(requestedIssueId, workspaceId);
            }

            @Override
            public List<Models.Project> projects() {
                return safeProjects();
            }

            @Override
            public List<Models.Member> members() {
                return safeMembers();
            }

            @Override
            public List<Models.Agent> agents() {
                return safeAgents();
            }
        }, 5), data -> {
            projectCache = data.projects;
            memberCache = data.members;
            agentCache = data.agents;
            try { squadCache = safeSquads(); } catch (Exception ignored) { squadCache = new ArrayList<>(); }
            LinearLayout screen = vertical();
            LinearLayout scrollBody = vertical();
            scrollBody.setPadding(dp(14), dp(8), dp(14), dp(14));

            LinearLayout top = horizontal();
            top.setGravity(Gravity.CENTER_VERTICAL);
            Button back = smallButton("‹");
            back.setOnClickListener(v -> showIssues(false));
            top.addView(back);
            top.addView(label(data.issue.identifier, 18, TEXT, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            Button edit = smallButton(t("edit"));
            edit.setOnClickListener(v -> showIssueEditor(data.issue, () -> showIssueDetail(data.issue.id)));
            top.addView(edit);
            scrollBody.addView(top);

            TextView title = label(data.issue.title, 23, TEXT, true);
            title.setPadding(0, dp(10), 0, dp(6));
            scrollBody.addView(title);
            scrollBody.addView(label(Models.statusLabel(data.issue.status, zh) + " · " + Models.priorityLabel(data.issue.priority, zh) + assigneeSuffix(data.issue), 13, MUTED, false));
            if (data.issue.description != null && !data.issue.description.trim().isEmpty()) {
                LinearLayout md = vertical();
                md.setPadding(0, dp(12), 0, dp(8));
                MarkdownRenderer.render(this, md, data.issue.description, TEXT, MUTED, BORDER);
                scrollBody.addView(md);
            }

            scrollBody.addView(sectionTitle(t("latestProgress")));
            Collections.sort(data.comments, (a, b) -> b.createdAt.compareTo(a.createdAt));
            if (data.comments.isEmpty()) scrollBody.addView(empty(t("emptyComments")));
            for (Models.Comment comment : data.comments) scrollBody.addView(commentView(comment));

            scrollBody.addView(sectionTitle(t("agentActivity")));
            if (data.runs.isEmpty()) scrollBody.addView(empty(t("emptyActivity")));
            for (Models.AgentTask run : data.runs) scrollBody.addView(agentRunView(run));

            ScrollView scroll = scroll(scrollBody);
            screen.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            screen.addView(commentInput(issueId), matchWrap());
            setContent(screen);
        }, error -> showError(t("loadFailed") + "\n" + error.getMessage(), () -> showIssueDetail(issueId)));
    }

    private void renderDemoIssueDetail(String issueId) {
        Models.Issue issue = findDemoIssue(issueId);
        if (issue == null) {
            showError(t("loadFailed"), () -> showIssues(false));
            return;
        }
        LinearLayout screen = vertical();
        LinearLayout scrollBody = vertical();
        scrollBody.setPadding(dp(14), dp(8), dp(14), dp(14));

        LinearLayout top = horizontal();
        top.setGravity(Gravity.CENTER_VERTICAL);
        Button back = smallButton("‹");
        back.setOnClickListener(v -> showIssues(false));
        top.addView(back);
        top.addView(label(issue.identifier, 18, TEXT, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button edit = smallButton(t("edit"));
        edit.setOnClickListener(v -> showIssueEditor(issue, () -> renderDemoIssueDetail(issue.id)));
        top.addView(edit);
        scrollBody.addView(top);

        TextView title = label(issue.title, 23, TEXT, true);
        title.setPadding(0, dp(10), 0, dp(6));
        scrollBody.addView(title);
        scrollBody.addView(label(Models.statusLabel(issue.status, zh) + " · " + Models.priorityLabel(issue.priority, zh) + assigneeSuffix(issue), 13, MUTED, false));
        LinearLayout md = vertical();
        md.setPadding(0, dp(12), 0, dp(8));
        MarkdownRenderer.render(this, md, issue.description, TEXT, MUTED, BORDER);
        scrollBody.addView(md);

        scrollBody.addView(sectionTitle(t("latestProgress")));
        List<Models.Comment> comments = demoCommentsByIssueId.get(issueId);
        if (comments == null || comments.isEmpty()) scrollBody.addView(empty(t("emptyComments")));
        else for (Models.Comment comment : comments) scrollBody.addView(commentView(comment));

        scrollBody.addView(sectionTitle(t("agentActivity")));
        List<Models.AgentTask> runs = demoRunsByIssueId.get(issueId);
        if (runs == null || runs.isEmpty()) scrollBody.addView(empty(t("emptyActivity")));
        else for (Models.AgentTask run : runs) scrollBody.addView(agentRunView(run));

        screen.addView(scroll(scrollBody), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        screen.addView(commentInput(issueId), matchWrap());
        setContent(screen);
    }

    private View commentView(Models.Comment comment) {
        LinearLayout box = card();
        LinearLayout meta = horizontal();
        meta.setGravity(Gravity.CENTER_VERTICAL);
        String name = comment.authorName == null || comment.authorName.isEmpty()
                ? assigneeName(comment.authorId, comment.authorType)
                : comment.authorName;
        meta.addView(label(("agent".equals(comment.authorType) ? "⚡ " : "○ ") + name, 13, TEXT, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        meta.addView(label(shortDate(comment.createdAt), 11, MUTED, false));
        box.addView(meta);
        LinearLayout md = vertical();
        md.setPadding(0, dp(6), 0, 0);
        MarkdownRenderer.render(this, md, comment.content, TEXT, MUTED, BORDER);
        box.addView(md);
        return box;
    }

    private View agentRunView(Models.AgentTask run) {
        LinearLayout row = card();
        LinearLayout line = horizontal();
        line.setGravity(Gravity.CENTER_VERTICAL);
        int color = "completed".equals(run.status) ? GREEN : "failed".equals(run.status) ? RED : BLUE;
        line.addView(label("●", 16, color, true));
        line.addView(label("  " + t("agentRun") + " · " + run.status, 15, TEXT, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button open = smallButton(t("details"));
        open.setOnClickListener(v -> showTranscript(run.id));
        line.addView(open);
        row.addView(line);
        row.addView(label(shortDate(run.startedAt), 12, MUTED, false));
        if (run.error != null && !run.error.isEmpty()) row.addView(label(run.error, 13, RED, false));
        return row;
    }

    private View commentInput(String issueId) {
        LinearLayout bar = horizontal();
        bar.setPadding(dp(12), dp(8), dp(12), dp(8));
        bar.setBackgroundColor(0xFFFFFFFF);
        EditText input = input(t("commentPlaceholder"));
        input.setMinLines(1);
        input.setMaxLines(4);
        Button send = smallButton("↑");
        send.setTextSize(20);
        send.setTextColor(BLUE);
        send.setOnClickListener(v -> {
            String content = input.getText().toString().trim();
            if (content.isEmpty()) return;
            if (demoMode) {
                addDemoComment(issueId, content);
                renderDemoIssueDetail(issueId);
                return;
            }
            send.setEnabled(false);
            String workspaceId = currentWorkspace == null ? null : currentWorkspace.id;
            run(() -> api.addComment(issueId, workspaceId, content), comment -> showIssueDetail(issueId), error -> {
                send.setEnabled(true);
                toast(t("commentFailed"));
            });
        });
        bar.addView(input, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        bar.addView(send);
        return bar;
    }

    private void showTranscript(String runId) {
        LinearLayout body = vertical();
        body.setPadding(dp(12), dp(8), dp(12), dp(8));
        body.addView(loadingView(t("loading")));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(t("transcript")).setView(scroll(body)).setPositiveButton(t("done"), null).show();
        if (demoMode) {
            body.removeAllViews();
            String[] rows = {
                    "#1 · thinking\nReviewing Android parity checklist.",
                    "#2 · tool_use · gradle\nassembleDebug",
                    "#3 · tool_result\nBUILD SUCCESSFUL",
                    "#4 · text\nMarkdown table renderer and issue editor are ready for inspection."
            };
            for (String rowText : rows) {
                LinearLayout row = card();
                row.addView(label(rowText, 13, TEXT, false));
                body.addView(row);
            }
            return;
        }
        String workspaceId = currentWorkspace == null ? null : currentWorkspace.id;
        run(() -> api.runMessages(runId, workspaceId), messages -> {
            body.removeAllViews();
            if (messages.isEmpty()) body.addView(empty(t("emptyActivity")));
            for (Models.TaskMessage msg : messages) {
                LinearLayout row = card();
                row.addView(label("#" + msg.seq + " · " + msg.type + (msg.tool.isEmpty() ? "" : " · " + msg.tool), 12, MUTED, true));
                row.addView(label(messageSummary(msg), 13, TEXT, false));
                body.addView(row);
            }
        }, error -> {
            body.removeAllViews();
            body.addView(label(t("loadFailed") + "\n" + error.getMessage(), 14, RED, false));
        });
    }

    private String messageSummary(Models.TaskMessage msg) {
        String text = msg.content == null || msg.content.isEmpty() ? msg.output : msg.content;
        if (text == null) text = "";
        return text.length() > 1200 ? text.substring(0, 1200) + "…" : text;
    }

    private void showIssueEditor(Models.Issue issue, Runnable afterSave) {
        boolean editing = issue != null;
        LinearLayout form = vertical();
        form.setPadding(dp(12), dp(8), dp(12), dp(8));
        EditText title = input(t("title"));
        title.setText(editing ? issue.title : "");
        EditText desc = input(t("description"));
        desc.setMinLines(4);
        desc.setGravity(Gravity.TOP);
        desc.setText(editing ? issue.description : "");
        Spinner project = spinner(projectLabels(true));
        Spinner status = spinner(statusLabels());
        Spinner priority = spinner(priorityLabels());
        Spinner assignee = spinner(assigneeLabels(true));
        if (editing) {
            selectValue(status, Models.STATUS_VALUES, issue.status);
            selectValue(priority, Models.PRIORITY_VALUES, issue.priority);
            selectProject(project, issue.projectId);
            selectAssignee(assignee, issue.assigneeId, issue.assigneeType);
        } else {
            selectValue(status, Models.STATUS_VALUES, "todo");
            selectValue(priority, Models.PRIORITY_VALUES, "medium");
        }
        form.addView(label(t("title"), 12, MUTED, true));
        form.addView(title);
        form.addView(label(t("description"), 12, MUTED, true));
        form.addView(desc);
        form.addView(label(t("project"), 12, MUTED, true));
        form.addView(project);
        form.addView(label(t("status"), 12, MUTED, true));
        form.addView(status);
        form.addView(label(t("priority"), 12, MUTED, true));
        form.addView(priority);
        form.addView(label(t("assignee"), 12, MUTED, true));
        form.addView(assignee);

        new AlertDialog.Builder(this)
                .setTitle(editing ? t("editIssue") : t("newIssue"))
                .setView(scroll(form))
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(editing ? t("save") : t("create"), (dialog, which) -> {
                    String titleText = title.getText().toString().trim();
                    if (titleText.isEmpty()) {
                        toast(t("titleRequired"));
                        return;
                    }
                    String projectId = projectIdAt(project.getSelectedItemPosition(), true);
                    String statusValue = Models.STATUS_VALUES[Math.max(0, status.getSelectedItemPosition())];
                    String priorityValue = Models.PRIORITY_VALUES[Math.max(0, priority.getSelectedItemPosition())];
                    Models.Assignee selectedAssignee = assigneeAt(assignee.getSelectedItemPosition(), true);
                    if (demoMode) {
                        upsertDemoIssue(
                                editing ? issue.id : "demo-issue-" + (demoIssues.size() + 1),
                                titleText,
                                desc.getText().toString(),
                                projectId,
                                statusValue,
                                priorityValue,
                                selectedAssignee
                        );
                        afterSave.run();
                        return;
                    }
                    run(() -> {
                        if (editing) {
                            return api.updateIssue(issue, titleText, desc.getText().toString(), projectId, statusValue, priorityValue, selectedAssignee);
                        }
                        if (currentWorkspace == null) throw new IllegalStateException(t("workspaceRequired"));
                        return api.createIssue(titleText, desc.getText().toString(), currentWorkspace.id, projectId, statusValue, priorityValue, selectedAssignee);
                    }, saved -> afterSave.run(), error -> toast(t("saveFailed") + ": " + error.getMessage()));
                }).show();
    }

    private void showProjects() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            renderProjects(projectCache);
            return;
        }
        setContent(loadingView(t("loadingProjects")));
        run(() -> api.projects(currentWorkspace.id, 100, 0).items, projects -> {
            projectCache = projects;
            renderProjects(projects);
        }, error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showProjects));
    }

    private void renderProjects(List<Models.Project> projects) {
        LinearLayout body = vertical();
        body.setPadding(dp(18), dp(14), dp(18), dp(18));
        body.setBackgroundColor(APP_BG);
        body.addView(screenTitle(t("projects")));
        if (projects.isEmpty()) body.addView(empty(t("emptyProjects")));
        for (Models.Project project : projects) {
            LinearLayout row = card();
            row.addView(label(project.name, 16, TEXT, true));
            String description = cleanDisplayText(project.description);
            if (!description.isEmpty()) {
                TextView preview = label(description, 13, MUTED, false);
                preview.setMaxLines(2);
                preview.setEllipsize(TextUtils.TruncateAt.END);
                row.addView(preview);
            }
            row.setOnClickListener(v -> showProjectDetail(project));
            body.addView(row);
        }
        setContent(scroll(body));
    }

    private void showProjectDetail(Models.Project project) {
        if (demoMode) {
            LinearLayout body = vertical();
            body.setPadding(dp(14), dp(8), dp(14), dp(14));
            Button back = smallButton("‹ " + t("projects"));
            back.setOnClickListener(v -> showProjects());
            body.addView(back);
            body.addView(screenTitle(project.name));
            if (project.description != null && !project.description.isEmpty()) body.addView(label(project.description, 14, MUTED, false));
            body.addView(sectionTitle(t("issues")));
            int count = 0;
            for (Models.Issue issue : demoIssues) {
                if (project.id.equals(issue.projectId)) {
                    body.addView(issueRow(issue));
                    count++;
                }
            }
            if (count == 0) body.addView(empty(t("emptyIssues")));
            setContent(scroll(body));
            return;
        }
        setContent(loadingView(t("loadingIssues")));
        run(() -> IssueBuckets.loadAllConcurrent((status, limit, offset) ->
                api.projectIssues(currentWorkspace.id, project.id, status, limit, offset), 100), issues -> {
            LinearLayout body = vertical();
            body.setPadding(dp(14), dp(8), dp(14), dp(14));
            Button back = smallButton("‹ " + t("projects"));
            back.setOnClickListener(v -> showProjects());
            body.addView(back);
            body.addView(screenTitle(project.name));
            if (project.description != null && !project.description.isEmpty()) body.addView(label(project.description, 14, MUTED, false));
            body.addView(sectionTitle(t("issues")));
            int count = 0;
            for (Models.Issue issue : issues) {
                if (project.id.equals(issue.projectId)) {
                    body.addView(issueRow(issue));
                    count++;
                }
            }
            if (count == 0) body.addView(empty(t("emptyIssues")));
            setContent(scroll(body));
        }, error -> showError(t("loadFailed"), () -> showProjectDetail(project)));
    }

    private void showAgents() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            renderAgents(agentCache);
            return;
        }
        setContent(loadingView(t("loadingAgents")));
        run(() -> {
            AgentsData data = new AgentsData();
            data.agents = api.agents(currentWorkspace.id);
            try {
                data.runtimes = api.runtimes(currentWorkspace.id);
            } catch (Exception ignored) {
                data.runtimes = runtimeCache;
            }
            return data;
        }, data -> {
            agentCache = data.agents;
            runtimeCache = data.runtimes;
            renderAgents(data.agents);
        }, error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showAgents));
    }

    private void renderAgents(List<Models.Agent> agents) {
        LinearLayout body = vertical();
        body.setPadding(dp(14), dp(8), dp(14), dp(14));
        LinearLayout tools = horizontal();
        tools.setGravity(Gravity.CENTER_VERTICAL);
        Button back = smallButton("‹");
        back.setOnClickListener(v -> showSettings());
        tools.addView(back);
        tools.addView(screenTitle(t("agents")), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button add = smallButton("+");
        add.setOnClickListener(v -> showAgentCreator());
        tools.addView(add);
        body.addView(tools);
        if (agents.isEmpty()) body.addView(empty(t("emptyAgents")));
        for (Models.Agent agent : agents) {
            LinearLayout row = card();
            LinearLayout titleRow = horizontal();
            titleRow.setGravity(Gravity.CENTER_VERTICAL);
            titleRow.addView(label("⚡ " + agent.name, 16, TEXT, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            if (agent.archivedAt == null || agent.archivedAt.isEmpty()) {
                Button archive = smallButton(t("archive"));
                archive.setOnClickListener(v -> confirmArchiveAgent(agent));
                titleRow.addView(archive);
            }
            row.addView(titleRow);
            if (agent.description != null && !agent.description.isEmpty()) row.addView(label(agent.description, 13, MUTED, false));
            if (agent.status != null && !agent.status.isEmpty()) row.addView(label(agent.status, 12, MUTED, false));
            if (agent.archivedAt != null && !agent.archivedAt.isEmpty()) row.addView(label(t("archived") + " · " + shortDate(agent.archivedAt), 12, MUTED, false));
            body.addView(row);
        }
        setContent(scroll(body));
    }

    private void confirmArchiveAgent(Models.Agent agent) {
        new AlertDialog.Builder(this)
                .setTitle(t("archiveAgent"))
                .setMessage(agent.name)
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("archive"), (dialog, which) -> {
                    if (demoMode) {
                        agentCache.remove(agent);
                        showAgents();
                        return;
                    }
                    if (currentWorkspace == null) {
                        toast(t("workspaceRequired"));
                        return;
                    }
                    run(() -> api.archiveAgent(currentWorkspace.id, agent.id),
                            ignored -> showAgents(),
                            error -> toast(t("saveFailed") + ": " + error.getMessage()));
                }).show();
    }

    private void showAgentCreator() {
        LinearLayout form = vertical();
        form.setPadding(dp(12), dp(8), dp(12), dp(8));
        EditText name = input(t("name"));
        EditText desc = input(t("description"));
        desc.setMinLines(3);
        Spinner runtime = spinner(runtimeLabels());
        form.addView(label(t("name"), 12, MUTED, true));
        form.addView(name);
        form.addView(label(t("description"), 12, MUTED, true));
        form.addView(desc);
        form.addView(label(t("runtime"), 12, MUTED, true));
        form.addView(runtime);
        new AlertDialog.Builder(this)
                .setTitle(t("newAgent"))
                .setView(form)
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("create"), (dialog, which) -> {
                    if (name.getText().toString().trim().isEmpty()) return;
                    String runtimeId = runtimeIdAt(runtime.getSelectedItemPosition());
                    if (runtimeId == null) {
                        toast(t("runtimeRequired"));
                        return;
                    }
                    if (demoMode) {
                        try {
                            Models.Agent agent = new Models.Agent(json(
                                    "id", "demo-agent-" + (agentCache.size() + 1),
                                    "name", name.getText().toString().trim(),
                                    "description", desc.getText().toString(),
                                    "status", "idle"
                            ));
                            agentCache.add(agent);
                            showAgents();
                        } catch (Exception error) {
                            toast(error.getMessage());
                        }
                        return;
                    }
                    run(() -> api.createAgent(currentWorkspace.id, name.getText().toString().trim(), desc.getText().toString(), runtimeId),
                            agent -> showAgents(),
                            error -> toast(t("saveFailed") + ": " + error.getMessage()));
                }).show();
    }

    private void showAutopilots() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            renderAutopilots(autopilotCache);
            return;
        }
        setContent(loadingView(t("loadingAutopilots")));
        run(() -> api.autopilots(currentWorkspace.id), autopilots -> {
            autopilotCache = autopilots;
            renderAutopilots(autopilots);
        }, error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showAutopilots));
    }

    private void renderAutopilots(List<Models.Autopilot> autopilots) {
        LinearLayout body = configPageShell(t("autopilots"));
        if (autopilots.isEmpty()) body.addView(empty(t("emptyAutopilots")));
        for (Models.Autopilot autopilot : autopilots) {
            LinearLayout row = card();
            row.addView(label(autopilot.title, 16, TEXT, true));
            if (autopilot.description != null && !autopilot.description.isEmpty()) row.addView(label(autopilot.description, 13, MUTED, false));
            if (autopilot.status != null && !autopilot.status.isEmpty()) row.addView(label(autopilot.status, 12, MUTED, false));
            body.addView(row);
        }
        setContent(scroll(body));
    }

    private void showRuntimes() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            renderRuntimes(runtimeCache);
            return;
        }
        setContent(loadingView(t("loadingRuntimes")));
        run(() -> api.runtimes(currentWorkspace.id), runtimes -> {
            runtimeCache = runtimes;
            renderRuntimes(runtimes);
        }, error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showRuntimes));
    }

    private void renderRuntimes(List<Models.Runtime> runtimes) {
        LinearLayout body = configPageShell(t("runtimes"));
        if (runtimes.isEmpty()) body.addView(empty(t("emptyRuntimes")));
        for (Models.Runtime runtime : runtimes) {
            LinearLayout row = card();
            row.addView(label(runtime.name, 16, TEXT, true));
            row.addView(label((runtime.status == null ? "" : runtime.status) + (runtime.version == null || runtime.version.isEmpty() ? "" : " · " + runtime.version), 13, MUTED, false));
            body.addView(row);
        }
        setContent(scroll(body));
    }

    private void showSkills() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            renderSkills(skillCache);
            return;
        }
        setContent(loadingView(t("loadingSkills")));
        run(() -> api.skills(currentWorkspace.id), skills -> {
            skillCache = skills;
            renderSkills(skills);
        }, error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showSkills));
    }

    private void renderSkills(List<Models.Skill> skills) {
        LinearLayout body = configPageShell(t("skills"));
        LinearLayout tools = (LinearLayout) body.getChildAt(0);
        Button add = smallButton("+");
        add.setOnClickListener(v -> showSkillCreator());
        tools.addView(add);
        if (skills.isEmpty()) body.addView(empty(t("emptySkills")));
        for (Models.Skill skill : skills) {
            LinearLayout row = card();
            row.addView(label(skill.name, 16, TEXT, true));
            if (skill.description != null && !skill.description.isEmpty()) row.addView(label(skill.description, 13, MUTED, false));
            body.addView(row);
        }
        setContent(scroll(body));
    }

    private LinearLayout configPageShell(String titleText) {
        LinearLayout body = vertical();
        body.setPadding(dp(14), dp(8), dp(14), dp(14));
        LinearLayout tools = horizontal();
        tools.setGravity(Gravity.CENTER_VERTICAL);
        Button back = smallButton("‹");
        back.setOnClickListener(v -> showSettings());
        tools.addView(back);
        tools.addView(screenTitle(titleText), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        body.addView(tools);
        return body;
    }

    private void showSkillCreator() {
        LinearLayout form = vertical();
        form.setPadding(dp(12), dp(8), dp(12), dp(8));
        EditText name = input(t("name"));
        EditText desc = input(t("description"));
        EditText content = input(t("content"));
        content.setMinLines(5);
        content.setGravity(Gravity.TOP);
        form.addView(label(t("name"), 12, MUTED, true));
        form.addView(name);
        form.addView(label(t("description"), 12, MUTED, true));
        form.addView(desc);
        form.addView(label(t("content"), 12, MUTED, true));
        form.addView(content);
        new AlertDialog.Builder(this)
                .setTitle(t("newSkill"))
                .setView(scroll(form))
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("create"), (dialog, which) -> {
                    String skillName = name.getText().toString().trim();
                    if (skillName.isEmpty()) return;
                    if (demoMode) {
                        try {
                            skillCache.add(new Models.Skill(json(
                                    "id", "demo-skill-" + (skillCache.size() + 1),
                                    "name", skillName,
                                    "description", desc.getText().toString(),
                                    "content", content.getText().toString()
                            )));
                            showSkills();
                        } catch (Exception error) {
                            toast(error.getMessage());
                        }
                        return;
                    }
                    run(() -> api.createSkill(currentWorkspace.id, skillName, desc.getText().toString(), content.getText().toString()),
                            skill -> showSkills(),
                            error -> toast(t("saveFailed") + ": " + error.getMessage()));
                }).show();
    }

    private void showWorkspaceDetails() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            renderTextRows(t("workspaceDetails"), Collections.singletonList("Mobile QA\nmobile-qa\nAndroid parity acceptance workspace.\nFocus: mobile feature parity."));
            return;
        }
        setContent(loadingView(t("loading")));
        run(() -> Collections.singletonList(api.workspaceSummary(currentWorkspace.id)),
                rows -> renderTextRows(t("workspaceDetails"), rows),
                error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showWorkspaceDetails));
    }

    private void showMembers() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            List<String> rows = new ArrayList<>();
            rows.add("Demo Member\ndemo@multica.ai · owner");
            rows.add("Codex Mobile\nagent · workspace automation");
            renderTextRows(t("members"), rows);
            return;
        }
        setContent(loadingView(t("loading")));
        run(() -> api.memberSummaries(currentWorkspace.id),
                rows -> renderTextRows(t("members"), rows),
                error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showMembers));
    }

    private void showNotifications() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            List<String> rows = new ArrayList<>();
            rows.add("assignments\nall");
            rows.add("comments\nall");
            rows.add("agent_activity\nall");
            renderTextRows(t("notifications"), rows);
            return;
        }
        setContent(loadingView(t("loading")));
        run(() -> api.notificationSummaries(currentWorkspace.id),
                rows -> renderTextRows(t("notifications"), rows),
                error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showNotifications));
    }

    private void showTokens() {
        if (demoMode) {
            List<String> rows = new ArrayList<>();
            rows.add("Local CLI\nmca_demo_**** · never used");
            renderTextRows(t("apiTokens"), rows);
            return;
        }
        setContent(loadingView(t("loading")));
        run(() -> api.tokenSummaries(),
                rows -> renderTextRows(t("apiTokens"), rows),
                error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showTokens));
    }

    private void renderTextRows(String titleText, List<String> rows) {
        LinearLayout body = configPageShell(titleText);
        if (rows.isEmpty()) body.addView(empty(t("empty")));
        for (String text : rows) {
            LinearLayout row = card();
            MarkdownRenderer.render(this, row, text, TEXT, MUTED, BORDER);
            body.addView(row);
        }
        setContent(scroll(body));
    }

    private void showWorkspaces() {
        LinearLayout body = configPageShell(t("workspaces"));
        if (workspaces.isEmpty()) body.addView(empty(t("empty")));
        for (Models.Workspace workspace : workspaces) {
            LinearLayout row = card();
            boolean selected = currentWorkspace != null && currentWorkspace.id.equals(workspace.id);
            row.addView(label((selected ? "✓ " : "") + workspace.name, 16, TEXT, true));
            row.addView(label(workspace.slug + " · " + Models.shortId(workspace.id), 12, MUTED, false));
            row.setOnClickListener(v -> {
                currentWorkspace = workspace;
                authStore.saveWorkspaceId(workspace.id);
                projectCache = new ArrayList<>();
                agentCache = new ArrayList<>();
                runtimeCache = new ArrayList<>();
                skillCache = new ArrayList<>();
                autopilotCache = new ArrayList<>();
                labelCache = new ArrayList<>();
                chatSessionCache = new ArrayList<>();
                showShell();
            });
            body.addView(row);
        }
        setContent(scroll(body));
    }

    private void showLabels() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            renderLabels(labelCache);
            return;
        }
        setContent(loadingView(t("loading")));
        run(() -> api.labels(currentWorkspace.id), labels -> {
            labelCache = labels;
            renderLabels(labels);
        }, error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showLabels));
    }

    private void renderLabels(List<Models.IssueLabel> labels) {
        LinearLayout body = configPageShell(t("labels"));
        LinearLayout tools = (LinearLayout) body.getChildAt(0);
        Button add = smallButton("+");
        add.setOnClickListener(v -> showLabelCreator());
        tools.addView(add);
        if (labels.isEmpty()) body.addView(empty(t("emptyLabels")));
        for (Models.IssueLabel label : labels) {
            LinearLayout row = card();
            row.addView(label(label.name, 16, TEXT, true));
            row.addView(label(label.color + " · " + Models.shortId(label.id), 12, MUTED, false));
            body.addView(row);
        }
        setContent(scroll(body));
    }

    private void showLabelCreator() {
        LinearLayout form = vertical();
        form.setPadding(dp(12), dp(8), dp(12), dp(8));
        EditText name = input(t("name"));
        EditText color = input("#2563EB");
        color.setText("#2563EB");
        form.addView(label(t("name"), 12, MUTED, true));
        form.addView(name);
        form.addView(label(t("color"), 12, MUTED, true));
        form.addView(color);
        new AlertDialog.Builder(this)
                .setTitle(t("newLabel"))
                .setView(form)
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("create"), (dialog, which) -> {
                    String labelName = name.getText().toString().trim();
                    if (labelName.isEmpty()) return;
                    String rawColor = color.getText().toString().trim();
                    final String labelColor = rawColor.isEmpty() ? "#2563EB" : rawColor;
                    if (demoMode) {
                        try {
                            labelCache.add(new Models.IssueLabel(json("id", "demo-label-" + (labelCache.size() + 1), "name", labelName, "color", labelColor)));
                            showLabels();
                        } catch (Exception error) {
                            toast(error.getMessage());
                        }
                        return;
                    }
                    run(() -> api.createLabel(currentWorkspace.id, labelName, labelColor),
                            label -> showLabels(),
                            error -> toast(t("saveFailed") + ": " + error.getMessage()));
                }).show();
    }

    private void showFeedback() {
        LinearLayout form = vertical();
        form.setPadding(dp(14), dp(8), dp(14), dp(14));
        LinearLayout tools = horizontal();
        tools.setGravity(Gravity.CENTER_VERTICAL);
        Button back = smallButton("‹");
        back.setOnClickListener(v -> showSettings());
        tools.addView(back);
        tools.addView(screenTitle(t("feedback")), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        form.addView(tools);
        EditText message = input(t("feedbackMessage"));
        message.setMinLines(5);
        message.setGravity(Gravity.TOP);
        EditText url = input("https://...");
        Button send = button(t("send"), BLUE, 0xFFFFFFFF);
        form.addView(label(t("message"), 12, MUTED, true));
        form.addView(message);
        form.addView(label("URL", 12, MUTED, true));
        form.addView(url);
        form.addView(spacer(10));
        form.addView(send, matchWrap());
        send.setOnClickListener(v -> {
            String text = message.getText().toString().trim();
            if (text.isEmpty()) {
                toast(t("feedbackRequired"));
                return;
            }
            if (demoMode) {
                toast(t("feedbackSent"));
                showSettings();
                return;
            }
            if (currentWorkspace == null) {
                toast(t("workspaceRequired"));
                return;
            }
            send.setEnabled(false);
            run(() -> {
                        api.createFeedback(currentWorkspace.id, text, url.getText().toString());
                        return true;
                    },
                    ignored -> {
                        toast(t("feedbackSent"));
                        showSettings();
                    },
                    error -> {
                        send.setEnabled(true);
                        toast(t("sendFailed") + ": " + error.getMessage());
                    });
        });
        setContent(scroll(form));
    }

    private void showSettings() {
        LinearLayout body = vertical();
        body.setPadding(dp(14), dp(8), dp(14), dp(14));
        body.addView(screenTitle(t("settings")));
        LinearLayout account = card();
        if (currentUser != null) {
            account.addView(label(currentUser.name, 17, TEXT, true));
            account.addView(label(currentUser.email, 13, MUTED, false));
        }
        if (currentWorkspace != null) account.addView(label(t("workspace") + ": " + currentWorkspace.name, 13, MUTED, false));
        body.addView(account);
        Button language = button(zh ? "Language: 中文" : "Language: English", 0xFFE5E7EB, TEXT);
        language.setOnClickListener(v -> {
            zh = !zh;
            authStore.setChinese(zh);
            showShell();
        });
        body.addView(language, matchWrap());
        body.addView(spacer(8));
        Button workspacesButton = button(t("workspaces"), 0xFFE5E7EB, TEXT);
        workspacesButton.setOnClickListener(v -> showWorkspaces());
        body.addView(workspacesButton, matchWrap());
        body.addView(spacer(8));
        Button workspace = button(t("workspaceDetails"), 0xFFE5E7EB, TEXT);
        workspace.setOnClickListener(v -> showWorkspaceDetails());
        body.addView(workspace, matchWrap());
        body.addView(spacer(8));
        Button members = button(t("members"), 0xFFE5E7EB, TEXT);
        members.setOnClickListener(v -> showMembers());
        body.addView(members, matchWrap());
        body.addView(spacer(8));
        Button notifications = button(t("notifications"), 0xFFE5E7EB, TEXT);
        notifications.setOnClickListener(v -> showNotifications());
        body.addView(notifications, matchWrap());
        body.addView(spacer(8));
        Button tokens = button(t("apiTokens"), 0xFFE5E7EB, TEXT);
        tokens.setOnClickListener(v -> showTokens());
        body.addView(tokens, matchWrap());
        body.addView(spacer(8));
        Button labels = button(t("labels"), 0xFFE5E7EB, TEXT);
        labels.setOnClickListener(v -> showLabels());
        body.addView(labels, matchWrap());
        body.addView(spacer(8));
        Button agents = button(t("agents"), 0xFFE5E7EB, TEXT);
        agents.setOnClickListener(v -> showAgents());
        body.addView(agents, matchWrap());
        body.addView(spacer(8));
        Button autopilots = button(t("autopilots"), 0xFFE5E7EB, TEXT);
        autopilots.setOnClickListener(v -> showAutopilots());
        body.addView(autopilots, matchWrap());
        body.addView(spacer(8));
        Button runtimes = button(t("runtimes"), 0xFFE5E7EB, TEXT);
        runtimes.setOnClickListener(v -> showRuntimes());
        body.addView(runtimes, matchWrap());
        body.addView(spacer(8));
        Button skills = button(t("skills"), 0xFFE5E7EB, TEXT);
        skills.setOnClickListener(v -> showSkills());
        body.addView(skills, matchWrap());
        body.addView(spacer(8));
        Button feedback = button(t("feedback"), 0xFFE5E7EB, TEXT);
        feedback.setOnClickListener(v -> showFeedback());
        body.addView(feedback, matchWrap());
        body.addView(spacer(8));
        body.addView(label(t("configureHint"), 13, MUTED, false));
        body.addView(spacer(8));
        Button logout = button(t("logout"), RED, 0xFFFFFFFF);
        logout.setOnClickListener(v -> {
            authStore.clearToken();
            currentUser = null;
            currentWorkspace = null;
            showLogin();
        });
        body.addView(logout, matchWrap());
        setContent(scroll(body));
    }

    private void showChatSessions() {
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        if (demoMode) {
            renderChatSessions(chatSessionCache);
            return;
        }
        setContent(loadingView(t("loadingChat")));
        run(() -> {
            if (agentCache.isEmpty()) agentCache = safeAgents();
            return api.chatSessions(currentWorkspace.id);
        }, sessions -> {
            chatSessionCache = sessions;
            renderChatSessions(sessions);
        }, error -> showError(t("loadFailed") + "\n" + error.getMessage(), this::showChatSessions));
    }

    private void renderChatSessions(List<Models.ChatSession> sessions) {
        LinearLayout body = vertical();
        body.setPadding(dp(14), dp(8), dp(14), dp(14));
        LinearLayout tools = horizontal();
        tools.setGravity(Gravity.CENTER_VERTICAL);
        Button back = smallButton("‹ " + t("inbox"));
        back.setOnClickListener(v -> showInbox());
        tools.addView(back);
        tools.addView(screenTitle(t("chat")), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button add = smallButton("+");
        add.setOnClickListener(v -> showChatCreator());
        tools.addView(add);
        body.addView(tools);
        if (sessions.isEmpty()) body.addView(empty(t("emptyChat")));
        for (Models.ChatSession session : sessions) {
            LinearLayout row = card();
            row.addView(label((session.hasUnread ? "● " : "") + session.title, 16, TEXT, true));
            row.addView(label(assigneeName(session.agentId, "agent") + " · " + session.status + " · " + shortDate(session.updatedAt), 12, MUTED, false));
            row.setOnClickListener(v -> showChatMessages(session));
            body.addView(row);
        }
        setContent(scroll(body));
    }

    private void showChatCreator() {
        if (agentCache.isEmpty() && !demoMode) {
            toast(t("loadAgentsFirst"));
            return;
        }
        LinearLayout form = vertical();
        form.setPadding(dp(12), dp(8), dp(12), dp(8));
        EditText title = input(t("title"));
        Spinner agent = spinner(agentLabels());
        form.addView(label(t("title"), 12, MUTED, true));
        form.addView(title);
        form.addView(label(t("agent"), 12, MUTED, true));
        form.addView(agent);
        new AlertDialog.Builder(this)
                .setTitle(t("newChat"))
                .setView(form)
                .setNegativeButton(t("cancel"), null)
                .setPositiveButton(t("create"), (dialog, which) -> {
                    Models.Agent selectedAgent = agentAt(agent.getSelectedItemPosition());
                    if (selectedAgent == null) {
                        toast(t("agentRequired"));
                        return;
                    }
                    String chatTitle = title.getText().toString().trim();
                    if (demoMode) {
                        try {
                            Models.ChatSession session = new Models.ChatSession(json(
                                    "id", "demo-chat-" + (chatSessionCache.size() + 1),
                                    "title", chatTitle.isEmpty() ? "Android QA Chat" : chatTitle,
                                    "agent_id", selectedAgent.id,
                                    "status", "active",
                                    "has_unread", false,
                                    "updated_at", "2026-05-08T09:15:00Z"
                            ));
                            chatSessionCache.add(0, session);
                            demoChatMessagesBySessionId.put(session.id, new ArrayList<>());
                            showChatMessages(session);
                        } catch (Exception error) {
                            toast(error.getMessage());
                        }
                        return;
                    }
                    run(() -> api.createChatSession(currentWorkspace.id, selectedAgent.id, chatTitle),
                            this::showChatMessages,
                            error -> toast(t("saveFailed") + ": " + error.getMessage()));
                }).show();
    }

    private void showChatMessages(Models.ChatSession session) {
        if (demoMode) {
            List<Models.ChatMessage> messages = demoChatMessagesBySessionId.get(session.id);
            renderChatMessages(session, messages == null ? Models.emptyList() : messages);
            return;
        }
        if (currentWorkspace == null) {
            showError(t("workspaceRequired"), this::restoreSession);
            return;
        }
        setContent(loadingView(t("loadingChat")));
        run(() -> api.chatMessages(currentWorkspace.id, session.id),
                messages -> renderChatMessages(session, messages),
                error -> showError(t("loadFailed") + "\n" + error.getMessage(), () -> showChatMessages(session)));
    }

    private void renderChatMessages(Models.ChatSession session, List<Models.ChatMessage> messages) {
        LinearLayout screen = vertical();
        LinearLayout body = vertical();
        body.setPadding(dp(14), dp(8), dp(14), dp(14));
        LinearLayout top = horizontal();
        top.setGravity(Gravity.CENTER_VERTICAL);
        Button back = smallButton("‹ " + t("chat"));
        back.setOnClickListener(v -> showChatSessions());
        top.addView(back);
        top.addView(screenTitle(session.title), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        body.addView(top);
        body.addView(label(assigneeName(session.agentId, "agent") + " · " + session.status, 12, MUTED, false));
        if (messages.isEmpty()) body.addView(empty(t("emptyMessages")));
        for (Models.ChatMessage message : messages) {
            LinearLayout row = card();
            row.addView(label("user".equals(message.role) ? t("you") : assigneeName(session.agentId, "agent"), 12, MUTED, true));
            LinearLayout md = vertical();
            MarkdownRenderer.render(this, md, message.content, TEXT, MUTED, BORDER);
            row.addView(md);
            if (message.failureReason != null && !message.failureReason.isEmpty()) row.addView(label(message.failureReason, 12, RED, false));
            body.addView(row);
        }
        screen.addView(scroll(body), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        screen.addView(chatInput(session), matchWrap());
        setContent(screen);
    }

    private View chatInput(Models.ChatSession session) {
        LinearLayout bar = horizontal();
        bar.setPadding(dp(12), dp(8), dp(12), dp(8));
        EditText input = input(t("messagePlaceholder"));
        input.setMaxLines(4);
        Button send = smallButton("↑");
        send.setTextSize(20);
        send.setTextColor(BLUE);
        send.setOnClickListener(v -> {
            String content = input.getText().toString().trim();
            if (content.isEmpty()) return;
            if (demoMode) {
                addDemoChatMessage(session, "user", content);
                addDemoChatMessage(session, "assistant", "收到，我会基于当前 workspace 继续处理。\n\n| 类型 | 状态 |\n|---|---|\n| Android | ready |");
                showChatMessages(session);
                return;
            }
            send.setEnabled(false);
            run(() -> {
                        api.sendChatMessage(currentWorkspace.id, session.id, content);
                        return true;
                    },
                    ignored -> showChatMessages(session),
                    error -> {
                        send.setEnabled(true);
                        toast(t("sendFailed") + ": " + error.getMessage());
                    });
        });
        input.setImeOptions(EditorInfo.IME_ACTION_SEND);
        input.setOnEditorActionListener((view, actionId, event) -> {
            boolean enterUp = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            if (actionId == EditorInfo.IME_ACTION_SEND || enterUp) {
                send.performClick();
                return true;
            }
            return false;
        });
        bar.addView(input, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        bar.addView(send);
        return bar;
    }

    private void showChatDialog() {
        LinearLayout body = vertical();
        body.setPadding(dp(10), dp(8), dp(10), dp(8));
        body.addView(label(t("chatHint"), 14, MUTED, false));
        new AlertDialog.Builder(this)
                .setTitle(t("chat"))
                .setView(body)
                .setPositiveButton(t("done"), null)
                .show();
    }

    private void handleDeepLink(Uri uri) {
        String host = uri.getHost();
        if ("issues".equals(host)) tab = "issues";
        else if ("inbox".equals(host)) tab = "inbox";
    }

    private void setupDemoData() {
        try {
            currentUser = new Models.User(json("id", "demo-user", "email", "demo@multica.ai", "name", "Demo Member"));
            currentWorkspace = new Models.Workspace(json("id", "demo-workspace", "name", "Mobile QA", "slug", "mobile-qa", "issue_prefix", "MQA"));
            workspaces = new ArrayList<>();
            workspaces.add(currentWorkspace);
            runtimeCache = new ArrayList<>();
            runtimeCache.add(new Models.Runtime(json("id", "demo-runtime", "name", "Default Runtime", "status", "online", "version", "2026.05")));
            agentCache = new ArrayList<>();
            agentCache.add(new Models.Agent(json("id", "demo-agent", "name", "Codex Mobile", "description", "Handles Android acceptance tasks.", "status", "idle")));
            skillCache = new ArrayList<>();
            skillCache.add(new Models.Skill(json("id", "demo-skill", "name", "Android QA", "description", "Checklist-driven Android acceptance.", "content", "Run build, install, inspect UI.")));
            autopilotCache = new ArrayList<>();
            autopilotCache.add(new Models.Autopilot(json("id", "demo-autopilot", "title", "Daily mobile triage", "description", "Creates follow-up issues for mobile regressions.", "status", "active", "assignee_id", "demo-agent")));
            labelCache = new ArrayList<>();
            labelCache.add(new Models.IssueLabel(json("id", "demo-label-1", "name", "android", "color", "#2563EB")));
            labelCache.add(new Models.IssueLabel(json("id", "demo-label-2", "name", "qa", "color", "#16A34A")));
            projectCache = new ArrayList<>();
            projectCache.add(new Models.Project(json("id", "demo-project", "name", "Android Parity", "description", "Match iOS and web mobile workflows.", "workspace_id", currentWorkspace.id, "created_at", "2026-05-08T08:00:00Z")));
            chatSessionCache = new ArrayList<>();
            chatSessionCache.add(new Models.ChatSession(json("id", "demo-chat-1", "title", "Android acceptance sync", "agent_id", "demo-agent", "status", "active", "has_unread", true, "updated_at", "2026-05-08T09:10:00Z")));
            demoIssues = new ArrayList<>();
            demoIssues.add(makeIssue("demo-issue-1", "MQA-101", 101, "Markdown comments render tables", "## 验收内容\n\n| 模块 | 状态 | 备注 |\n|---|---|---|\n| Comments | Pass | 表格可横向滚动 |\n| Code | Pass | `inline` 与代码块 |\n\n> 这条描述用于 Android 本地验收。", "in_progress", "high", "demo-user", "member", "demo-project", "2026-05-08T08:30:00Z"));
            demoIssues.add(makeIssue("demo-issue-2", "MQA-102", 102, "Agent activity opens transcript", "Agent activity should open without gray screen.", "todo", "medium", "demo-agent", "agent", "demo-project", "2026-05-08T07:30:00Z"));
            demoIssues.add(makeIssue("demo-issue-3", "MQA-103", 103, "Workspace scoped lists stay fast", "Switching tabs keeps the selected workspace.", "done", "low", null, null, null, "2026-05-08T06:00:00Z"));

            demoInbox = new ArrayList<>();
            demoInbox.add(new Models.InboxItem(json("id", "demo-inbox-1", "issue_id", "demo-issue-1", "issue_identifier", "MQA-101", "issue_title", "Markdown comments render tables", "read", false, "created_at", "2026-05-08T08:45:00Z")));
            demoInbox.add(new Models.InboxItem(json("id", "demo-inbox-2", "issue_id", "demo-issue-2", "issue_identifier", "MQA-102", "issue_title", "Agent activity opens transcript", "read", true, "created_at", "2026-05-08T08:15:00Z")));

            demoCommentsByIssueId.clear();
            List<Models.Comment> comments = new ArrayList<>();
            comments.add(new Models.Comment(json("id", "demo-comment-1", "content", "**最新进展**\n\n| Step | Result |\n|---|---|\n| Build | Passed |\n| Install | Passed |", "author_id", "demo-user", "author_type", "member", "author_name", "Demo Member", "issue_id", "demo-issue-1", "created_at", "2026-05-08T08:50:00Z")));
            comments.add(new Models.Comment(json("id", "demo-comment-2", "content", "Agent 已补充 `task-runs` 路径。", "author_id", "demo-agent", "author_type", "agent", "author_name", "Codex Mobile", "issue_id", "demo-issue-1", "created_at", "2026-05-08T08:40:00Z")));
            demoCommentsByIssueId.put("demo-issue-1", comments);

            demoRunsByIssueId.clear();
            List<Models.AgentTask> runs = new ArrayList<>();
            runs.add(new Models.AgentTask(json("id", "demo-run-1", "issue_id", "demo-issue-1", "agent_id", "demo-agent", "status", "completed", "started_at", "2026-05-08T08:35:00Z", "completed_at", "2026-05-08T08:39:00Z")));
            demoRunsByIssueId.put("demo-issue-1", runs);

            demoChatMessagesBySessionId.clear();
            List<Models.ChatMessage> chatMessages = new ArrayList<>();
            chatMessages.add(new Models.ChatMessage(json("id", "demo-chat-message-1", "chat_session_id", "demo-chat-1", "role", "user", "content", "帮我检查 Android 工程是否独立。", "created_at", "2026-05-08T09:08:00Z")));
            chatMessages.add(new Models.ChatMessage(json("id", "demo-chat-message-2", "chat_session_id", "demo-chat-1", "role", "assistant", "content", "已确认 Android 在 `/Users/park0er/coding/MulticaAndroid`，不会和 iOS 工程混放。", "created_at", "2026-05-08T09:09:00Z")));
            demoChatMessagesBySessionId.put("demo-chat-1", chatMessages);
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private JSONObject json(Object... pairs) throws Exception {
        JSONObject object = new JSONObject();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            Object value = pairs[i + 1];
            object.put(String.valueOf(pairs[i]), value == null ? JSONObject.NULL : value);
        }
        return object;
    }

    private Models.Issue makeIssue(String id, String identifier, int number, String title, String description,
                                   String status, String priority, String assigneeId, String assigneeType,
                                   String projectId, String updatedAt) throws Exception {
        return new Models.Issue(json(
                "id", id,
                "identifier", identifier,
                "number", number,
                "title", title,
                "description", description,
                "status", status,
                "priority", priority,
                "assignee_id", assigneeId,
                "assignee_type", assigneeType,
                "project_id", projectId,
                "workspace_id", currentWorkspace.id,
                "created_at", "2026-05-08T06:00:00Z",
                "updated_at", updatedAt
        ));
    }

    private Models.Issue findDemoIssue(String issueId) {
        for (Models.Issue issue : demoIssues) {
            if (issue.id.equals(issueId)) return issue;
        }
        return null;
    }

    private void upsertDemoIssue(String id, String title, String description, String projectId,
                                 String status, String priority, Models.Assignee assignee) {
        try {
            Models.Issue existing = findDemoIssue(id);
            String identifier = existing == null ? "MQA-" + (100 + demoIssues.size() + 1) : existing.identifier;
            int number = existing == null ? 100 + demoIssues.size() + 1 : existing.number;
            Models.Issue updated = makeIssue(
                    id,
                    identifier,
                    number,
                    title,
                    description,
                    status,
                    priority,
                    assignee == null ? null : assignee.id,
                    assignee == null ? null : assignee.type,
                    projectId,
                    "2026-05-08T09:00:00Z"
            );
            if (existing == null) {
                demoIssues.add(updated);
            } else {
                for (int i = 0; i < demoIssues.size(); i++) {
                    if (demoIssues.get(i).id.equals(id)) {
                        demoIssues.set(i, updated);
                        break;
                    }
                }
            }
        } catch (Exception error) {
            toast(error.getMessage());
        }
    }

    private void addDemoComment(String issueId, String content) {
        try {
            List<Models.Comment> comments = demoCommentsByIssueId.computeIfAbsent(issueId, key -> new ArrayList<>());
            comments.add(0, new Models.Comment(json(
                    "id", "demo-comment-" + System.currentTimeMillis(),
                    "content", content,
                    "author_id", "demo-user",
                    "author_type", "member",
                    "author_name", "Demo Member",
                    "issue_id", issueId,
                    "created_at", "2026-05-08T09:05:00Z"
            )));
        } catch (Exception error) {
            toast(error.getMessage());
        }
    }

    private void addDemoChatMessage(Models.ChatSession session, String role, String content) {
        try {
            List<Models.ChatMessage> messages = demoChatMessagesBySessionId.computeIfAbsent(session.id, key -> new ArrayList<>());
            messages.add(new Models.ChatMessage(json(
                    "id", "demo-chat-message-" + System.currentTimeMillis() + "-" + role,
                    "chat_session_id", session.id,
                    "role", role,
                    "content", content,
                    "created_at", "2026-05-08T09:20:00Z"
            )));
        } catch (Exception error) {
            toast(error.getMessage());
        }
    }

    private List<Models.Project> safeProjects() {
        if (currentWorkspace == null) return Models.emptyList();
        try {
            return api.projects(currentWorkspace.id, 100, 0).items;
        } catch (Exception ignored) {
            return projectCache;
        }
    }

    private List<Models.Squad> safeSquads() {
        try { return api.squads(currentWorkspace.id); } catch (Exception e) { return new ArrayList<>(); }
    }

    private List<Models.Agent> safeAgents() {
        if (currentWorkspace == null) return Models.emptyList();
        try {
            return api.agents(currentWorkspace.id);
        } catch (Exception ignored) {
            return agentCache;
        }
    }

    private List<Models.Member> safeMembers() {
        if (currentWorkspace == null) return Models.emptyList();
        try {
            return api.members(currentWorkspace.id);
        } catch (Exception ignored) {
            return memberCache;
        }
    }

    private List<Models.Assignee> assignees(boolean includeEmpty) {
        if (memberCache.isEmpty()) memberCache = safeMembers();
        if (agentCache.isEmpty()) agentCache = safeAgents();
        return Models.issueAssignees(includeEmpty, t("unassigned"), currentUser, memberCache, agentCache, squadCache);
    }

    private String assigneeName(String id, String type) {
        if (id == null || id.isEmpty()) return t("unassigned");
        if (currentUser != null && currentUser.id.equals(id)) return currentUser.name;
        for (Models.Member member : memberCache) if (member.id.equals(id)) return member.displayName;
        for (Models.Agent agent : agentCache) if (agent.id.equals(id)) return agent.name;
        String prefix;
        if ("agent".equals(type)) prefix = t("agent");
        else if ("squad".equals(type)) prefix = t("squad");
        else prefix = t("member");
        return prefix + " " + Models.shortId(id);
    }

    private List<String> assigneeLabels(boolean includeEmpty) {
        List<String> labels = new ArrayList<>();
        for (Models.Assignee assignee : assignees(includeEmpty)) labels.add(assignee.name);
        return labels;
    }

    private Models.Assignee assigneeAt(int index, boolean includeEmpty) {
        List<Models.Assignee> list = assignees(includeEmpty);
        if (index < 0 || index >= list.size()) return null;
        Models.Assignee assignee = list.get(index);
        return assignee.id == null ? null : assignee;
    }

    private List<String> agentLabels() {
        List<String> labels = new ArrayList<>();
        for (Models.Agent agent : agentCache) labels.add(agent.name);
        if (labels.isEmpty()) labels.add(t("emptyAgents"));
        return labels;
    }

    private Models.Agent agentAt(int index) {
        if (index < 0 || index >= agentCache.size()) return null;
        return agentCache.get(index);
    }

    private void selectAssignee(Spinner spinner, String assigneeId, String assigneeType) {
        List<Models.Assignee> list = assignees(true);
        for (int i = 0; i < list.size(); i++) {
            Models.Assignee a = list.get(i);
            if (assigneeId == null && a.id == null || assigneeId != null && assigneeId.equals(a.id) && (assigneeType == null || assigneeType.equals(a.type))) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private List<String> projectLabels(boolean includeEmpty) {
        List<String> labels = new ArrayList<>();
        if (includeEmpty) labels.add(t("noProject"));
        for (Models.Project project : projectCache) labels.add(project.name);
        return labels;
    }

    private String projectIdAt(int index, boolean includeEmpty) {
        int real = includeEmpty ? index - 1 : index;
        if (real < 0 || real >= projectCache.size()) return null;
        return projectCache.get(real).id;
    }

    private void selectProject(Spinner spinner, String projectId) {
        if (projectId == null) {
            spinner.setSelection(0);
            return;
        }
        for (int i = 0; i < projectCache.size(); i++) {
            if (projectId.equals(projectCache.get(i).id)) {
                spinner.setSelection(i + 1);
                return;
            }
        }
    }

    private List<String> statusLabels() {
        List<String> labels = new ArrayList<>();
        for (String status : Models.STATUS_VALUES) labels.add(Models.statusLabel(status, zh));
        return labels;
    }

    private List<String> priorityLabels() {
        List<String> labels = new ArrayList<>();
        for (String priority : Models.PRIORITY_VALUES) labels.add(Models.priorityLabel(priority, zh));
        return labels;
    }

    private List<String> runtimeLabels() {
        List<String> labels = new ArrayList<>();
        if (runtimeCache.isEmpty()) {
            labels.add(t("noRuntime"));
        } else {
            for (Models.Runtime runtime : runtimeCache) {
                labels.add(runtime.name + (runtime.status == null || runtime.status.isEmpty() ? "" : " · " + runtime.status));
            }
        }
        return labels;
    }

    private String runtimeIdAt(int index) {
        if (index < 0 || index >= runtimeCache.size()) return null;
        return runtimeCache.get(index).id;
    }

    private void selectValue(Spinner spinner, String[] values, String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private Spinner spinner(List<String> values) {
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values));
        return spinner;
    }

    private void run(Runnable task, Runnable success, Failure failure) {
        run(() -> {
            task.run();
            return true;
        }, ignored -> success.run(), failure);
    }

    private <T> void run(Callable<T> task, Success<T> success, Failure failure) {
        try {
            worker().submit(() -> {
                try {
                    T value = task.call();
                    main.post(() -> {
                        if (!isFinishing() && !isDestroyed()) success.accept(value);
                    });
                } catch (Exception error) {
                    main.post(() -> {
                        if (!isFinishing() && !isDestroyed()) failure.accept(error);
                    });
                }
            });
        } catch (RejectedExecutionException error) {
            main.post(() -> {
                if (!isFinishing() && !isDestroyed()) failure.accept(error);
            });
        }
    }

    private synchronized ExecutorService worker() {
        if (executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newFixedThreadPool(4);
        }
        return executor;
    }

    private void setContent(View view) {
        if (content == null) {
            setContentView(view);
            return;
        }
        content.removeAllViews();
        content.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showLoading(String text) {
        setContentView(loadingView(text));
    }

    private View loadingView(String text) {
        LinearLayout box = vertical();
        box.setGravity(Gravity.CENTER);
        box.setBackgroundColor(APP_BG);
        box.addView(label(text, 15, MUTED, false));
        return box;
    }

    private void showError(String message, Runnable retry) {
        LinearLayout body = vertical();
        body.setGravity(Gravity.CENTER);
        body.setPadding(dp(24), dp(24), dp(24), dp(24));
        body.addView(label(message, 14, RED, false));
        body.addView(spacer(12));
        Button button = button(t("retry"), BLUE, 0xFFFFFFFF);
        button.setOnClickListener(v -> retry.run());
        body.addView(button, matchWrap());
        setContent(body);
    }

    private TextView screenTitle(String value) {
        TextView tv = label(value, 28, TEXT, true);
        tv.setPadding(0, dp(8), 0, dp(12));
        return tv;
    }

    private TextView sectionTitle(String value) {
        TextView tv = label(value, 17, TEXT, true);
        tv.setPadding(0, dp(18), 0, dp(8));
        return tv;
    }

    private TextView empty(String value) {
        TextView tv = label(value, 14, MUTED, false);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(16), dp(24), dp(16), dp(24));
        return tv;
    }

    private LinearLayout card() {
        LinearLayout card = vertical();
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(roundedStroke(SURFACE, BORDER, 14, 1));
        card.setElevation(dp(1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(6), 0, dp(6));
        card.setLayoutParams(params);
        return card;
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout horizontal() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private ScrollView scroll(View child) {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(APP_BG);
        scroll.addView(child);
        return scroll;
    }

    private TextView label(String value, int sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value == null ? "" : value);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setLineSpacing(dp(1), 1.10f);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    private Button button(String text, int background, int foreground) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(foreground);
        button.setBackground(rounded(background, 13));
        button.setPadding(dp(12), dp(7), dp(12), dp(7));
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(dp(34));
        button.setMinimumHeight(dp(34));
        button.setStateListAnimator(null);
        return button;
    }

    private Button smallButton(String text) {
        Button button = pillButton(text, SOFT_BLUE, BLUE);
        button.setMinWidth(dp(44));
        button.setMinimumHeight(dp(36));
        return button;
    }

    private Button pillButton(String text, int background, int foreground) {
        Button button = button(text, background, foreground);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setMinWidth(dp(36));
        button.setMinimumWidth(dp(36));
        return button;
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(15);
        input.setSingleLine(false);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setBackground(roundedStroke(0xFFFFFFFF, BORDER, 12, 1));
        return input;
    }

    private View spacer(int dp) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(dp)));
        return view;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private GradientDrawable rounded(int color, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable roundedStroke(int fill, int stroke, float radiusDp, int strokeWidthDp) {
        GradientDrawable drawable = rounded(fill, radiusDp);
        if (strokeWidthDp > 0) drawable.setStroke(dp(strokeWidthDp), stroke);
        return drawable;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int dp(int value) {
        return MarkdownRenderer.dp(this, value);
    }

    private String shortDate(String value) {
        if (value == null) return "";
        return value.length() > 10 ? value.substring(0, Math.min(value.length(), 19)).replace("T", " ") : value;
    }

    private void toast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_LONG).show();
    }

    private String t(String key) {
        if (zh) {
            switch (key) {
                case "loading": return "加载中...";
                case "sessionExpired": return "登录已过期，请重新登录";
                case "signIn": return "登录 Multi-Casual";
                case "signInHint": return "输入邮箱获取登录验证码";
                case "continue": return "继续";
                case "emailRequired": return "请输入邮箱";
                case "sendCodeFailed": return "验证码发送失败";
                case "otpTitle": return "输入验证码";
                case "verify": return "验证并登录";
                case "back": return "返回";
                case "otpRequired": return "请输入验证码";
                case "otpFailed": return "验证码无效或已过期";
                case "inbox": return "收件箱";
                case "issues": return "Issues";
                case "projects": return "项目";
                case "agents": return "Agent";
                case "settings": return "设置";
                case "chat": return "聊天";
                case "loadingChat": return "正在加载聊天...";
                case "newChat": return "新建聊天";
                case "messagePlaceholder": return "发送消息，支持 Markdown...";
                case "sendFailed": return "发送失败";
                case "you": return "你";
                case "agentRequired": return "请先选择 Agent";
                case "loadAgentsFirst": return "请先加载 Agent 列表";
                case "loadingInbox": return "正在加载收件箱...";
                case "loadingIssues": return "正在加载 Issues...";
                case "loadingIssue": return "正在加载详情...";
                case "loadingProjects": return "正在加载项目...";
                case "loadingAgents": return "正在加载 Agent...";
                case "emptyInbox": return "暂无收件箱消息";
                case "emptyIssues": return "暂无 Issues";
                case "emptyProjects": return "暂无项目";
                case "emptyAgents": return "暂无 Agent";
                case "emptyChat": return "暂无聊天会话";
                case "emptyMessages": return "暂无消息";
                case "emptyComments": return "暂无评论";
                case "emptyActivity": return "暂无 Agent 活动";
                case "loadFailed": return "加载失败";
                case "workspaceRequired": return "缺少 workspace_id，请先选择工作区";
                case "retry": return "重试";
                case "list": return "列表";
                case "board": return "看板";
                case "latestProgress": return "最新进展与评论";
                case "agentActivity": return "Agent 工作详情";
                case "agentRun": return "Agent 运行";
                case "details": return "详情";
                case "transcript": return "Agent 记录";
                case "done": return "完成";
                case "commentPlaceholder": return "添加评论，支持 Markdown...";
                case "commentFailed": return "评论发送失败";
                case "edit": return "编辑";
                case "editIssue": return "编辑 Issue";
                case "newIssue": return "新建 Issue";
                case "title": return "标题";
                case "description": return "描述";
                case "project": return "项目";
                case "status": return "状态";
                case "priority": return "优先级";
                case "assignee": return "负责人";
                case "cancel": return "取消";
                case "save": return "保存";
                case "create": return "创建";
                case "titleRequired": return "请输入标题";
                case "saveFailed": return "保存失败";
                case "unassigned": return "未分配";
                case "member": return "成员";
                case "agent": return "Agent";
                case "squad": return "小队";
                case "archive": return "归档";
                case "archived": return "已归档";
                case "archiveAgent": return "归档 Agent";
                case "noProject": return "不关联项目";
                case "workspace": return "工作区";
                case "workspaces": return "工作区";
                case "logout": return "退出登录";
                case "name": return "名称";
                case "newAgent": return "新建 Agent";
                case "myIssues": return "我的 Issues";
                case "autopilots": return "Autopilots";
                case "runtimes": return "运行时";
                case "skills": return "技能";
                case "runtime": return "运行时";
                case "noRuntime": return "没有可用运行时";
                case "runtimeRequired": return "请先选择运行时";
                case "workspaceDetails": return "Workspace 详情";
                case "members": return "成员";
                case "notifications": return "通知";
                case "apiTokens": return "API Tokens";
                case "labels": return "标签";
                case "newLabel": return "新建标签";
                case "emptyLabels": return "暂无标签";
                case "color": return "颜色";
                case "feedback": return "反馈";
                case "message": return "消息";
                case "feedbackMessage": return "请输入反馈内容";
                case "feedbackRequired": return "请输入反馈内容";
                case "feedbackSent": return "反馈已发送";
                case "send": return "发送";
                case "empty": return "暂无内容";
                case "loadingAutopilots": return "正在加载 Autopilots...";
                case "loadingRuntimes": return "正在加载运行时...";
                case "loadingSkills": return "正在加载技能...";
                case "emptyAutopilots": return "暂无 Autopilot";
                case "emptyRuntimes": return "暂无运行时";
                case "emptySkills": return "暂无技能";
                case "content": return "内容";
                case "newSkill": return "新建技能";
                case "configureHint": return "工作区、成员、通知、Token、标签、Agent、Autopilot、运行时、技能和反馈入口已按 iOS 设置页结构展开。";
                case "chatHint": return "聊天入口已常驻在收件箱左上角，并支持会话列表、消息列表和 Markdown 消息发送。";
            }
        } else {
            switch (key) {
                case "loading": return "Loading...";
                case "sessionExpired": return "Session expired. Please sign in again.";
                case "signIn": return "Sign in to Multi-Casual";
                case "signInHint": return "Enter your email to get a login code";
                case "continue": return "Continue";
                case "emailRequired": return "Email is required";
                case "sendCodeFailed": return "Failed to send code";
                case "otpTitle": return "Enter code";
                case "verify": return "Verify and sign in";
                case "back": return "Back";
                case "otpRequired": return "Enter the code";
                case "otpFailed": return "Invalid or expired code";
                case "inbox": return "Inbox";
                case "issues": return "Issues";
                case "projects": return "Projects";
                case "agents": return "Agents";
                case "settings": return "Settings";
                case "chat": return "Chat";
                case "loadingChat": return "Loading chat...";
                case "newChat": return "New Chat";
                case "messagePlaceholder": return "Send a Markdown message...";
                case "sendFailed": return "Send failed";
                case "you": return "You";
                case "agentRequired": return "Choose an agent first";
                case "loadAgentsFirst": return "Load Agents first";
                case "loadingInbox": return "Loading inbox...";
                case "loadingIssues": return "Loading issues...";
                case "loadingIssue": return "Loading issue...";
                case "loadingProjects": return "Loading projects...";
                case "loadingAgents": return "Loading agents...";
                case "emptyInbox": return "No inbox items";
                case "emptyIssues": return "No issues";
                case "emptyProjects": return "No projects";
                case "emptyAgents": return "No agents";
                case "emptyChat": return "No chat sessions";
                case "emptyMessages": return "No messages";
                case "emptyComments": return "No comments";
                case "emptyActivity": return "No agent activity";
                case "loadFailed": return "Load failed";
                case "workspaceRequired": return "workspace_id is required. Select a workspace first.";
                case "retry": return "Retry";
                case "list": return "List";
                case "board": return "Board";
                case "latestProgress": return "Latest Progress & Comments";
                case "agentActivity": return "Agent Work Details";
                case "agentRun": return "Agent run";
                case "details": return "Details";
                case "transcript": return "Agent Transcript";
                case "done": return "Done";
                case "commentPlaceholder": return "Add a Markdown comment...";
                case "commentFailed": return "Failed to post comment";
                case "edit": return "Edit";
                case "editIssue": return "Edit Issue";
                case "newIssue": return "New Issue";
                case "title": return "Title";
                case "description": return "Description";
                case "project": return "Project";
                case "status": return "Status";
                case "priority": return "Priority";
                case "assignee": return "Assignee";
                case "cancel": return "Cancel";
                case "save": return "Save";
                case "create": return "Create";
                case "titleRequired": return "Title is required";
                case "saveFailed": return "Save failed";
                case "unassigned": return "Unassigned";
                case "member": return "Member";
                case "agent": return "Agent";
                case "squad": return "Squad";
                case "archive": return "Archive";
                case "archived": return "Archived";
                case "archiveAgent": return "Archive Agent";
                case "noProject": return "No project";
                case "workspace": return "Workspace";
                case "workspaces": return "Workspaces";
                case "logout": return "Log Out";
                case "name": return "Name";
                case "newAgent": return "New Agent";
                case "myIssues": return "My Issues";
                case "autopilots": return "Autopilots";
                case "runtimes": return "Runtimes";
                case "skills": return "Skills";
                case "runtime": return "Runtime";
                case "noRuntime": return "No available runtime";
                case "runtimeRequired": return "Choose a runtime first";
                case "workspaceDetails": return "Workspace Details";
                case "members": return "Members";
                case "notifications": return "Notifications";
                case "apiTokens": return "API Tokens";
                case "labels": return "Labels";
                case "newLabel": return "New Label";
                case "emptyLabels": return "No labels";
                case "color": return "Color";
                case "feedback": return "Feedback";
                case "message": return "Message";
                case "feedbackMessage": return "Enter feedback";
                case "feedbackRequired": return "Feedback message is required";
                case "feedbackSent": return "Feedback sent";
                case "send": return "Send";
                case "empty": return "No content";
                case "loadingAutopilots": return "Loading autopilots...";
                case "loadingRuntimes": return "Loading runtimes...";
                case "loadingSkills": return "Loading skills...";
                case "emptyAutopilots": return "No autopilots";
                case "emptyRuntimes": return "No runtimes";
                case "emptySkills": return "No skills";
                case "content": return "Content";
                case "newSkill": return "New Skill";
                case "configureHint": return "Workspaces, members, notifications, tokens, labels, agents, autopilots, runtimes, skills, and feedback follow the iOS Settings structure.";
                case "chatHint": return "Chat is pinned at the top-left of Inbox and supports sessions, messages, and Markdown sends.";
            }
        }
        return key;
    }

    private static final class SessionData {
        Models.User user;
        List<Models.Workspace> workspaces;
    }

    private static final class IssuesData {
        List<Models.Issue> issues;
        List<Models.Project> projects;
        List<Models.Agent> agents;
    }

    private static final class AgentsData {
        List<Models.Agent> agents;
        List<Models.Runtime> runtimes;
    }
}
