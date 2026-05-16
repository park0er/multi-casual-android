package ai.multica.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class ApiClient {
    interface Parser<T> {
        T parse(JSONObject json) throws Exception;
    }

    private static final String BASE = "https://api.multica.ai";
    private final AuthStore authStore;

    ApiClient(AuthStore authStore) {
        this.authStore = authStore;
    }

    void sendCode(String email) throws Exception {
        JSONObject body = new JSONObject();
        body.put("email", email);
        requestObject("POST", "/auth/send-code", null, body);
    }

    String verifyCode(String email, String code) throws Exception {
        JSONObject body = new JSONObject();
        body.put("email", email);
        body.put("code", code);
        return requestObject("POST", "/auth/verify-code", null, body).optString("token");
    }

    Models.User me() throws Exception {
        return new Models.User(requestObject("GET", "/api/me", null, null));
    }

    Models.User updateMe(String name, String avatarUrl, String language) throws Exception {
        JSONObject body = new JSONObject();
        if (name != null) body.put("name", name);
        if (avatarUrl != null) body.put("avatar_url", avatarUrl);
        if (language != null) body.put("language", language);
        return new Models.User(requestObject("PATCH", "/api/me", null, body));
    }

    Models.User markOnboardingComplete(String completionPath) throws Exception {
        JSONObject body = new JSONObject();
        if (completionPath != null && !completionPath.trim().isEmpty()) {
            body.put("completion_path", completionPath.trim());
        }
        return new Models.User(requestObject("POST", "/api/me/onboarding/complete", null, body));
    }

    Models.User joinCloudWaitlist(String email, String reason) throws Exception {
        JSONObject body = new JSONObject();
        body.put("email", email);
        if (reason != null && !reason.trim().isEmpty()) body.put("reason", reason.trim());
        return new Models.User(requestObject("POST", "/api/me/onboarding/cloud-waitlist", null, body));
    }

    Models.User patchOnboarding(JSONObject questionnaire) throws Exception {
        JSONObject body = new JSONObject();
        body.put("questionnaire", questionnaire == null ? new JSONObject() : questionnaire);
        return new Models.User(requestObject("PATCH", "/api/me/onboarding", null, body));
    }

    Models.StarterContentImportResponse importStarterContent(JSONObject payload) throws Exception {
        return new Models.StarterContentImportResponse(
                requestObject("POST", "/api/me/starter-content/import", null, payload == null ? new JSONObject() : payload));
    }

    Models.User dismissStarterContent(String workspaceId) throws Exception {
        JSONObject body = new JSONObject();
        if (workspaceId != null && !workspaceId.trim().isEmpty()) body.put("workspace_id", workspaceId.trim());
        return new Models.User(requestObject("POST", "/api/me/starter-content/dismiss", null, body));
    }

    List<Models.Workspace> workspaces() throws Exception {
        JSONObject json = requestObject("GET", "/api/workspaces", null, null);
        return parseArray(extractArray(json, "workspaces"), Models.Workspace::new);
    }

    Models.Workspace workspace(String workspaceId) throws Exception {
        return new Models.Workspace(unwrap(requestObject("GET", "/api/workspaces/" + encPath(workspaceId),
                query(null, "workspace_id", workspaceId), null), "workspace"));
    }

    Models.Workspace createWorkspace(String name, String slug, String description, String context) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("slug", slug);
        if (description == null || description.isEmpty()) body.put("description", JSONObject.NULL);
        else body.put("description", description);
        if (context == null || context.isEmpty()) body.put("context", JSONObject.NULL);
        else body.put("context", context);
        return new Models.Workspace(unwrap(requestObject("POST", "/api/workspaces", null, body), "workspace"));
    }

    Models.Workspace updateWorkspace(String workspaceId, String name, String description, String context) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        if (description == null || description.isEmpty()) body.put("description", JSONObject.NULL);
        else body.put("description", description);
        if (context == null || context.isEmpty()) body.put("context", JSONObject.NULL);
        else body.put("context", context);
        return new Models.Workspace(unwrap(requestObject("PATCH", "/api/workspaces/" + encPath(workspaceId),
                query(null, "workspace_id", workspaceId), body), "workspace"));
    }

    Models.Workspace updateWorkspaceRepos(String workspaceId, List<String> repoUrls) throws Exception {
        JSONArray repos = new JSONArray();
        if (repoUrls != null) {
            for (String url : repoUrls) {
                if (url == null || url.trim().isEmpty()) continue;
                repos.put(new JSONObject().put("url", url.trim()));
            }
        }
        JSONObject body = new JSONObject();
        body.put("repos", repos);
        return new Models.Workspace(unwrap(requestObject("PATCH", "/api/workspaces/" + encPath(workspaceId),
                query(null, "workspace_id", workspaceId), body), "workspace"));
    }

    Models.Workspace updateWorkspaceCoAuthoredBy(String workspaceId, boolean enabled) throws Exception {
        JSONObject settings = new JSONObject(workspace(workspaceId).settings.toString());
        settings.put("co_authored_by_enabled", enabled);
        JSONObject body = new JSONObject();
        body.put("settings", settings);
        return new Models.Workspace(unwrap(requestObject("PATCH", "/api/workspaces/" + encPath(workspaceId),
                query(null, "workspace_id", workspaceId), body), "workspace"));
    }

    void deleteWorkspace(String workspaceId) throws Exception {
        requestObject("DELETE", "/api/workspaces/" + encPath(workspaceId),
                query(null, "workspace_id", workspaceId), null);
    }

    void leaveWorkspace(String workspaceId) throws Exception {
        requestObject("POST", "/api/workspaces/" + encPath(workspaceId) + "/leave",
                query(null, "workspace_id", workspaceId), null);
    }

    Models.Page<Models.InboxItem> inbox(String workspaceId, int limit, int offset) throws Exception {
        JSONObject json = requestObject("GET", "/api/inbox", query(null, "workspace_id", workspaceId, "limit", limit, "offset", offset), null);
        if (offset == 0) {
            authStore.saveInboxCache(workspaceId, json.toString());
        }
        return parsePage(json, "inbox", Models.InboxItem::new);
    }

    Models.Page<Models.InboxItem> cachedInbox(String workspaceId) throws Exception {
        String cached = authStore.inboxCache(workspaceId);
        if (cached == null || cached.trim().isEmpty()) return null;
        return parsePage(new JSONObject(cached), "inbox", Models.InboxItem::new);
    }

    Models.InboxItem markInboxRead(String workspaceId, String id) throws Exception {
        return new Models.InboxItem(requestObject("POST", "/api/inbox/" + encPath(id) + "/read",
                query(null, "workspace_id", workspaceId), null));
    }

    Models.InboxItem archiveInbox(String workspaceId, String id) throws Exception {
        return new Models.InboxItem(requestObject("POST", "/api/inbox/" + encPath(id) + "/archive",
                query(null, "workspace_id", workspaceId), null));
    }

    int markAllInboxRead(String workspaceId) throws Exception {
        return requestObject("POST", "/api/inbox/mark-all-read",
                query(null, "workspace_id", workspaceId), null).optInt("count", 0);
    }

    int archiveAllReadInbox(String workspaceId) throws Exception {
        return requestObject("POST", "/api/inbox/archive-all-read",
                query(null, "workspace_id", workspaceId), null).optInt("count", 0);
    }

    int archiveAllInbox(String workspaceId) throws Exception {
        return requestObject("POST", "/api/inbox/archive-all",
                query(null, "workspace_id", workspaceId), null).optInt("count", 0);
    }

    int archiveCompletedInbox(String workspaceId) throws Exception {
        return requestObject("POST", "/api/inbox/archive-completed",
                query(null, "workspace_id", workspaceId), null).optInt("count", 0);
    }

    Models.Page<Models.Issue> issues(String workspaceId, int limit, int offset) throws Exception {
        return issues(workspaceId, limit, offset, null);
    }

    Models.Page<Models.Issue> issues(String workspaceId, int limit, int offset, String assigneeId) throws Exception {
        return issues(workspaceId, limit, offset, assigneeId, null);
    }

    Models.Page<Models.Issue> issues(String workspaceId, int limit, int offset, String assigneeId, String status) throws Exception {
        return issuesFiltered(workspaceId, limit, offset, assigneeId, status, null, Collections.emptyList());
    }

    Models.Page<Models.Issue> issuesFiltered(String workspaceId, int limit, int offset, String assigneeId, String status,
                                             String creatorId, List<String> assigneeIds) throws Exception {
        JSONObject query = query(null, "workspace_id", workspaceId, "limit", limit, "offset", offset);
        if (assigneeId != null && !assigneeId.isEmpty()) query.put("assignee_id", assigneeId);
        if (status != null && !status.isEmpty()) query.put("status", status);
        if (creatorId != null && !creatorId.isEmpty()) query.put("creator_id", creatorId);
        if (assigneeIds != null && !assigneeIds.isEmpty()) query.put("assignee_ids", String.join(",", assigneeIds));
        JSONObject json = requestObject("GET", "/api/issues", query, null);
        return parsePage(json, "issues", Models.Issue::new);
    }

    Models.Issue issue(String id) throws Exception {
        return new Models.Issue(requestObject("GET", "/api/issues/" + encPath(id), null, null));
    }

    Models.Issue issue(String id, String workspaceId) throws Exception {
        return new Models.Issue(requestObject("GET", "/api/issues/" + encPath(id),
                query(null, "workspace_id", workspaceId), null));
    }

    Models.Page<Models.Issue> projectIssues(String workspaceId, String projectId, int limit, int offset) throws Exception {
        return projectIssues(workspaceId, projectId, null, limit, offset);
    }

    Models.Page<Models.Issue> projectIssues(String workspaceId, String projectId, String status, int limit, int offset) throws Exception {
        JSONObject query = query(null, "workspace_id", workspaceId, "project_id", projectId, "limit", limit, "offset", offset);
        if (status != null && !status.isEmpty()) query.put("status", status);
        JSONObject json = requestObject("GET", "/api/issues", query, null);
        return parsePage(json, "issues", Models.Issue::new);
    }

    List<Models.Issue> searchIssues(String workspaceId, String text, int limit) throws Exception {
        JSONObject query = query(null,
                "workspace_id", workspaceId,
                "q", text,
                "limit", limit,
                "include_closed", "true");
        JSONObject json = requestObject("GET", "/api/issues/search", query, null);
        return parseArray(extractArray(json, "issues"), Models.Issue::new);
    }

    Models.Issue createIssue(String title, String description, String workspaceId, String projectId,
                             String status, String priority, Models.Assignee assignee) throws Exception {
        return createIssue(title, description, workspaceId, projectId, status, priority, assignee, null);
    }

    Models.Issue createIssue(String title, String description, String workspaceId, String projectId,
                             String status, String priority, Models.Assignee assignee, String dueDate) throws Exception {
        return createIssue(title, description, workspaceId, projectId, status, priority, assignee, dueDate, null);
    }

    Models.Issue createIssue(String title, String description, String workspaceId, String projectId,
                             String status, String priority, Models.Assignee assignee, String dueDate,
                             String parentIssueId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("description", description == null || description.isEmpty() ? JSONObject.NULL : description);
        body.put("workspace_id", workspaceId);
        if (projectId != null) body.put("project_id", projectId);
        if (parentIssueId != null && !parentIssueId.isEmpty()) body.put("parent_issue_id", parentIssueId);
        if (status != null) body.put("status", status);
        if (priority != null) body.put("priority", priority);
        if (dueDate != null && !dueDate.isEmpty()) body.put("due_date", dueDate); else body.put("due_date", JSONObject.NULL);
        if (assignee != null && assignee.id != null) {
            body.put("assignee_id", assignee.id);
            body.put("assignee_type", assignee.type);
        }
        return new Models.Issue(requestObject("POST", "/api/issues", query(null, "workspace_id", workspaceId), body));
    }

    List<Models.Issue> childIssues(String workspaceId, String issueId) throws Exception {
        JSONObject json = requestObject("GET", "/api/issues/" + encPath(issueId) + "/children",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "issues"), Models.Issue::new);
    }

    Models.Issue updateIssue(Models.Issue issue, String title, String description, String projectId,
                             String status, String priority, Models.Assignee assignee) throws Exception {
        return updateIssue(issue, title, description, projectId, status, priority, assignee, issue.dueDate);
    }

    Models.Issue updateIssue(Models.Issue issue, String title, String description, String projectId,
                             String status, String priority, Models.Assignee assignee, String dueDate) throws Exception {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("description", description == null || description.isEmpty() ? JSONObject.NULL : description);
        body.put("status", status);
        body.put("priority", priority);
        if (projectId == null) body.put("project_id", JSONObject.NULL); else body.put("project_id", projectId);
        if (dueDate == null || dueDate.isEmpty()) body.put("due_date", JSONObject.NULL); else body.put("due_date", dueDate);
        if (assignee == null || assignee.id == null) {
            body.put("assignee_id", JSONObject.NULL);
            body.put("assignee_type", JSONObject.NULL);
        } else {
            body.put("assignee_id", assignee.id);
            body.put("assignee_type", assignee.type);
        }
        return new Models.Issue(requestObject("PUT", "/api/issues/" + encPath(issue.id),
                query(null, "workspace_id", issue.workspaceId), body));
    }

    Models.Issue updateIssueParent(Models.Issue issue, String parentIssueId) throws Exception {
        JSONObject body = new JSONObject();
        if (parentIssueId == null || parentIssueId.isEmpty()) {
            body.put("parent_issue_id", JSONObject.NULL);
        } else {
            body.put("parent_issue_id", parentIssueId);
        }
        return new Models.Issue(requestObject("PUT", "/api/issues/" + encPath(issue.id),
                query(null, "workspace_id", issue.workspaceId), body));
    }

    Models.Issue updateIssueStatus(String workspaceId, String issueId, String status) throws Exception {
        JSONObject body = new JSONObject();
        body.put("status", status);
        return new Models.Issue(requestObject("PUT", "/api/issues/" + encPath(issueId),
                query(null, "workspace_id", workspaceId), body));
    }

    Models.QuickCreateTask quickCreateIssue(String workspaceId, String agentId, String prompt) throws Exception {
        JSONObject body = new JSONObject();
        body.put("workspace_id", workspaceId);
        body.put("agent_id", agentId);
        body.put("prompt", prompt);
        return new Models.QuickCreateTask(requestObject("POST", "/api/issues/quick-create",
                query(null, "workspace_id", workspaceId), body));
    }

    Models.IssueUsage issueUsage(String workspaceId, String issueId) throws Exception {
        return new Models.IssueUsage(requestObject("GET", "/api/issues/" + encPath(issueId) + "/usage",
                query(null, "workspace_id", workspaceId), null));
    }

    Models.TimelinePage issueTimeline(String workspaceId, String issueId, int limit, String beforeCursor, String afterCursor) throws Exception {
        return issueTimeline(workspaceId, issueId, limit, beforeCursor, afterCursor, null);
    }

    Models.TimelinePage issueTimeline(String workspaceId, String issueId, int limit, String beforeCursor, String afterCursor, String aroundId) throws Exception {
        JSONObject params = query(null, "workspace_id", workspaceId, "limit", limit);
        if (beforeCursor != null && !beforeCursor.isEmpty()) params.put("before", beforeCursor);
        if (afterCursor != null && !afterCursor.isEmpty()) params.put("after", afterCursor);
        if (aroundId != null && !aroundId.isEmpty()) params.put("around", aroundId);
        return new Models.TimelinePage(requestObject("GET", "/api/issues/" + encPath(issueId) + "/timeline", params, null));
    }

    List<Models.AgentTask> activeIssueTasks(String workspaceId, String issueId) throws Exception {
        JSONObject json = requestObject("GET", "/api/issues/" + encPath(issueId) + "/active-task",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "tasks"), Models.AgentTask::new);
    }

    Models.AgentTask cancelIssueTask(String workspaceId, String issueId, String taskId) throws Exception {
        return new Models.AgentTask(requestObject("POST",
                "/api/issues/" + encPath(issueId) + "/tasks/" + encPath(taskId) + "/cancel",
                query(null, "workspace_id", workspaceId), null));
    }

    void deleteIssue(String workspaceId, String issueId) throws Exception {
        requestObject("DELETE", "/api/issues/" + encPath(issueId),
                query(null, "workspace_id", workspaceId), null);
    }

    int batchUpdateIssues(String workspaceId, List<String> issueIds, String status, String priority) throws Exception {
        return batchUpdateIssues(workspaceId, issueIds, status, priority, null, null);
    }

    int batchUpdateIssues(String workspaceId, List<String> issueIds, String status, String priority,
                          String assigneeType, String assigneeId) throws Exception {
        JSONObject updates = new JSONObject();
        if (status != null && !status.isEmpty()) updates.put("status", status);
        if (priority != null && !priority.isEmpty()) updates.put("priority", priority);
        if (assigneeType != null && !assigneeType.isEmpty()) updates.put("assignee_type", assigneeType);
        if (assigneeId != null && !assigneeId.isEmpty()) updates.put("assignee_id", assigneeId);
        JSONObject body = new JSONObject();
        JSONArray ids = new JSONArray();
        for (String id : issueIds) ids.put(id);
        body.put("issue_ids", ids);
        body.put("updates", updates);
        return requestObject("POST", "/api/issues/batch-update",
                query(null, "workspace_id", workspaceId), body).optInt("updated", 0);
    }

    int batchDeleteIssues(String workspaceId, List<String> issueIds) throws Exception {
        JSONObject body = new JSONObject();
        JSONArray ids = new JSONArray();
        for (String id : issueIds) ids.put(id);
        body.put("issue_ids", ids);
        return requestObject("POST", "/api/issues/batch-delete",
                query(null, "workspace_id", workspaceId), body).optInt("deleted", 0);
    }

    Models.Page<Models.Comment> comments(String issueId, int limit, int offset) throws Exception {
        return comments(issueId, null, limit, offset);
    }

    Models.Page<Models.Comment> comments(String issueId, String workspaceId, int limit, int offset) throws Exception {
        JSONObject json = requestObject("GET", "/api/issues/" + encPath(issueId) + "/comments",
                query(null, "workspace_id", workspaceId, "limit", limit, "offset", offset), null);
        return parsePage(json, "comments", Models.Comment::new);
    }

    Models.Comment addComment(String issueId, String content) throws Exception {
        return addComment(issueId, null, content);
    }

    Models.Comment addComment(String issueId, String workspaceId, String content) throws Exception {
        return addComment(issueId, workspaceId, content, null);
    }

    Models.Comment addComment(String issueId, String workspaceId, String content, List<String> attachmentIds) throws Exception {
        return addComment(issueId, workspaceId, content, attachmentIds, null);
    }

    Models.Comment addComment(String issueId, String workspaceId, String content, List<String> attachmentIds, String parentId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("content", content);
        body.put("type", "comment");
        body.put("parent_id", parentId == null || parentId.isEmpty() ? JSONObject.NULL : parentId);
        if (attachmentIds != null && !attachmentIds.isEmpty()) {
            JSONArray ids = new JSONArray();
            for (String id : attachmentIds) ids.put(id);
            body.put("attachment_ids", ids);
        }
        return new Models.Comment(requestObject("POST", "/api/issues/" + encPath(issueId) + "/comments",
                query(null, "workspace_id", workspaceId), body));
    }

    Models.Comment addCommentAsAgent(String issueId, String workspaceId, String agentId, String content) throws Exception {
        JSONObject body = new JSONObject();
        body.put("content", content);
        body.put("type", "comment");
        body.put("parent_id", JSONObject.NULL);
        return new Models.Comment(requestObject("POST", "/api/issues/" + encPath(issueId) + "/comments",
                query(null, "workspace_id", workspaceId), body, Collections.singletonMap("X-Agent-ID", agentId)));
    }

    Models.Comment updateComment(String workspaceId, String commentId, String content) throws Exception {
        JSONObject body = new JSONObject();
        body.put("content", content);
        return new Models.Comment(requestObject("PUT", "/api/comments/" + encPath(commentId),
                query(null, "workspace_id", workspaceId), body));
    }

    void deleteComment(String workspaceId, String commentId) throws Exception {
        requestObject("DELETE", "/api/comments/" + encPath(commentId),
                query(null, "workspace_id", workspaceId), null);
    }

    Models.Reaction addCommentReaction(String workspaceId, String commentId, String emoji) throws Exception {
        JSONObject body = new JSONObject();
        body.put("emoji", emoji);
        JSONObject json = requestObject("POST", "/api/comments/" + encPath(commentId) + "/reactions",
                query(null, "workspace_id", workspaceId), body);
        return new Models.Reaction(unwrap(json, "reaction"));
    }

    void removeCommentReaction(String workspaceId, String commentId, String emoji) throws Exception {
        JSONObject body = new JSONObject();
        body.put("emoji", emoji);
        requestObject("DELETE", "/api/comments/" + encPath(commentId) + "/reactions",
                query(null, "workspace_id", workspaceId), body);
    }

    Models.Reaction addIssueReaction(String workspaceId, String issueId, String emoji) throws Exception {
        JSONObject body = new JSONObject();
        body.put("emoji", emoji);
        JSONObject json = requestObject("POST", "/api/issues/" + encPath(issueId) + "/reactions",
                query(null, "workspace_id", workspaceId), body);
        return new Models.Reaction(unwrap(json, "reaction"));
    }

    void removeIssueReaction(String workspaceId, String issueId, String emoji) throws Exception {
        JSONObject body = new JSONObject();
        body.put("emoji", emoji);
        requestObject("DELETE", "/api/issues/" + encPath(issueId) + "/reactions",
                query(null, "workspace_id", workspaceId), body);
    }

    List<Models.IssueSubscriber> issueSubscribers(String workspaceId, String issueId) throws Exception {
        JSONObject json = requestObject("GET", "/api/issues/" + encPath(issueId) + "/subscribers",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "subscribers"), Models.IssueSubscriber::new);
    }

    void subscribeToIssue(String workspaceId, String issueId, String userId, String userType) throws Exception {
        JSONObject body = new JSONObject();
        if (userId != null && !userId.isEmpty()) body.put("user_id", userId);
        if (userType != null && !userType.isEmpty()) body.put("user_type", userType);
        requestObject("POST", "/api/issues/" + encPath(issueId) + "/subscribe",
                query(null, "workspace_id", workspaceId), body);
    }

    void unsubscribeFromIssue(String workspaceId, String issueId, String userId, String userType) throws Exception {
        JSONObject body = new JSONObject();
        if (userId != null && !userId.isEmpty()) body.put("user_id", userId);
        if (userType != null && !userType.isEmpty()) body.put("user_type", userType);
        requestObject("POST", "/api/issues/" + encPath(issueId) + "/unsubscribe",
                query(null, "workspace_id", workspaceId), body);
    }

    List<Models.Attachment> issueAttachments(String workspaceId, String issueId) throws Exception {
        JSONObject json = requestObject("GET", "/api/issues/" + encPath(issueId) + "/attachments",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "attachments"), Models.Attachment::new);
    }

    Models.Attachment uploadIssueAttachment(String workspaceId, String issueId, String filename,
                                            String contentType, byte[] data) throws Exception {
        JSONObject fields = new JSONObject();
        fields.put("issue_id", issueId);
        JSONObject json = requestMultipart("/api/upload-file", query(null, "workspace_id", workspaceId),
                fields, "file", filename, contentType, data);
        return new Models.Attachment(json);
    }

    Models.Attachment uploadAttachment(String workspaceId, String filename, String contentType, byte[] data) throws Exception {
        JSONObject json = requestMultipart("/api/upload-file", query(null, "workspace_id", workspaceId),
                "file", filename, contentType, data);
        return new Models.Attachment(json);
    }

    void deleteAttachment(String workspaceId, String attachmentId) throws Exception {
        requestObject("DELETE", "/api/attachments/" + encPath(attachmentId),
                query(null, "workspace_id", workspaceId), null);
    }

    List<Models.AgentTask> agentRuns(String issueId) throws Exception {
        return agentRuns(issueId, null);
    }

    List<Models.AgentTask> agentRuns(String issueId, String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/issues/" + encPath(issueId) + "/task-runs",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "runs"), Models.AgentTask::new);
    }

    List<Models.AgentTask> agentTasks(String workspaceId, String agentId) throws Exception {
        JSONObject json = requestObject("GET", "/api/agents/" + encPath(agentId) + "/tasks",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "tasks"), Models.AgentTask::new);
    }

    List<Models.AgentTask> agentTaskSnapshot(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/agent-task-snapshot",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "tasks"), Models.AgentTask::new);
    }

    List<Models.AgentActivityBucket> agentActivity30d(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/agent-activity-30d",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "activity"), Models.AgentActivityBucket::new);
    }

    List<Models.AgentRunCount> agentRunCounts30d(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/agent-run-counts",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "run_counts"), Models.AgentRunCount::new);
    }

    List<Models.TaskMessage> runMessages(String taskId) throws Exception {
        return runMessages(taskId, null);
    }

    List<Models.TaskMessage> runMessages(String taskId, String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/tasks/" + encPath(taskId) + "/messages",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "messages"), Models.TaskMessage::new);
    }

    Models.Page<Models.Project> projects(String workspaceId, int limit, int offset) throws Exception {
        JSONObject query = query(null, "workspace_id", workspaceId, "limit", limit, "offset", offset);
        JSONObject json = requestObject("GET", "/api/projects", query, null);
        return parsePage(json, "projects", Models.Project::new);
    }

    List<Models.Project> searchProjects(String workspaceId, String text, int limit) throws Exception {
        JSONObject query = query(null,
                "workspace_id", workspaceId,
                "q", text,
                "limit", limit,
                "include_closed", "true");
        JSONObject json = requestObject("GET", "/api/projects/search", query, null);
        return parseArray(extractArray(json, "projects"), Models.Project::new);
    }

    Models.Project project(String workspaceId, String projectId) throws Exception {
        return new Models.Project(unwrap(requestObject("GET", "/api/projects/" + encPath(projectId),
                query(null, "workspace_id", workspaceId), null), "project"));
    }

    Models.Project createProject(String workspaceId, String title, String description) throws Exception {
        return createProject(workspaceId, title, description, "planned", "none", "", "", "", new ArrayList<>());
    }

    Models.Project createProject(
            String workspaceId,
            String title,
            String description,
            String status,
            String priority,
            String icon,
            String leadType,
            String leadId,
            List<String> resourceUrls
    ) throws Exception {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("workspace_id", workspaceId);
        if (description == null || description.isEmpty()) body.put("description", JSONObject.NULL);
        else body.put("description", description);
        body.put("status", status == null || status.isEmpty() ? "planned" : status);
        body.put("priority", priority == null || priority.isEmpty() ? "none" : priority);
        if (icon == null || icon.isEmpty()) body.put("icon", JSONObject.NULL);
        else body.put("icon", icon);
        if (leadType == null || leadType.isEmpty() || leadId == null || leadId.isEmpty()) {
            body.put("lead_type", JSONObject.NULL);
            body.put("lead_id", JSONObject.NULL);
        } else {
            body.put("lead_type", leadType);
            body.put("lead_id", leadId);
        }
        if (resourceUrls != null && !resourceUrls.isEmpty()) {
            JSONArray resources = new JSONArray();
            int position = 0;
            for (String url : resourceUrls) {
                if (url == null || url.trim().isEmpty()) continue;
                JSONObject resource = new JSONObject();
                resource.put("resource_type", "github_repo");
                resource.put("url", url.trim());
                resource.put("position", position++);
                resources.put(resource);
            }
            if (resources.length() > 0) body.put("resources", resources);
        }
        return new Models.Project(unwrap(requestObject("POST", "/api/projects",
                query(null, "workspace_id", workspaceId), body), "project"));
    }

    Models.Project updateProject(String workspaceId, Models.Project project, String title, String description) throws Exception {
        return updateProject(workspaceId, project, title, description, project.status, project.priority, project.icon, project.leadType, project.leadId);
    }

    Models.Project updateProject(
            String workspaceId,
            Models.Project project,
            String title,
            String description,
            String status,
            String priority,
            String icon,
            String leadType,
            String leadId
    ) throws Exception {
        JSONObject body = new JSONObject();
        body.put("title", title);
        if (description == null || description.isEmpty()) body.put("description", JSONObject.NULL);
        else body.put("description", description);
        body.put("status", status == null || status.isEmpty() ? "planned" : status);
        body.put("priority", priority == null || priority.isEmpty() ? "none" : priority);
        if (icon == null || icon.isEmpty()) body.put("icon", JSONObject.NULL);
        else body.put("icon", icon);
        if (leadType == null || leadType.isEmpty() || leadId == null || leadId.isEmpty()) {
            body.put("lead_type", JSONObject.NULL);
            body.put("lead_id", JSONObject.NULL);
        } else {
            body.put("lead_type", leadType);
            body.put("lead_id", leadId);
        }
        return new Models.Project(unwrap(requestObject("PUT", "/api/projects/" + encPath(project.id),
                query(null, "workspace_id", workspaceId), body), "project"));
    }

    void deleteProject(String workspaceId, String projectId) throws Exception {
        requestObject("DELETE", "/api/projects/" + encPath(projectId),
                query(null, "workspace_id", workspaceId), null);
    }

    Models.Page<Models.ProjectResource> projectResources(String workspaceId, String projectId) throws Exception {
        JSONObject json = requestObject("GET", "/api/projects/" + encPath(projectId) + "/resources",
                query(null, "workspace_id", workspaceId), null);
        return parsePage(json, "resources", Models.ProjectResource::new);
    }

    Models.ProjectResource createProjectResource(String workspaceId, String projectId, String resourceType, String url) throws Exception {
        JSONObject ref = new JSONObject();
        ref.put("url", url);
        JSONObject body = new JSONObject();
        body.put("resource_type", resourceType);
        body.put("resource_ref", ref);
        return new Models.ProjectResource(unwrap(requestObject("POST", "/api/projects/" + encPath(projectId) + "/resources",
                query(null, "workspace_id", workspaceId), body), "resource"));
    }

    void deleteProjectResource(String workspaceId, String projectId, String resourceId) throws Exception {
        requestObject("DELETE", "/api/projects/" + encPath(projectId) + "/resources/" + encPath(resourceId),
                query(null, "workspace_id", workspaceId), null);
    }

    List<Models.PinnedItem> pins(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/pins", query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "pins"), Models.PinnedItem::new);
    }

    Models.PinnedItem createPin(String workspaceId, String itemType, String itemId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("item_type", itemType);
        body.put("item_id", itemId);
        return new Models.PinnedItem(unwrap(requestObject("POST", "/api/pins",
                query(null, "workspace_id", workspaceId), body), "pin"));
    }

    void deletePin(String workspaceId, String itemType, String itemId) throws Exception {
        requestObject("DELETE", "/api/pins/" + encPath(itemType) + "/" + encPath(itemId),
                query(null, "workspace_id", workspaceId), null);
    }

    void reorderPins(String workspaceId, List<Models.PinnedItem> orderedPins) throws Exception {
        JSONArray items = new JSONArray();
        if (orderedPins != null) {
            for (int i = 0; i < orderedPins.size(); i++) {
                Models.PinnedItem pin = orderedPins.get(i);
                if (pin == null || pin.id == null || pin.id.isEmpty()) continue;
                items.put(new JSONObject()
                        .put("id", pin.id)
                        .put("position", i + 1));
            }
        }
        JSONObject body = new JSONObject().put("items", items);
        requestObject("PUT", "/api/pins/reorder", query(null, "workspace_id", workspaceId), body);
    }

    List<Models.Agent> agents(String workspaceId) throws Exception {
        return agents(workspaceId, false);
    }

    List<Models.Agent> agents(String workspaceId, boolean includeArchived) throws Exception {
        JSONObject query = query(null, "workspace_id", workspaceId);
        if (includeArchived) query.put("include_archived", "true");
        JSONObject json = requestObject("GET", "/api/agents", query, null);
        return parseArray(extractArray(json, "agents"), Models.Agent::new);
    }

    List<Models.Runtime> runtimes(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/runtimes", query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "runtimes"), Models.Runtime::new);
    }

    List<Models.RuntimeUsage> runtimeUsage(String workspaceId, String runtimeId, int days) throws Exception {
        JSONObject json = requestObject("GET", "/api/runtimes/" + encPath(runtimeId) + "/usage",
                query(null, "workspace_id", workspaceId, "days", days), null);
        return parseArray(extractArray(json, "usage"), Models.RuntimeUsage::new);
    }

    List<Models.RuntimeHourlyActivity> runtimeActivity(String workspaceId, String runtimeId) throws Exception {
        JSONObject json = requestObject("GET", "/api/runtimes/" + encPath(runtimeId) + "/activity",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "activity"), Models.RuntimeHourlyActivity::new);
    }

    List<Models.RuntimeUsageByAgent> runtimeUsageByAgent(String workspaceId, String runtimeId, int days) throws Exception {
        JSONObject json = requestObject("GET", "/api/runtimes/" + encPath(runtimeId) + "/usage/by-agent",
                query(null, "workspace_id", workspaceId, "days", days), null);
        return parseArray(extractArray(json, "usage"), Models.RuntimeUsageByAgent::new);
    }

    List<Models.RuntimeUsageByHour> runtimeUsageByHour(String workspaceId, String runtimeId, int days) throws Exception {
        JSONObject json = requestObject("GET", "/api/runtimes/" + encPath(runtimeId) + "/usage/by-hour",
                query(null, "workspace_id", workspaceId, "days", days), null);
        return parseArray(extractArray(json, "usage"), Models.RuntimeUsageByHour::new);
    }

    void deleteRuntime(String workspaceId, String runtimeId) throws Exception {
        requestObject("DELETE", "/api/runtimes/" + encPath(runtimeId), query(null, "workspace_id", workspaceId), null);
    }

    Models.RuntimeUpdate initiateRuntimeUpdate(String workspaceId, String runtimeId, String targetVersion) throws Exception {
        JSONObject body = new JSONObject();
        body.put("target_version", targetVersion);
        JSONObject json = requestObject("POST", "/api/runtimes/" + encPath(runtimeId) + "/update",
                query(null, "workspace_id", workspaceId), body);
        return new Models.RuntimeUpdate(json);
    }

    Models.RuntimeUpdate runtimeUpdateResult(String workspaceId, String runtimeId, String updateId) throws Exception {
        return new Models.RuntimeUpdate(requestObject("GET",
                "/api/runtimes/" + encPath(runtimeId) + "/update/" + encPath(updateId),
                query(null, "workspace_id", workspaceId), null));
    }

    Models.RuntimeModelListRequest initiateListRuntimeModels(String workspaceId, String runtimeId) throws Exception {
        return new Models.RuntimeModelListRequest(requestObject("POST",
                "/api/runtimes/" + encPath(runtimeId) + "/models",
                query(null, "workspace_id", workspaceId), null, null, 8000, 8000));
    }

    Models.RuntimeModelListRequest runtimeModelListResult(String workspaceId, String runtimeId, String requestId) throws Exception {
        return new Models.RuntimeModelListRequest(requestObject("GET",
                "/api/runtimes/" + encPath(runtimeId) + "/models/" + encPath(requestId),
                query(null, "workspace_id", workspaceId), null));
    }

    Models.RuntimeLocalSkillListRequest initiateListRuntimeLocalSkills(String workspaceId, String runtimeId) throws Exception {
        return new Models.RuntimeLocalSkillListRequest(requestObject("POST",
                "/api/runtimes/" + encPath(runtimeId) + "/local-skills",
                query(null, "workspace_id", workspaceId), null, null, 8000, 8000));
    }

    Models.RuntimeLocalSkillListRequest runtimeLocalSkillListResult(String workspaceId, String runtimeId, String requestId) throws Exception {
        return new Models.RuntimeLocalSkillListRequest(requestObject("GET",
                "/api/runtimes/" + encPath(runtimeId) + "/local-skills/" + encPath(requestId),
                query(null, "workspace_id", workspaceId), null));
    }

    Models.RuntimeLocalSkillImportRequest initiateImportRuntimeLocalSkill(String workspaceId, String runtimeId,
                                                                          String skillKey, String name, String description) throws Exception {
        JSONObject body = new JSONObject();
        body.put("skill_key", skillKey);
        if (name == null || name.trim().isEmpty()) body.put("name", JSONObject.NULL); else body.put("name", name.trim());
        if (description == null || description.trim().isEmpty()) body.put("description", JSONObject.NULL);
        else body.put("description", description.trim());
        return new Models.RuntimeLocalSkillImportRequest(requestObject("POST",
                "/api/runtimes/" + encPath(runtimeId) + "/local-skills/import",
                query(null, "workspace_id", workspaceId), body));
    }

    Models.RuntimeLocalSkillImportRequest runtimeLocalSkillImportResult(String workspaceId, String runtimeId, String requestId) throws Exception {
        return new Models.RuntimeLocalSkillImportRequest(requestObject("GET",
                "/api/runtimes/" + encPath(runtimeId) + "/local-skills/import/" + encPath(requestId),
                query(null, "workspace_id", workspaceId), null));
    }

    List<Models.Skill> skills(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/skills", query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "skills"), Models.Skill::new);
    }

    Models.Skill skill(String workspaceId, String skillId) throws Exception {
        return new Models.Skill(requestObject("GET", "/api/skills/" + encPath(skillId),
                query(null, "workspace_id", workspaceId), null));
    }

    List<Models.SkillFile> skillFiles(String workspaceId, String skillId) throws Exception {
        JSONObject json = requestObject("GET", "/api/skills/" + encPath(skillId) + "/files",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "items"), Models.SkillFile::new);
    }

    Models.SkillFile upsertSkillFile(String workspaceId, String skillId, String path, String content) throws Exception {
        JSONObject body = new JSONObject();
        body.put("path", path);
        body.put("content", content == null ? "" : content);
        return new Models.SkillFile(requestObject("PUT", "/api/skills/" + encPath(skillId) + "/files",
                query(null, "workspace_id", workspaceId), body));
    }

    Models.Skill createSkill(String workspaceId, String name, String description, String content) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("description", description == null ? "" : description);
        body.put("content", content == null ? "" : content);
        return new Models.Skill(requestObject("POST", "/api/skills", query(null, "workspace_id", workspaceId), body));
    }

    Models.Skill updateSkill(String workspaceId, Models.Skill skill, String name, String description, String content) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("description", description == null ? "" : description);
        body.put("content", content == null ? "" : content);
        return new Models.Skill(requestObject("PUT", "/api/skills/" + encPath(skill.id),
                query(null, "workspace_id", workspaceId), body));
    }

    Models.Skill importSkill(String workspaceId, String url) throws Exception {
        JSONObject body = new JSONObject();
        body.put("url", url);
        return new Models.Skill(requestObject("POST", "/api/skills/import",
                query(null, "workspace_id", workspaceId), body));
    }

    void deleteSkill(String workspaceId, String skillId) throws Exception {
        requestObject("DELETE", "/api/skills/" + encPath(skillId), query(null, "workspace_id", workspaceId), null);
    }

    List<Models.Autopilot> autopilots(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/autopilots", query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "autopilots"), Models.Autopilot::new);
    }

    Models.Autopilot createAutopilot(String workspaceId, String title, String description, String assigneeId,
                                     String executionMode, String issueTitleTemplate) throws Exception {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("description", description == null || description.trim().isEmpty() ? JSONObject.NULL : description);
        body.put("assignee_id", assigneeId);
        body.put("execution_mode", executionMode == null || executionMode.isEmpty() ? "create_issue" : executionMode);
        if (issueTitleTemplate == null || issueTitleTemplate.trim().isEmpty()) {
            body.put("issue_title_template", JSONObject.NULL);
        } else {
            body.put("issue_title_template", issueTitleTemplate);
        }
        return new Models.Autopilot(unwrap(requestObject("POST", "/api/autopilots", query(null, "workspace_id", workspaceId), body), "autopilot"));
    }

    Models.Autopilot updateAutopilot(String workspaceId, Models.Autopilot autopilot, String title, String description,
                                     String assigneeId, String status, String executionMode, String issueTitleTemplate) throws Exception {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("description", description == null || description.trim().isEmpty() ? JSONObject.NULL : description);
        body.put("assignee_id", assigneeId);
        body.put("status", status == null || status.isEmpty() ? "active" : status);
        body.put("execution_mode", executionMode == null || executionMode.isEmpty() ? "create_issue" : executionMode);
        if (issueTitleTemplate == null || issueTitleTemplate.trim().isEmpty()) {
            body.put("issue_title_template", JSONObject.NULL);
        } else {
            body.put("issue_title_template", issueTitleTemplate);
        }
        return new Models.Autopilot(unwrap(requestObject("PATCH", "/api/autopilots/" + encPath(autopilot.id),
                query(null, "workspace_id", workspaceId), body), "autopilot"));
    }

    void deleteAutopilot(String workspaceId, String autopilotId) throws Exception {
        requestObject("DELETE", "/api/autopilots/" + encPath(autopilotId), query(null, "workspace_id", workspaceId), null);
    }

    Models.AutopilotRun triggerAutopilot(String workspaceId, String autopilotId) throws Exception {
        return new Models.AutopilotRun(unwrap(requestObject("POST", "/api/autopilots/" + encPath(autopilotId) + "/trigger",
                query(null, "workspace_id", workspaceId), null), "run"));
    }

    List<Models.AutopilotTrigger> autopilotTriggers(String workspaceId, String autopilotId) throws Exception {
        JSONObject json = requestObject("GET", "/api/autopilots/" + encPath(autopilotId),
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "triggers"), Models.AutopilotTrigger::new);
    }

    List<Models.AutopilotRun> autopilotRuns(String workspaceId, String autopilotId) throws Exception {
        JSONObject json = requestObject("GET", "/api/autopilots/" + encPath(autopilotId) + "/runs",
                query(null, "workspace_id", workspaceId, "limit", 20, "offset", 0), null);
        return parseArray(extractArray(json, "runs"), Models.AutopilotRun::new);
    }

    Models.AutopilotTrigger createAutopilotTrigger(String workspaceId, String autopilotId, String kind,
                                                   String cronExpression, String timezone, String label) throws Exception {
        JSONObject body = new JSONObject();
        body.put("kind", normalizedAutopilotTriggerKind(kind));
        if (cronExpression == null || cronExpression.trim().isEmpty()) body.put("cron_expression", JSONObject.NULL);
        else body.put("cron_expression", cronExpression.trim());
        if (timezone == null || timezone.trim().isEmpty()) body.put("timezone", JSONObject.NULL);
        else body.put("timezone", timezone.trim());
        if (label == null || label.trim().isEmpty()) body.put("label", JSONObject.NULL);
        else body.put("label", label.trim());
        return new Models.AutopilotTrigger(unwrap(requestObject("POST",
                "/api/autopilots/" + encPath(autopilotId) + "/triggers",
                query(null, "workspace_id", workspaceId), body), "trigger"));
    }

    static String normalizedAutopilotTriggerKind(String kind) {
        if (kind == null || kind.trim().isEmpty() || "cron".equals(kind.trim())) return "schedule";
        return kind.trim();
    }

    Models.AutopilotTrigger updateAutopilotTrigger(String workspaceId, String autopilotId, String triggerId,
                                                   Boolean enabled, String cronExpression, String timezone,
                                                   String label) throws Exception {
        JSONObject body = new JSONObject();
        if (enabled == null) body.put("enabled", JSONObject.NULL);
        else body.put("enabled", enabled.booleanValue());
        if (cronExpression == null || cronExpression.trim().isEmpty()) body.put("cron_expression", JSONObject.NULL);
        else body.put("cron_expression", cronExpression.trim());
        if (timezone == null || timezone.trim().isEmpty()) body.put("timezone", JSONObject.NULL);
        else body.put("timezone", timezone.trim());
        if (label == null || label.trim().isEmpty()) body.put("label", JSONObject.NULL);
        else body.put("label", label.trim());
        return new Models.AutopilotTrigger(unwrap(requestObject("PATCH",
                "/api/autopilots/" + encPath(autopilotId) + "/triggers/" + encPath(triggerId),
                query(null, "workspace_id", workspaceId), body), "trigger"));
    }

    void deleteAutopilotTrigger(String workspaceId, String autopilotId, String triggerId) throws Exception {
        requestObject("DELETE", "/api/autopilots/" + encPath(autopilotId) + "/triggers/" + encPath(triggerId),
                query(null, "workspace_id", workspaceId), null);
    }

    List<Models.IssueLabel> labels(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/labels", query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "labels"), Models.IssueLabel::new);
    }

    List<Models.IssueLabel> issueLabels(String workspaceId, String issueId) throws Exception {
        JSONObject json = requestObject("GET", "/api/issues/" + encPath(issueId) + "/labels",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "labels"), Models.IssueLabel::new);
    }

    List<Models.IssueLabel> attachIssueLabel(String workspaceId, String issueId, String labelId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("label_id", labelId);
        JSONObject json = requestObject("POST", "/api/issues/" + encPath(issueId) + "/labels",
                query(null, "workspace_id", workspaceId), body);
        return parseArray(extractArray(json, "labels"), Models.IssueLabel::new);
    }

    List<Models.IssueLabel> detachIssueLabel(String workspaceId, String issueId, String labelId) throws Exception {
        JSONObject json = requestObject("DELETE", "/api/issues/" + encPath(issueId) + "/labels/" + encPath(labelId),
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "labels"), Models.IssueLabel::new);
    }

    Models.IssueLabel createLabel(String workspaceId, String name, String color) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("color", color);
        return new Models.IssueLabel(requestObject("POST", "/api/labels", query(null, "workspace_id", workspaceId), body));
    }

    Models.IssueLabel updateLabel(String workspaceId, String labelId, String name, String color) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("color", color);
        return new Models.IssueLabel(requestObject("PUT", "/api/labels/" + encPath(labelId),
                query(null, "workspace_id", workspaceId), body));
    }

    void deleteLabel(String workspaceId, String labelId) throws Exception {
        requestObject("DELETE", "/api/labels/" + encPath(labelId),
                query(null, "workspace_id", workspaceId), null);
    }

    void createFeedback(String workspaceId, String message, String url) throws Exception {
        JSONObject body = new JSONObject();
        body.put("workspace_id", workspaceId);
        body.put("message", message);
        if (url != null && !url.trim().isEmpty()) body.put("url", url.trim());
        requestObject("POST", "/api/feedback", query(null, "workspace_id", workspaceId), body);
    }

    List<Models.ChatSession> chatSessions(String workspaceId) throws Exception {
        return chatSessions(workspaceId, null);
    }

    List<Models.ChatSession> chatSessions(String workspaceId, String status) throws Exception {
        JSONObject query = query(null, "workspace_id", workspaceId);
        if (status != null && !status.trim().isEmpty()) {
            query.put("status", status.trim());
        }
        JSONObject json = requestObject("GET", "/api/chat/sessions", query, null);
        return parseArray(extractArray(json, "sessions"), Models.ChatSession::new);
    }

    Models.ChatSession chatSession(String workspaceId, String sessionId) throws Exception {
        return new Models.ChatSession(requestObject("GET", "/api/chat/sessions/" + encPath(sessionId),
                query(null, "workspace_id", workspaceId), null));
    }

    Models.ChatSession createChatSession(String workspaceId, String agentId, String title) throws Exception {
        JSONObject body = new JSONObject();
        body.put("agent_id", agentId);
        if (title != null && !title.trim().isEmpty()) body.put("title", title.trim());
        return new Models.ChatSession(requestObject("POST", "/api/chat/sessions", query(null, "workspace_id", workspaceId), body));
    }

    void archiveChatSession(String workspaceId, String sessionId) throws Exception {
        requestObject("DELETE", "/api/chat/sessions/" + encPath(sessionId),
                query(null, "workspace_id", workspaceId), null);
    }

    List<Models.ChatMessage> chatMessages(String workspaceId, String sessionId) throws Exception {
        JSONObject json = requestObject("GET", "/api/chat/sessions/" + encPath(sessionId) + "/messages",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "messages"), Models.ChatMessage::new);
    }

    void sendChatMessage(String workspaceId, String sessionId, String content) throws Exception {
        JSONObject body = new JSONObject();
        body.put("content", content);
        requestObject("POST", "/api/chat/sessions/" + encPath(sessionId) + "/messages",
                query(null, "workspace_id", workspaceId), body);
    }

    Models.ChatPendingTask pendingChatTask(String workspaceId, String sessionId) throws Exception {
        return new Models.ChatPendingTask(requestObject("GET",
                "/api/chat/sessions/" + encPath(sessionId) + "/pending-task",
                query(null, "workspace_id", workspaceId), null));
    }

    List<Models.PendingChatTaskItem> pendingChatTasks(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/chat/pending-tasks",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "tasks"), Models.PendingChatTaskItem::new);
    }

    void markChatSessionRead(String workspaceId, String sessionId) throws Exception {
        requestObject("POST", "/api/chat/sessions/" + encPath(sessionId) + "/read",
                query(null, "workspace_id", workspaceId), null);
    }

    void cancelTaskById(String workspaceId, String taskId) throws Exception {
        requestObject("POST", "/api/tasks/" + encPath(taskId) + "/cancel",
                query(null, "workspace_id", workspaceId), null);
    }

    List<String> memberSummaries(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/workspaces/" + encPath(workspaceId) + "/members",
                query(null, "workspace_id", workspaceId), null);
        JSONArrayLike array = new JSONArrayLike(extractArray(json, "members"));
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            rows.add(item.optString("name", item.optString("email", Models.shortId(item.optString("id"))))
                    + "\n" + item.optString("email")
                    + (item.optString("role").isEmpty() ? "" : " · " + item.optString("role")));
        }
        return rows;
    }

    List<Models.Member> members(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/workspaces/" + encPath(workspaceId) + "/members",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "members"), Models.Member::new);
    }

    Models.Invitation createMemberInvitation(String workspaceId, String email, String role) throws Exception {
        JSONObject body = new JSONObject();
        body.put("email", email);
        body.put("role", role == null || role.isEmpty() ? "member" : role);
        JSONObject json = requestObject("POST", "/api/workspaces/" + encPath(workspaceId) + "/members",
                query(null, "workspace_id", workspaceId), body);
        return new Models.Invitation(unwrap(json, "invitation"));
    }

    Models.Member updateMemberRole(String workspaceId, String memberId, String role) throws Exception {
        JSONObject body = new JSONObject();
        body.put("role", role);
        JSONObject json = requestObject("PATCH", "/api/workspaces/" + encPath(workspaceId) + "/members/" + encPath(memberId),
                query(null, "workspace_id", workspaceId), body);
        return new Models.Member(unwrap(json, "member"));
    }

    void deleteMember(String workspaceId, String memberId) throws Exception {
        requestObject("DELETE", "/api/workspaces/" + encPath(workspaceId) + "/members/" + encPath(memberId),
                query(null, "workspace_id", workspaceId), null);
    }

    List<Models.Invitation> workspaceInvitations(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/workspaces/" + encPath(workspaceId) + "/invitations",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "invitations"), Models.Invitation::new);
    }

    void revokeInvitation(String workspaceId, String invitationId) throws Exception {
        requestObject("DELETE", "/api/workspaces/" + encPath(workspaceId) + "/invitations/" + encPath(invitationId),
                query(null, "workspace_id", workspaceId), null);
    }

    List<Models.Invitation> myInvitations() throws Exception {
        JSONObject json = requestObject("GET", "/api/invitations", null, null);
        return parseArray(extractArray(json, "invitations"), Models.Invitation::new);
    }

    Models.Invitation invitation(String invitationId) throws Exception {
        return new Models.Invitation(requestObject("GET", "/api/invitations/" + encPath(invitationId), null, null));
    }

    Models.Member acceptInvitation(String invitationId) throws Exception {
        JSONObject json = requestObject("POST", "/api/invitations/" + encPath(invitationId) + "/accept", null, null);
        return new Models.Member(unwrap(json, "member"));
    }

    void declineInvitation(String invitationId) throws Exception {
        requestObject("POST", "/api/invitations/" + encPath(invitationId) + "/decline", null, null);
    }

    List<String> tokenSummaries() throws Exception {
        JSONObject json = requestObject("GET", "/api/tokens", null, null);
        JSONArrayLike array = new JSONArrayLike(extractArray(json, "tokens"));
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            rows.add(item.optString("name", Models.shortId(item.optString("id")))
                    + "\n" + item.optString("token_prefix")
                    + (item.optString("last_used_at").isEmpty() ? "" : " · " + item.optString("last_used_at")));
        }
        return rows;
    }

    List<Models.PersonalAccessToken> personalAccessTokens() throws Exception {
        JSONObject json = requestObject("GET", "/api/tokens", null, null);
        return parseArray(extractArray(json, "tokens"), Models.PersonalAccessToken::new);
    }

    Models.CreatedPersonalAccessToken createPersonalAccessToken(String name, Integer expiresInDays) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        if (expiresInDays == null) body.put("expires_in_days", JSONObject.NULL);
        else body.put("expires_in_days", expiresInDays);
        return new Models.CreatedPersonalAccessToken(unwrap(requestObject("POST", "/api/tokens", null, body), "token"));
    }

    void revokePersonalAccessToken(String id) throws Exception {
        requestObject("DELETE", "/api/tokens/" + encPath(id), null, null);
    }

    List<String> notificationSummaries(String workspaceId) throws Exception {
        Models.NotificationPreferences preferences = notificationPreferences(workspaceId);
        List<String> rows = new ArrayList<>();
        for (Map.Entry<String, String> entry : preferences.values.entrySet()) {
            rows.add(entry.getKey() + "\n" + entry.getValue());
        }
        return rows;
    }

    Models.NotificationPreferences notificationPreferences(String workspaceId) throws Exception {
        return new Models.NotificationPreferences(requestObject("GET", "/api/notification-preferences",
                query(null, "workspace_id", workspaceId), null));
    }

    Models.NotificationPreferences updateNotificationPreferences(String workspaceId, Map<String, String> preferences) throws Exception {
        JSONObject values = new JSONObject();
        for (Map.Entry<String, String> entry : preferences.entrySet()) {
            values.put(entry.getKey(), entry.getValue());
        }
        JSONObject body = new JSONObject();
        body.put("preferences", values);
        return new Models.NotificationPreferences(requestObject("PUT", "/api/notification-preferences",
                query(null, "workspace_id", workspaceId), body));
    }

    String workspaceSummary(String workspaceId) throws Exception {
        JSONObject json = requestObject("GET", "/api/workspaces/" + encPath(workspaceId),
                query(null, "workspace_id", workspaceId), null);
        return json.optString("name") + "\n"
                + json.optString("slug") + "\n"
                + json.optString("description") + "\n"
                + json.optString("context");
    }

    private static final class JSONArrayLike {
        private final org.json.JSONArray array;
        JSONArrayLike(org.json.JSONArray array) { this.array = array; }
        int length() { return array.length(); }
        JSONObject getJSONObject(int index) throws Exception { return array.getJSONObject(index); }
    }

    Models.Agent createAgent(String workspaceId, String name, String description, String runtimeId) throws Exception {
        return createAgent(workspaceId, name, description,
                description == null || description.trim().isEmpty() ? "Help with workspace tasks." : description,
                runtimeId, "private", 1, "", null);
    }

    Models.Agent createAgent(String workspaceId, String name, String description, String instructions, String runtimeId,
                             String visibility, int maxConcurrentTasks, String model, String avatarUrl) throws Exception {
        return createAgent(workspaceId, name, description, instructions, runtimeId, visibility, maxConcurrentTasks,
                model, avatarUrl, null, null);
    }

    Models.Agent createAgent(String workspaceId, String name, String description, String instructions, String runtimeId,
                             String visibility, int maxConcurrentTasks, String model, String avatarUrl,
                             Map<String, String> customEnv, List<String> customArgs) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("description", description == null ? "" : description);
        body.put("instructions", instructions == null || instructions.trim().isEmpty() ? "Help with workspace tasks." : instructions);
        body.put("runtime_id", runtimeId);
        body.put("visibility", visibility == null || visibility.isEmpty() ? "private" : visibility);
        body.put("max_concurrent_tasks", Math.max(1, maxConcurrentTasks));
        body.put("model", model == null ? "" : model);
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) body.put("avatar_url", JSONObject.NULL);
        else body.put("avatar_url", avatarUrl.trim());
        putCustomAgentConfig(body, customEnv, customArgs);
        return new Models.Agent(requestObject("POST", "/api/agents", query(null, "workspace_id", workspaceId), body));
    }

    Models.Agent updateAgent(String workspaceId, Models.Agent agent, String name, String description, String instructions,
                             String runtimeId, String visibility, int maxConcurrentTasks, String model, String avatarUrl) throws Exception {
        return updateAgent(workspaceId, agent, name, description, instructions, runtimeId, visibility, maxConcurrentTasks,
                model, avatarUrl, null, null);
    }

    Models.Agent updateAgent(String workspaceId, Models.Agent agent, String name, String description, String instructions,
                             String runtimeId, String visibility, int maxConcurrentTasks, String model, String avatarUrl,
                             Map<String, String> customEnv, List<String> customArgs) throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("description", description == null ? "" : description);
        body.put("instructions", instructions == null || instructions.trim().isEmpty() ? "Help with workspace tasks." : instructions);
        if (runtimeId == null || runtimeId.trim().isEmpty()) body.put("runtime_id", JSONObject.NULL);
        else body.put("runtime_id", runtimeId);
        body.put("visibility", visibility == null || visibility.isEmpty() ? "private" : visibility);
        body.put("max_concurrent_tasks", Math.max(1, maxConcurrentTasks));
        body.put("model", model == null ? "" : model);
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) body.put("avatar_url", JSONObject.NULL);
        else body.put("avatar_url", avatarUrl.trim());
        putCustomAgentConfig(body, customEnv, customArgs);
        return new Models.Agent(requestObject("PUT", "/api/agents/" + encPath(agent.id),
                query(null, "workspace_id", workspaceId), body));
    }

    private static void putCustomAgentConfig(JSONObject body, Map<String, String> customEnv, List<String> customArgs) throws Exception {
        if (customEnv != null) {
            JSONObject env = new JSONObject();
            for (Map.Entry<String, String> entry : customEnv.entrySet()) {
                if (entry.getKey() != null && !entry.getKey().trim().isEmpty()) {
                    env.put(entry.getKey().trim(), entry.getValue() == null ? "" : entry.getValue());
                }
            }
            body.put("custom_env", env);
        }
        if (customArgs != null) {
            JSONArray args = new JSONArray();
            for (String arg : customArgs) {
                if (arg != null && !arg.trim().isEmpty()) args.put(arg.trim());
            }
            body.put("custom_args", args);
        }
    }

    List<Models.Skill> agentSkills(String workspaceId, String agentId) throws Exception {
        JSONObject json = requestObject("GET", "/api/agents/" + encPath(agentId) + "/skills",
                query(null, "workspace_id", workspaceId), null);
        return parseArray(extractArray(json, "skills"), Models.Skill::new);
    }

    void setAgentSkills(String workspaceId, String agentId, List<String> skillIds) throws Exception {
        JSONObject body = new JSONObject();
        JSONArray ids = new JSONArray();
        for (String id : skillIds) ids.put(id);
        body.put("skill_ids", ids);
        requestObject("PUT", "/api/agents/" + encPath(agentId) + "/skills",
                query(null, "workspace_id", workspaceId), body);
    }

    Models.Agent archiveAgent(String workspaceId, String agentId) throws Exception {
        return new Models.Agent(requestObject("POST", "/api/agents/" + encPath(agentId) + "/archive",
                query(null, "workspace_id", workspaceId), null));
    }

    Models.Agent restoreAgent(String workspaceId, String agentId) throws Exception {
        return new Models.Agent(requestObject("POST", "/api/agents/" + encPath(agentId) + "/restore",
                query(null, "workspace_id", workspaceId), null));
    }

    String uploadFile(String workspaceId, String filename, String contentType, byte[] data) throws Exception {
        JSONObject json = requestMultipart("/api/upload-file", query(null, "workspace_id", workspaceId),
                "file", filename, contentType, data);
        return json.optString("url", json.optString("link", ""));
    }

    int cancelAgentTasks(String workspaceId, String agentId) throws Exception {
        JSONObject json = requestObject("POST", "/api/agents/" + encPath(agentId) + "/cancel-tasks",
                query(null, "workspace_id", workspaceId), null);
        return json.optInt("cancelled", json.optInt("count", 0));
    }

    private JSONObject requestMultipart(String path, JSONObject query, String fieldName, String filename,
                                        String contentType, byte[] data) throws Exception {
        return requestMultipart(path, query, null, fieldName, filename, contentType, data);
    }

    private JSONObject requestMultipart(String path, JSONObject query, JSONObject fields, String fieldName, String filename,
                                        String contentType, byte[] data) throws Exception {
        String boundary = "Boundary-" + System.currentTimeMillis();
        URL url = new URL(BASE + path + queryString(query));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("X-Client-Platform", "android");
        conn.setRequestProperty("X-Client-Version", "debug");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        String token = authStore.token();
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setDoOutput(true);
        try (OutputStream out = conn.getOutputStream()) {
            if (fields != null) {
                JSONArray names = fields.names();
                if (names != null) {
                    for (int i = 0; i < names.length(); i++) {
                        String key = names.getString(i);
                        Object value = fields.opt(key);
                        if (value == null || value == JSONObject.NULL) continue;
                        writeUtf8(out, "--" + boundary + "\r\n");
                        writeUtf8(out, "Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
                        writeUtf8(out, String.valueOf(value));
                        writeUtf8(out, "\r\n");
                    }
                }
            }
            writeUtf8(out, "--" + boundary + "\r\n");
            writeUtf8(out, "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n");
            writeUtf8(out, "Content-Type: " + (contentType == null || contentType.isEmpty() ? "application/octet-stream" : contentType) + "\r\n\r\n");
            out.write(data);
            writeUtf8(out, "\r\n--" + boundary + "--\r\n");
        }

        int code = conn.getResponseCode();
        saveResponseCookies(conn);
        InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String text = readAll(stream);
        if (code < 200 || code >= 300) {
            throw new ApiException(code, text);
        }
        if (text == null || text.trim().isEmpty()) return new JSONObject();
        return new JSONObject(text.trim());
    }

    private static void writeUtf8(OutputStream out, String value) throws Exception {
        out.write(value.getBytes(StandardCharsets.UTF_8));
    }

    private JSONObject requestObject(String method, String path, JSONObject query, JSONObject body) throws Exception {
        return requestObject(method, path, query, body, null);
    }

    private JSONObject requestObject(String method, String path, JSONObject query, JSONObject body, Map<String, String> extraHeaders) throws Exception {
        return requestObject(method, path, query, body, extraHeaders, 15000, 30000);
    }

    private JSONObject requestObject(String method, String path, JSONObject query, JSONObject body,
                                     Map<String, String> extraHeaders, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        URL url = new URL(BASE + path + queryString(query));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(connectTimeoutMs);
        conn.setReadTimeout(readTimeoutMs);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("X-Client-Platform", "android");
        conn.setRequestProperty("X-Client-Version", "debug");
        String token = authStore.token();
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        if (extraHeaders != null) {
            for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        }
        if (body != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(bytes);
            }
        }

        int code = conn.getResponseCode();
        saveResponseCookies(conn);
        InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String text = readAll(stream);
        if (code < 200 || code >= 300) {
            throw new ApiException(code, text);
        }
        if (text == null || text.trim().isEmpty()) return new JSONObject();
        String trimmed = text.trim();
        if (trimmed.startsWith("[")) {
            JSONObject wrapper = new JSONObject();
            wrapper.put("items", new JSONArray(trimmed));
            return wrapper;
        }
        return new JSONObject(trimmed);
    }

    private void saveResponseCookies(HttpURLConnection conn) {
        if (conn == null) return;
        Map<String, List<String>> headers = conn.getHeaderFields();
        List<String> cookies = headers == null ? null : headers.get("Set-Cookie");
        if (cookies == null && headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("Set-Cookie")) {
                    cookies = entry.getValue();
                    break;
                }
            }
        }
        authStore.saveCloudFrontCookies(cookies);
    }

    private static String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private static String queryString(JSONObject query) throws Exception {
        if (query == null || query.length() == 0) return "";
        StringBuilder sb = new StringBuilder("?");
        boolean first = true;
        JSONArray names = query.names();
        if (names == null) return "";
        for (int i = 0; i < names.length(); i++) {
            String key = names.getString(i);
            Object value = query.opt(key);
            if (value == null || value == JSONObject.NULL) continue;
            if (!first) sb.append('&');
            first = false;
            sb.append(URLEncoder.encode(key, "UTF-8"));
            sb.append('=');
            sb.append(URLEncoder.encode(String.valueOf(value), "UTF-8"));
        }
        return first ? "" : sb.toString();
    }

    private static JSONObject query(JSONObject seed, Object... pairs) throws Exception {
        JSONObject obj = seed == null ? new JSONObject() : seed;
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            String key = String.valueOf(pairs[i]);
            Object value = pairs[i + 1];
            if ("workspace_id".equals(key) && isBlankQueryValue(value)) {
                throw new IllegalArgumentException("workspace_id is required. Select a workspace first.");
            }
            obj.put(key, value);
        }
        return obj;
    }

    private static boolean isBlankQueryValue(Object value) {
        if (value == null || value == JSONObject.NULL) return true;
        return String.valueOf(value).trim().isEmpty();
    }

    private static String encPath(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
    }

    private static JSONArray extractArray(JSONObject json, String preferredKey) {
        JSONArray direct = json.optJSONArray(preferredKey);
        if (direct != null) return direct;
        JSONArray items = json.optJSONArray("items");
        if (items != null) return items;
        JSONArray data = json.optJSONArray("data");
        return data == null ? new JSONArray() : data;
    }

    private static JSONObject unwrap(JSONObject json, String preferredKey) {
        JSONObject nested = json.optJSONObject(preferredKey);
        if (nested != null) return nested;
        JSONObject data = json.optJSONObject("data");
        return data == null ? json : data;
    }

    private static <T> List<T> parseArray(JSONArray array, Parser<T> parser) throws Exception {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) list.add(parser.parse(array.getJSONObject(i)));
        return list;
    }

    private static <T> Models.Page<T> parsePage(JSONObject json, String key, Parser<T> parser) throws Exception {
        List<T> list = parseArray(extractArray(json, key), parser);
        boolean hasMore = json.optBoolean("has_more", json.optBoolean("hasMore", false));
        return new Models.Page<>(list, hasMore);
    }

    static final class ApiException extends Exception {
        final int code;
        final String response;

        ApiException(int code, String response) {
            super("HTTP " + code + ": " + response);
            this.code = code;
            this.response = response;
        }
    }
}
