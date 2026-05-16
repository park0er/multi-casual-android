package ai.multica.app;

import org.json.JSONArray;
import org.json.JSONObject;

final class OnboardingPayload {
    private OnboardingPayload() {}

    static JSONObject questionnaire(String teamSize, String role, String useCase, String notes) throws Exception {
        JSONObject json = new JSONObject();
        putTrimmed(json, "team_size", teamSize);
        putTrimmed(json, "role", role);
        putTrimmed(json, "use_case", useCase);
        putTrimmed(json, "notes", notes);
        return json;
    }

    static JSONObject starterContentPayload(String workspaceId, boolean assignToSelf) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("workspace_id", workspaceId);
        payload.put("project", new JSONObject()
                .put("title", "Getting Started with Multica")
                .put("description", "A lightweight starter project for learning Multica on mobile.")
                .put("icon", "sparkles"));
        payload.put("welcome_issue_template", new JSONObject()
                .put("title", "Welcome to Multica")
                .put("description", "Use this issue to test comments, Markdown, status changes, and agent collaboration.")
                .put("priority", "high"));
        payload.put("agent_guided_sub_issues", new JSONArray()
                .put(starterIssue(
                        "Ask an agent to summarize your workspace",
                        "Open the agent picker, assign this issue to an agent, and watch the activity thread update.",
                        "todo",
                        "high",
                        assignToSelf))
                .put(starterIssue(
                        "Review Markdown rendering",
                        "Add a comment with a table, list, quote, and code block. Confirm the mobile detail page remains readable.",
                        "todo",
                        "medium",
                        assignToSelf))
                .put(starterIssue(
                        "Try Inbox triage",
                        "Generate a notification, mark it read, then archive it from Inbox.",
                        "todo",
                        "medium",
                        assignToSelf)));
        payload.put("self_serve_sub_issues", new JSONArray()
                .put(starterIssue(
                        "Create your first issue",
                        "Use New Issue to set project, status, priority, and assignee.",
                        "todo",
                        "high",
                        assignToSelf))
                .put(starterIssue(
                        "Organize issues by status",
                        "Move an issue across the status groups and verify the list stays sorted.",
                        "todo",
                        "medium",
                        assignToSelf))
                .put(starterIssue(
                        "Invite a teammate or add an agent later",
                        "When you are ready, open Settings to manage members, agents, and runtimes.",
                        "todo",
                        "low",
                        assignToSelf)));
        return payload;
    }

    private static JSONObject starterIssue(
            String title,
            String description,
            String status,
            String priority,
            boolean assignToSelf) throws Exception {
        return new JSONObject()
                .put("title", title)
                .put("description", description)
                .put("status", status)
                .put("priority", priority)
                .put("assign_to_self", assignToSelf);
    }

    private static void putTrimmed(JSONObject json, String key, String value) throws Exception {
        if (value == null) return;
        String trimmed = value.trim();
        if (!trimmed.isEmpty()) json.put(key, trimmed);
    }
}
