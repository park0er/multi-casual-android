package ai.multica.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Models {
    static final String[] STATUS_VALUES = {"backlog", "todo", "in_progress", "in_review", "done", "blocked", "cancelled"};
    static final String[] PRIORITY_VALUES = {"urgent", "high", "medium", "low", "none"};
    static final String[] PROJECT_STATUS_VALUES = {"planned", "in_progress", "paused", "completed", "cancelled"};

    static String statusLabel(String value, boolean zh) {
        switch (value == null ? "" : value) {
            case "backlog": return zh ? "待规划" : "Backlog";
            case "todo": return zh ? "待处理" : "Todo";
            case "in_progress": return zh ? "进行中" : "In Progress";
            case "in_review": return zh ? "评审中" : "In Review";
            case "done": return zh ? "已完成" : "Done";
            case "blocked": return zh ? "阻塞" : "Blocked";
            case "cancelled": return zh ? "已取消" : "Cancelled";
            default: return value == null ? "" : value;
        }
    }

    static String priorityLabel(String value, boolean zh) {
        switch (value == null ? "" : value) {
            case "urgent": return zh ? "紧急" : "Urgent";
            case "high": return zh ? "高" : "High";
            case "medium": return zh ? "中" : "Medium";
            case "low": return zh ? "低" : "Low";
            case "none":
            case "no_priority": return zh ? "无优先级" : "No Priority";
            default: return value == null ? "" : value;
        }
    }

    static String projectStatusLabel(String value, boolean zh) {
        switch (value == null ? "" : value) {
            case "planned": return zh ? "已规划" : "Planned";
            case "in_progress": return zh ? "进行中" : "In Progress";
            case "paused": return zh ? "已暂停" : "Paused";
            case "completed": return zh ? "已完成" : "Completed";
            case "cancelled": return zh ? "已取消" : "Cancelled";
            default: return value == null ? "" : value;
        }
    }

    static String cleanString(JSONObject json, String key, String fallback) {
        String value = json.optString(key, fallback);
        return value == null || value.isEmpty() || "null".equals(value) ? fallback : value;
    }

    static String shortId(String id) {
        if (id == null || id.length() <= 8) return id == null ? "" : id;
        return id.substring(0, 8);
    }

    static JSONObject objectWith(String key, String value) {
        try {
            return new JSONObject().put(key, value);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    static List<Reaction> reactions(JSONObject json) {
        List<Reaction> out = new ArrayList<>();
        JSONArray array = json.optJSONArray("reactions");
        if (array == null) return out;
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null) out.add(new Reaction(item));
        }
        return out;
    }

    static List<IssueLabel> issueLabels(JSONObject json) {
        List<IssueLabel> out = new ArrayList<>();
        JSONArray array = json.optJSONArray("labels");
        if (array == null) return out;
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null) out.add(new IssueLabel(item));
        }
        return out;
    }

    static String commentAuthorDisplayName(Comment comment, List<Member> members, List<Agent> agents, User currentUser) {
        if (comment == null) return "";
        if (comment.authorName != null && !comment.authorName.isEmpty()) return comment.authorName;
        String authorId = comment.authorId;
        if (authorId != null && !authorId.isEmpty()) {
            if ("agent".equals(comment.authorType)) {
                for (Agent agent : agents) {
                    if (authorId.equals(agent.id)) return agent.name;
                }
            } else {
                for (Member member : members) {
                    if (authorId.equals(member.id) || authorId.equals(member.userId)) return member.displayName;
                }
                if (currentUser != null && authorId.equals(currentUser.id)) return currentUser.name;
            }
            for (Agent agent : agents) {
                if (authorId.equals(agent.id)) return agent.name;
            }
            for (Member member : members) {
                if (authorId.equals(member.id) || authorId.equals(member.userId)) return member.displayName;
            }
            return (comment.authorType == null || comment.authorType.isEmpty() ? "" : comment.authorType + " ")
                    + shortId(authorId);
        }
        return comment.authorType == null ? "" : comment.authorType;
    }

    static String commentAuthorAvatarUrl(Comment comment, List<Member> members, List<Agent> agents, User currentUser) {
        if (comment == null) return "";
        if (comment.authorAvatarUrl != null && !comment.authorAvatarUrl.isEmpty()) return comment.authorAvatarUrl;
        String authorId = comment.authorId;
        if (authorId == null || authorId.isEmpty()) return "";
        if ("agent".equals(comment.authorType)) {
            for (Agent agent : agents) {
                if (authorId.equals(agent.id)) return agent.avatarUrl;
            }
        } else {
            if (currentUser != null && authorId.equals(currentUser.id)) return currentUser.avatarUrl;
            for (Member member : members) {
                if (authorId.equals(member.id) || authorId.equals(member.userId)) return member.avatarUrl;
            }
        }
        for (Agent agent : agents) {
            if (authorId.equals(agent.id)) return agent.avatarUrl;
        }
        for (Member member : members) {
            if (authorId.equals(member.id) || authorId.equals(member.userId)) return member.avatarUrl;
        }
        return "";
    }

    static String issueSubscriberDisplayName(IssueSubscriber subscriber, List<Member> members, List<Agent> agents, User currentUser) {
        if (subscriber == null) return "";
        String userId = subscriber.userId;
        if (userId != null && !userId.isEmpty()) {
            if ("agent".equals(subscriber.userType)) {
                for (Agent agent : agents) {
                    if (userId.equals(agent.id)) return agent.name;
                }
            } else {
                if (currentUser != null && userId.equals(currentUser.id)) return currentUser.name;
                for (Member member : members) {
                    if (userId.equals(member.id)) return member.displayName;
                }
            }
            for (Agent agent : agents) {
                if (userId.equals(agent.id)) return agent.name;
            }
            for (Member member : members) {
                if (userId.equals(member.id)) return member.displayName;
            }
            return (subscriber.userType == null || subscriber.userType.isEmpty() ? "" : subscriber.userType + " ")
                    + shortId(userId);
        }
        return subscriber.userType == null ? "" : subscriber.userType;
    }

    static String issueAssigneeDisplayName(Issue issue, List<Member> members, List<Agent> agents, User currentUser) {
        if (issue == null || issue.assigneeId == null || issue.assigneeId.isEmpty()) return "";
        String assigneeId = issue.assigneeId;
        if ("agent".equals(issue.assigneeType)) {
            for (Agent agent : agents) {
                if (assigneeId.equals(agent.id)) return agent.name;
            }
        } else {
            if (currentUser != null && assigneeId.equals(currentUser.id)) return currentUser.name;
            for (Member member : members) {
                if (assigneeId.equals(member.id) || assigneeId.equals(member.userId)) return member.displayName;
            }
        }
        for (Agent agent : agents) {
            if (assigneeId.equals(agent.id)) return agent.name;
        }
        for (Member member : members) {
            if (assigneeId.equals(member.id) || assigneeId.equals(member.userId)) return member.displayName;
        }
        String prefix;
        if ("agent".equals(issue.assigneeType)) {
            prefix = "Agent ";
        } else if ("member".equals(issue.assigneeType)) {
            prefix = "Member ";
        } else {
            prefix = "";
        }
        return prefix + shortId(assigneeId);
    }

    static List<Assignee> issueAssignees(boolean includeEmpty, String emptyLabel, User currentUser,
                                         List<Member> members, List<Agent> agents) {
        List<Assignee> list = new ArrayList<>();
        List<String> seenMemberIds = new ArrayList<>();
        if (includeEmpty) list.add(new Assignee(null, null, emptyLabel));
        if (currentUser != null && currentUser.id != null && !currentUser.id.isEmpty()) {
            list.add(new Assignee(currentUser.id, "member", currentUser.name));
            seenMemberIds.add(currentUser.id);
        }
        for (Member member : members) {
            if (member.id == null || member.id.isEmpty() || seenMemberIds.contains(member.id)) continue;
            list.add(new Assignee(member.id, "member", member.displayName));
            seenMemberIds.add(member.id);
        }
        for (Agent agent : agents) {
            if (agent.id == null || agent.id.isEmpty()) continue;
            list.add(new Assignee(agent.id, "agent", agent.name));
        }
        return list;
    }

    static final class Page<T> {
        final List<T> items;
        final boolean hasMore;

        Page(List<T> items, boolean hasMore) {
            this.items = items;
            this.hasMore = hasMore;
        }
    }

    static final class User {
        final String id;
        final String email;
        final String name;
        final String avatarUrl;
        final String language;
        final String onboardedAt;
        final String starterContentState;
        final JSONObject onboardingQuestionnaire;

        User(JSONObject json) {
            id = json.optString("id");
            email = json.optString("email");
            name = json.optString("name", email);
            avatarUrl = cleanString(json, "avatar_url", cleanString(json, "avatarUrl", ""));
            language = cleanString(json, "language", "");
            onboardedAt = cleanString(json, "onboarded_at", "");
            starterContentState = cleanString(json, "starter_content_state", "");
            JSONObject questionnaire = json.optJSONObject("onboarding_questionnaire");
            onboardingQuestionnaire = questionnaire == null ? new JSONObject() : questionnaire;
        }
    }

    static final class StarterContentImportResponse {
        final User user;
        final String projectId;
        final String welcomeIssueId;

        StarterContentImportResponse(JSONObject json) {
            JSONObject userJson = json.optJSONObject("user");
            user = new User(userJson == null ? new JSONObject() : userJson);
            projectId = cleanString(json, "project_id", "");
            welcomeIssueId = cleanString(json, "welcome_issue_id", "");
        }
    }

    static final class Workspace {
        final String id;
        final String name;
        final String slug;
        final String description;
        final String context;
        final String issuePrefix;
        final String createdAt;
        final String updatedAt;
        final List<WorkspaceRepo> repos;
        final JSONObject settings;

        Workspace(JSONObject json) {
            id = json.optString("id");
            name = json.optString("name", json.optString("slug"));
            slug = json.optString("slug");
            description = cleanString(json, "description", "");
            context = cleanString(json, "context", "");
            issuePrefix = json.optString("issue_prefix");
            createdAt = cleanString(json, "created_at", "");
            updatedAt = cleanString(json, "updated_at", "");
            repos = new ArrayList<>();
            JSONArray repoArray = json.optJSONArray("repos");
            if (repoArray != null) {
                for (int i = 0; i < repoArray.length(); i++) {
                    JSONObject repo = repoArray.optJSONObject(i);
                    if (repo != null) repos.add(new WorkspaceRepo(repo));
                }
            }
            JSONObject rawSettings = json.optJSONObject("settings");
            settings = rawSettings == null ? new JSONObject() : rawSettings;
        }

        boolean coAuthoredByEnabled() {
            return !settings.has("co_authored_by_enabled") || settings.optBoolean("co_authored_by_enabled", true);
        }
    }

    static final class WorkspaceRepo {
        final String url;

        WorkspaceRepo(JSONObject json) {
            url = cleanString(json, "url", "");
        }
    }

    static final class Member {
        final String id;
        final String userId;
        final String name;
        final String email;
        final String role;
        final String displayName;
        final String avatarUrl;

        Member(JSONObject json) {
            id = json.optString("id");
            userId = json.optString("user_id");
            name = cleanString(json, "name", "");
            email = cleanString(json, "email", "");
            role = cleanString(json, "role", "");
            avatarUrl = cleanString(json, "avatar_url", cleanString(json, "avatarUrl", ""));
            displayName = !name.isEmpty() ? name : (!email.isEmpty() ? email : shortId(id));
        }
    }

    static final class Issue {
        String id;
        String identifier;
        int number;
        String title;
        String description;
        String status;
        String priority;
        String assigneeId;
        String assigneeType;
        String parentIssueId;
        String projectId;
        String workspaceId;
        String dueDate;
        String createdAt;
        String updatedAt;
        String matchSource;
        String matchedSnippet;
        final List<Reaction> reactions;
        final List<IssueLabel> labels;

        Issue(JSONObject json) {
            id = json.optString("id");
            identifier = json.optString("identifier", "#" + json.optInt("number"));
            number = json.optInt("number");
            title = json.optString("title");
            description = cleanString(json, "description", "");
            status = json.optString("status", "todo");
            priority = json.optString("priority", "medium");
            if ("no_priority".equals(priority)) priority = "none";
            assigneeId = json.optString("assignee_id", null);
            if ("null".equals(assigneeId) || assigneeId != null && assigneeId.isEmpty()) assigneeId = null;
            assigneeType = json.optString("assignee_type", null);
            if ("null".equals(assigneeType) || assigneeType != null && assigneeType.isEmpty()) assigneeType = null;
            parentIssueId = json.optString("parent_issue_id", null);
            if ("null".equals(parentIssueId) || parentIssueId != null && parentIssueId.isEmpty()) parentIssueId = null;
            projectId = json.optString("project_id", null);
            if ("null".equals(projectId) || projectId != null && projectId.isEmpty()) projectId = null;
            workspaceId = json.optString("workspace_id");
            dueDate = cleanString(json, "due_date", "");
            createdAt = json.optString("created_at");
            updatedAt = json.optString("updated_at");
            matchSource = cleanString(json, "match_source", "");
            matchedSnippet = cleanString(json, "matched_snippet", "");
            reactions = reactions(json);
            labels = issueLabels(json);
        }
    }

    static final class QuickCreateTask {
        final String taskId;

        QuickCreateTask(JSONObject json) {
            taskId = cleanString(json, "task_id", "");
        }
    }

    static final class NotificationPreferences {
        final String workspaceId;
        final Map<String, String> values;

        NotificationPreferences(JSONObject json) {
            workspaceId = cleanString(json, "workspace_id", "");
            values = new LinkedHashMap<>();
            JSONObject preferences = json.optJSONObject("preferences");
            if (preferences != null) {
                org.json.JSONArray names = preferences.names();
                if (names != null) {
                    for (int i = 0; i < names.length(); i++) {
                        String key = names.optString(i);
                        if (key != null && !key.isEmpty()) values.put(key, preferences.optString(key, "all"));
                    }
                }
            }
        }

        String value(String key) {
            String value = values.get(key);
            return value == null || value.isEmpty() ? "all" : value;
        }
    }

    static final class Invitation {
        final String id;
        final String workspaceId;
        final String inviteeEmail;
        final String inviteeUserId;
        final String role;
        final String status;
        final String inviterName;
        final String inviterEmail;
        final String workspaceName;
        final String createdAt;
        final String updatedAt;
        final String expiresAt;

        Invitation(JSONObject json) {
            id = cleanString(json, "id", "");
            workspaceId = cleanString(json, "workspace_id", "");
            inviteeEmail = cleanString(json, "invitee_email", cleanString(json, "email", ""));
            inviteeUserId = cleanString(json, "invitee_user_id", "");
            role = cleanString(json, "role", "member");
            status = cleanString(json, "status", "");
            inviterName = cleanString(json, "inviter_name", "");
            inviterEmail = cleanString(json, "inviter_email", "");
            workspaceName = cleanString(json, "workspace_name", "");
            createdAt = cleanString(json, "created_at", "");
            updatedAt = cleanString(json, "updated_at", "");
            expiresAt = cleanString(json, "expires_at", "");
        }
    }

    static final class Comment {
        final String id;
        final String content;
        final String authorId;
        final String authorType;
        final String parentId;
        final String issueId;
        final String createdAt;
        final String authorName;
        final String authorAvatarUrl;
        final List<Attachment> attachments;
        final List<Reaction> reactions;

        Comment(JSONObject json) {
            id = json.optString("id");
            content = json.optString("content");
            authorId = cleanString(json, "author_id", cleanString(json, "actor_id", ""));
            authorType = cleanString(json, "author_type", cleanString(json, "actor_type", "member"));
            parentId = json.optString("parent_id", null);
            issueId = json.optString("issue_id");
            createdAt = json.optString("created_at");
            JSONObject author = json.optJSONObject("author");
            authorName = author == null ? json.optString("author_name", "") : author.optString("name", author.optString("email"));
            authorAvatarUrl = author == null
                    ? cleanString(json, "author_avatar_url", cleanString(json, "avatar_url", ""))
                    : cleanString(author, "avatar_url", cleanString(author, "avatarUrl", ""));
            attachments = new ArrayList<>();
            org.json.JSONArray array = json.optJSONArray("attachments");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.optJSONObject(i);
                    if (item != null) attachments.add(new Attachment(item));
                }
            }
            reactions = reactions(json);
        }
    }

    static final class Reaction {
        final String id;
        final String targetId;
        final String actorId;
        final String actorType;
        final String emoji;
        final String createdAt;

        Reaction(JSONObject json) {
            id = json.optString("id");
            targetId = json.optString("comment_id", json.optString("issue_id"));
            actorId = json.optString("actor_id");
            actorType = json.optString("actor_type");
            emoji = json.optString("emoji");
            createdAt = json.optString("created_at");
        }
    }

    static final class IssueSubscriber {
        final String issueId;
        final String userType;
        final String userId;
        final String reason;
        final String createdAt;

        IssueSubscriber(JSONObject json) {
            issueId = json.optString("issue_id");
            userType = json.optString("user_type");
            userId = json.optString("user_id");
            reason = json.optString("reason", "manual");
            createdAt = json.optString("created_at");
        }
    }

    static final class Attachment {
        final String id;
        final String workspaceId;
        final String issueId;
        final String commentId;
        final String uploaderType;
        final String uploaderId;
        final String filename;
        final String url;
        final String downloadUrl;
        final String contentType;
        final long sizeBytes;
        final String createdAt;

        Attachment(JSONObject json) {
            id = json.optString("id");
            workspaceId = json.optString("workspace_id");
            issueId = cleanString(json, "issue_id", "");
            commentId = cleanString(json, "comment_id", "");
            uploaderType = cleanString(json, "uploader_type", "");
            uploaderId = cleanString(json, "uploader_id", "");
            filename = cleanString(json, "filename", shortId(id));
            url = cleanString(json, "url", "");
            downloadUrl = cleanString(json, "download_url", url);
            contentType = cleanString(json, "content_type", "");
            sizeBytes = json.optLong("size_bytes", 0L);
            createdAt = json.optString("created_at");
        }
    }

    static final class InboxItem {
        final String id;
        final String issueId;
        final String issueIdentifier;
        final String issueTitle;
        final String type;
        final String body;
        final String issueStatus;
        final String commentId;
        final boolean read;
        final boolean archived;
        final String createdAt;

        InboxItem(JSONObject json) {
            id = json.optString("id");
            JSONObject details = json.optJSONObject("details");
            issueId = json.optString("issue_id", details == null ? "" : details.optString("issue_id", details.optString("id")));
            issueIdentifier = json.optString("issue_identifier", details == null ? issueId : details.optString("identifier", issueId));
            issueTitle = json.optString("issue_title", json.optString("title", details == null ? "" : details.optString("title")));
            type = json.optString("type", "notification");
            body = json.optString("body", "");
            issueStatus = json.optString("issue_status", details == null ? "" : details.optString("status"));
            commentId = details == null ? "" : cleanString(details, "comment_id", "");
            read = json.optBoolean("read", false);
            archived = json.optBoolean("archived", false);
            createdAt = json.optString("created_at");
        }
    }

    static final class Project {
        final String id;
        final String name;
        final String description;
        final String icon;
        final String status;
        final String priority;
        final String leadType;
        final String leadId;
        final String workspaceId;
        final String createdAt;
        final int issueCount;
        final int doneCount;
        final String matchSource;
        final String matchedSnippet;

        Project(JSONObject json) {
            id = json.optString("id");
            name = cleanString(json, "name", cleanString(json, "title", shortId(id)));
            description = json.optString("description", "");
            icon = cleanString(json, "icon", "");
            status = cleanString(json, "status", "planned");
            priority = cleanString(json, "priority", "none");
            leadType = cleanString(json, "lead_type", "");
            leadId = cleanString(json, "lead_id", "");
            workspaceId = json.optString("workspace_id");
            createdAt = json.optString("created_at");
            issueCount = json.optInt("issue_count", 0);
            doneCount = json.optInt("done_count", 0);
            matchSource = cleanString(json, "match_source", "");
            matchedSnippet = cleanString(json, "matched_snippet", "");
        }
    }

    static final class ProjectResource {
        final String id;
        final String resourceType;
        final String displayTitle;
        final String url;
        final int position;

        ProjectResource(JSONObject json) {
            id = json.optString("id");
            resourceType = json.optString("resource_type", json.optString("type", ""));
            position = json.optInt("position", 0);
            JSONObject ref = json.optJSONObject("resource_ref");
            url = ref == null ? "" : ref.optString("url", "");
            String title = json.optString("title", "");
            if (title.isEmpty()) title = json.optString("name", "");
            if (title.isEmpty()) title = url;
            displayTitle = title.isEmpty() ? shortId(id) : title;
        }
    }

    static final class PinnedItem {
        final String id;
        final String workspaceId;
        final String userId;
        final String itemType;
        final String itemId;
        final int position;
        final String createdAt;

        PinnedItem(JSONObject json) {
            id = json.optString("id");
            workspaceId = json.optString("workspace_id");
            userId = json.optString("user_id");
            itemType = json.optString("item_type");
            itemId = json.optString("item_id");
            position = json.optInt("position", 0);
            createdAt = json.optString("created_at");
        }
    }

    static final class AgentTask {
        final String id;
        final String agentId;
        final String issueId;
        final String status;
        final String startedAt;
        final String completedAt;
        final String createdAt;
        final String error;
        final String failureReason;
        final String kind;
        final String triggerSummary;
        final String workDir;

        AgentTask(JSONObject json) {
            id = json.optString("id");
            agentId = cleanString(json, "agent_id", "");
            issueId = json.optString("issue_id");
            status = json.optString("status");
            startedAt = cleanString(json, "started_at", "");
            completedAt = cleanString(json, "completed_at", "");
            createdAt = cleanString(json, "created_at", "");
            error = cleanString(json, "error", "");
            failureReason = cleanString(json, "failure_reason", "");
            kind = cleanString(json, "kind", "");
            triggerSummary = cleanString(json, "trigger_summary", "");
            workDir = cleanString(json, "work_dir", "");
        }
    }

    static final class AgentActivityBucket {
        final String agentId;
        final String bucketAt;
        final int taskCount;
        final int failedCount;

        AgentActivityBucket(JSONObject json) {
            agentId = cleanString(json, "agent_id", "");
            bucketAt = cleanString(json, "bucket_at", "");
            taskCount = json.optInt("task_count", 0);
            failedCount = json.optInt("failed_count", 0);
        }
    }

    static final class AgentRunCount {
        final String agentId;
        final int runCount;

        AgentRunCount(JSONObject json) {
            agentId = cleanString(json, "agent_id", "");
            runCount = json.optInt("run_count", 0);
        }
    }

    static final class TaskMessage {
        final String id;
        final int seq;
        final String type;
        final String tool;
        final String content;
        final String output;
        final String createdAt;

        TaskMessage(JSONObject json) {
            id = json.optString("id");
            seq = json.optInt("seq");
            type = json.optString("type");
            tool = json.optString("tool");
            content = json.optString("content");
            output = json.optString("output");
            createdAt = cleanString(json, "created_at", cleanString(json, "timestamp", ""));
        }
    }

    static final class Agent {
        final String id;
        final String name;
        final String description;
        final String instructions;
        final String runtimeId;
        final String status;
        final String visibility;
        final int maxConcurrentTasks;
        final String model;
        final String avatarUrl;
        final String ownerId;
        final String archivedAt;
        final Map<String, String> customEnv;
        final List<String> customArgs;
        final boolean customEnvRedacted;

        Agent(JSONObject json) {
            id = json.optString("id");
            name = json.optString("name", json.optString("slug", shortId(id)));
            description = json.optString("description", "");
            instructions = json.optString("instructions", description);
            runtimeId = json.optString("runtime_id", "");
            status = json.optString("status", json.optString("state", ""));
            visibility = json.optString("visibility", "private");
            maxConcurrentTasks = json.optInt("max_concurrent_tasks", 1);
            model = json.optString("model", "");
            avatarUrl = cleanString(json, "avatar_url", cleanString(json, "avatarUrl", ""));
            ownerId = cleanString(json, "owner_id", "");
            archivedAt = cleanString(json, "archived_at", "");
            customEnv = stringMap(json.optJSONObject("custom_env"));
            customArgs = stringList(json.optJSONArray("custom_args"));
            customEnvRedacted = json.optBoolean("custom_env_redacted", false);
        }
    }

    static final class Runtime {
        final String id;
        final String workspaceId;
        final String name;
        final String status;
        final String version;
        final String provider;
        final String runtimeMode;
        final String daemonId;
        final String deviceInfo;
        final String ownerId;
        final String lastSeenAt;

        Runtime(JSONObject json) {
            id = json.optString("id");
            workspaceId = json.optString("workspace_id", "");
            name = json.optString("name", shortId(id));
            status = json.optString("status", "");
            version = json.optString("version", "");
            provider = json.optString("provider", "");
            runtimeMode = json.optString("runtime_mode", "");
            daemonId = cleanString(json, "daemon_id", "");
            deviceInfo = cleanString(json, "device_info", "");
            ownerId = cleanString(json, "owner_id", "");
            lastSeenAt = cleanString(json, "last_seen_at", "");
        }
    }

    static final class RuntimeUsage {
        final String runtimeId;
        final String date;
        final String provider;
        final String model;
        final long inputTokens;
        final long outputTokens;
        final long cacheReadTokens;
        final long cacheWriteTokens;

        RuntimeUsage(JSONObject json) {
            runtimeId = json.optString("runtime_id", "");
            date = json.optString("date", "");
            provider = json.optString("provider", "");
            model = json.optString("model", "");
            inputTokens = json.optLong("input_tokens", json.optLong("total_input_tokens", 0));
            outputTokens = json.optLong("output_tokens", json.optLong("total_output_tokens", 0));
            cacheReadTokens = json.optLong("cache_read_tokens", json.optLong("total_cache_read_tokens", 0));
            cacheWriteTokens = json.optLong("cache_write_tokens", json.optLong("total_cache_write_tokens", 0));
        }

        long totalTokens() {
            return inputTokens + outputTokens + cacheReadTokens + cacheWriteTokens;
        }
    }

    static final class IssueUsage {
        final long inputTokens;
        final long outputTokens;
        final long cacheReadTokens;
        final long cacheWriteTokens;
        final int taskCount;

        IssueUsage(JSONObject json) {
            inputTokens = json.optLong("input_tokens", json.optLong("total_input_tokens", 0));
            outputTokens = json.optLong("output_tokens", json.optLong("total_output_tokens", 0));
            cacheReadTokens = json.optLong("cache_read_tokens", json.optLong("total_cache_read_tokens", 0));
            cacheWriteTokens = json.optLong("cache_write_tokens", json.optLong("total_cache_write_tokens", 0));
            taskCount = json.optInt("task_count", 0);
        }

        long totalTokens() {
            return inputTokens + outputTokens + cacheReadTokens + cacheWriteTokens;
        }
    }

    static final class TimelineEntry {
        final String type;
        final String id;
        final String actorType;
        final String actorId;
        final String createdAt;
        final String action;
        final String content;
        final String parentId;

        TimelineEntry(JSONObject json) {
            type = json.optString("type", "");
            id = json.optString("id", "");
            actorType = json.optString("actor_type", "");
            actorId = json.optString("actor_id", "");
            createdAt = json.optString("created_at", "");
            action = cleanString(json, "action", "");
            content = cleanString(json, "content", "");
            parentId = cleanString(json, "parent_id", "");
        }
    }

    static final class TimelinePage {
        final List<TimelineEntry> entries;
        final String nextCursor;
        final String prevCursor;
        final boolean hasMoreBefore;
        final boolean hasMoreAfter;
        final int targetIndex;

        TimelinePage(JSONObject json) throws Exception {
            entries = new ArrayList<>();
            JSONArray array = json.optJSONArray("entries");
            if (array == null) array = json.optJSONArray("items");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    entries.add(new TimelineEntry(array.getJSONObject(i)));
                }
            }
            nextCursor = cleanString(json, "next_cursor", "");
            prevCursor = cleanString(json, "prev_cursor", "");
            hasMoreBefore = json.optBoolean("has_more_before", false);
            hasMoreAfter = json.optBoolean("has_more_after", false);
            targetIndex = json.has("target_index") && !json.isNull("target_index") ? json.optInt("target_index", -1) : -1;
        }
    }

    static final class RuntimeHourlyActivity {
        final String hour;
        final int totalTasks;

        RuntimeHourlyActivity(JSONObject json) {
            hour = json.optString("hour", String.valueOf(json.optInt("hour", 0)));
            totalTasks = json.optInt("total_tasks", json.optInt("count", json.optInt("task_count", 0)));
        }
    }

    static final class RuntimeUsageByAgent {
        final String agentId;
        final String agentName;
        final String model;
        final long inputTokens;
        final long outputTokens;
        final long cacheReadTokens;
        final long cacheWriteTokens;
        final int taskCount;

        RuntimeUsageByAgent(JSONObject json) {
            agentId = json.optString("agent_id", "");
            agentName = cleanString(json, "agent_name", "");
            model = json.optString("model", "");
            inputTokens = json.optLong("input_tokens", json.optLong("total_input_tokens", 0));
            outputTokens = json.optLong("output_tokens", json.optLong("total_output_tokens", 0));
            cacheReadTokens = json.optLong("cache_read_tokens", json.optLong("total_cache_read_tokens", 0));
            cacheWriteTokens = json.optLong("cache_write_tokens", json.optLong("total_cache_write_tokens", 0));
            taskCount = json.optInt("task_count", 0);
        }

        long totalTokens() {
            return inputTokens + outputTokens + cacheReadTokens + cacheWriteTokens;
        }
    }

    static final class RuntimeUsageByHour {
        final String hour;
        final String model;
        final long inputTokens;
        final long outputTokens;
        final long cacheReadTokens;
        final long cacheWriteTokens;

        RuntimeUsageByHour(JSONObject json) {
            hour = json.optString("hour", String.valueOf(json.optInt("hour", 0)));
            model = json.optString("model", "");
            inputTokens = json.optLong("input_tokens", json.optLong("total_input_tokens", 0));
            outputTokens = json.optLong("output_tokens", json.optLong("total_output_tokens", 0));
            cacheReadTokens = json.optLong("cache_read_tokens", json.optLong("total_cache_read_tokens", 0));
            cacheWriteTokens = json.optLong("cache_write_tokens", json.optLong("total_cache_write_tokens", 0));
        }

        long totalTokens() {
            return inputTokens + outputTokens + cacheReadTokens + cacheWriteTokens;
        }
    }

    static final class RuntimeUpdate {
        final String id;
        final String runtimeId;
        final String status;
        final String targetVersion;
        final String output;
        final String error;

        RuntimeUpdate(JSONObject json) {
            id = json.optString("id", "");
            runtimeId = json.optString("runtime_id", "");
            status = json.optString("status", "unknown");
            targetVersion = cleanString(json, "target_version", "");
            output = cleanString(json, "output", "");
            error = cleanString(json, "error", "");
        }
    }

    static final class RuntimeModelInfo {
        final String id;
        final String name;
        final String provider;

        RuntimeModelInfo(JSONObject json) {
            name = cleanString(json, "name", cleanString(json, "id", ""));
            id = cleanString(json, "id", name);
            provider = cleanString(json, "provider", "");
        }
    }

    static final class RuntimeModelListRequest {
        final String id;
        final String runtimeId;
        final String status;
        final boolean supported;
        final String error;
        final List<RuntimeModelInfo> models;

        RuntimeModelListRequest(JSONObject json) {
            id = json.optString("id", "");
            runtimeId = json.optString("runtime_id", "");
            status = json.optString("status", "unknown");
            supported = json.optBoolean("supported", false);
            error = cleanString(json, "error", "");
            models = new ArrayList<>();
            org.json.JSONArray array = json.optJSONArray("models");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.optJSONObject(i);
                    if (item != null) models.add(new RuntimeModelInfo(item));
                    else models.add(new RuntimeModelInfo(objectWith("name", array.optString(i))));
                }
            }
        }
    }

    static final class RuntimeLocalSkillInfo {
        final String id;
        final String key;
        final String name;
        final String path;
        final String description;
        final String provider;

        RuntimeLocalSkillInfo(JSONObject json) {
            key = cleanString(json, "key", cleanString(json, "id", cleanString(json, "path", "")));
            id = cleanString(json, "id", key);
            path = cleanString(json, "path", "");
            String fallbackName = !path.isEmpty() && path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : key;
            name = cleanString(json, "name", fallbackName);
            description = cleanString(json, "description", "");
            provider = cleanString(json, "provider", "");
        }
    }

    static final class RuntimeLocalSkillListRequest {
        final String id;
        final String runtimeId;
        final String status;
        final boolean supported;
        final String error;
        final List<RuntimeLocalSkillInfo> skills;

        RuntimeLocalSkillListRequest(JSONObject json) {
            id = json.optString("id", "");
            runtimeId = json.optString("runtime_id", "");
            status = json.optString("status", "unknown");
            supported = json.optBoolean("supported", false);
            error = cleanString(json, "error", "");
            skills = new ArrayList<>();
            org.json.JSONArray array = json.optJSONArray("skills");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.optJSONObject(i);
                    if (item != null) skills.add(new RuntimeLocalSkillInfo(item));
                    else skills.add(new RuntimeLocalSkillInfo(objectWith("path", array.optString(i))));
                }
            }
        }
    }

    static final class RuntimeLocalSkillImportRequest {
        final String id;
        final String runtimeId;
        final String skillKey;
        final String name;
        final String description;
        final String status;
        final String error;
        final Skill skill;

        RuntimeLocalSkillImportRequest(JSONObject json) {
            id = json.optString("id", "");
            runtimeId = json.optString("runtime_id", "");
            skillKey = cleanString(json, "skill_key", "");
            name = cleanString(json, "name", "");
            description = cleanString(json, "description", "");
            status = json.optString("status", "unknown");
            error = cleanString(json, "error", "");
            JSONObject skillJson = json.optJSONObject("skill");
            skill = skillJson == null ? null : new Skill(skillJson);
        }
    }

    static final class Skill {
        final String id;
        final String name;
        final String description;
        final String content;
        final String originSourceUrl;
        final String originType;
        final String createdAt;
        final String updatedAt;
        final List<SkillFile> files;

        Skill(JSONObject json) {
            id = json.optString("id");
            name = json.optString("name", shortId(id));
            description = json.optString("description", "");
            content = json.optString("content", "");
            createdAt = cleanString(json, "created_at", "");
            updatedAt = cleanString(json, "updated_at", "");
            JSONObject origin = null;
            JSONObject config = json.optJSONObject("config");
            if (config != null) origin = config.optJSONObject("origin");
            originSourceUrl = origin == null ? "" : origin.optString("source_url", "");
            originType = origin == null ? "" : origin.optString("type", "");
            files = new ArrayList<>();
            org.json.JSONArray array = json.optJSONArray("files");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.optJSONObject(i);
                    if (item != null) files.add(new SkillFile(item));
                }
            }
        }
    }

    static final class SkillFile {
        final String id;
        final String skillId;
        final String path;
        final String content;
        final String createdAt;
        final String updatedAt;

        SkillFile(JSONObject json) {
            id = cleanString(json, "id", "");
            skillId = cleanString(json, "skill_id", "");
            path = cleanString(json, "path", "");
            content = cleanString(json, "content", "");
            createdAt = cleanString(json, "created_at", "");
            updatedAt = cleanString(json, "updated_at", "");
        }
    }

    static final class Autopilot {
        final String id;
        final String title;
        final String description;
        final String status;
        final String assigneeId;
        final String executionMode;
        final String issueTitleTemplate;
        final String lastRunAt;
        final String createdAt;

        Autopilot(JSONObject json) {
            id = json.optString("id");
            title = json.optString("title", json.optString("name", shortId(json.optString("id"))));
            description = json.optString("description", "");
            status = json.optString("status", "");
            assigneeId = json.optString("assignee_id", "");
            executionMode = json.optString("execution_mode", "create_issue");
            issueTitleTemplate = cleanString(json, "issue_title_template", "");
            lastRunAt = cleanString(json, "last_run_at", "");
            createdAt = cleanString(json, "created_at", "");
        }
    }

    static final class AutopilotRun {
        final String id;
        final String autopilotId;
        final String source;
        final String status;
        final String issueId;
        final String taskId;
        final String triggeredAt;
        final String failureReason;

        AutopilotRun(JSONObject json) {
            id = json.optString("id");
            autopilotId = json.optString("autopilot_id");
            source = json.optString("source", "");
            status = json.optString("status", "");
            issueId = cleanString(json, "issue_id", "");
            taskId = cleanString(json, "task_id", "");
            triggeredAt = cleanString(json, "triggered_at", cleanString(json, "created_at", ""));
            failureReason = cleanString(json, "failure_reason", "");
        }
    }

    static final class AutopilotTrigger {
        final String id;
        final String autopilotId;
        final String kind;
        final boolean enabled;
        final String cronExpression;
        final String timezone;
        final String label;
        final String nextRunAt;

        AutopilotTrigger(JSONObject json) {
            id = json.optString("id");
            autopilotId = json.optString("autopilot_id");
            kind = json.optString("kind", "");
            enabled = json.optBoolean("enabled", true);
            cronExpression = cleanString(json, "cron_expression", "");
            timezone = cleanString(json, "timezone", "");
            label = cleanString(json, "label", "");
            nextRunAt = cleanString(json, "next_run_at", "");
        }
    }

    static final class IssueLabel {
        final String id;
        final String name;
        final String color;

        IssueLabel(JSONObject json) {
            id = json.optString("id");
            name = json.optString("name", shortId(id));
            color = json.optString("color", "#94A3B8");
        }
    }

    static final class ChatSession {
        final String id;
        final String title;
        final String agentId;
        final String status;
        final boolean hasUnread;
        final String updatedAt;

        ChatSession(JSONObject json) {
            id = json.optString("id");
            title = json.optString("title", shortId(id));
            agentId = json.optString("agent_id");
            status = json.optString("status", "active");
            hasUnread = json.optBoolean("has_unread", false);
            updatedAt = json.optString("updated_at", json.optString("created_at"));
        }
    }

    static final class ChatMessage {
        final String id;
        final String role;
        final String content;
        final String taskId;
        final String createdAt;
        final String failureReason;

        ChatMessage(JSONObject json) {
            id = json.optString("id");
            role = json.optString("role", "assistant");
            content = json.optString("content", "");
            taskId = json.optString("task_id", "");
            createdAt = json.optString("created_at");
            failureReason = cleanString(json, "failure_reason", "");
        }
    }

    static final class ChatPendingTask {
        final String taskId;
        final String status;
        final String createdAt;

        ChatPendingTask(JSONObject json) {
            taskId = cleanString(json, "task_id", "");
            status = cleanString(json, "status", "");
            createdAt = cleanString(json, "created_at", "");
        }
    }

    static final class PendingChatTaskItem {
        final String taskId;
        final String status;
        final String chatSessionId;

        PendingChatTaskItem(JSONObject json) {
            taskId = cleanString(json, "task_id", "");
            status = cleanString(json, "status", "");
            chatSessionId = cleanString(json, "chat_session_id", "");
        }
    }

    static class PersonalAccessToken {
        final String id;
        final String name;
        final String tokenPrefix;
        final String expiresAt;
        final String lastUsedAt;
        final String createdAt;

        PersonalAccessToken(JSONObject json) {
            id = json.optString("id");
            name = json.optString("name", shortId(id));
            tokenPrefix = json.optString("token_prefix", "");
            expiresAt = cleanString(json, "expires_at", "");
            lastUsedAt = cleanString(json, "last_used_at", "");
            createdAt = cleanString(json, "created_at", "");
        }
    }

    static final class CreatedPersonalAccessToken extends PersonalAccessToken {
        final String token;

        CreatedPersonalAccessToken(JSONObject json) {
            super(json);
            token = json.optString("token", "");
        }
    }

    static final class Assignee {
        final String id;
        final String type;
        final String name;

        Assignee(String id, String type, String name) {
            this.id = id;
            this.type = type;
            this.name = name;
        }
    }

    static <T> List<T> emptyList() {
        return new ArrayList<>();
    }

    private static Map<String, String> stringMap(JSONObject object) {
        Map<String, String> out = new LinkedHashMap<>();
        if (object == null) return out;
        JSONArray names = object.names();
        if (names == null) return out;
        for (int i = 0; i < names.length(); i++) {
            String key = names.optString(i, "");
            if (key.isEmpty()) continue;
            out.put(key, object.optString(key, ""));
        }
        return out;
    }

    private static List<String> stringList(JSONArray array) {
        List<String> out = new ArrayList<>();
        if (array == null) return out;
        for (int i = 0; i < array.length(); i++) {
            String value = array.optString(i, "");
            if (!value.isEmpty()) out.add(value);
        }
        return out;
    }
}
