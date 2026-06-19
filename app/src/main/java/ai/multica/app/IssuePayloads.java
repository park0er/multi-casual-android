package ai.multica.app;

import org.json.JSONObject;

final class IssuePayloads {
    private IssuePayloads() {
    }

    static JSONObject createIssue(String title, String description, String workspaceId, String projectId,
                                  String teamId, String status, String priority, Models.Assignee assignee,
                                  String dueDate, String parentIssueId) throws Exception {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("description", description == null || description.isEmpty() ? JSONObject.NULL : description);
        body.put("workspace_id", workspaceId);
        if (projectId != null) body.put("project_id", projectId);
        if (teamId != null && !teamId.trim().isEmpty()) body.put("team_id", teamId.trim());
        if (parentIssueId != null && !parentIssueId.isEmpty()) body.put("parent_issue_id", parentIssueId);
        if (status != null) body.put("status", status);
        if (priority != null) body.put("priority", priority);
        if (dueDate != null && !dueDate.isEmpty()) body.put("due_date", dueDate); else body.put("due_date", JSONObject.NULL);
        if (assignee != null && assignee.id != null) {
            body.put("assignee_id", assignee.id);
            body.put("assignee_type", assignee.type);
        }
        return body;
    }
}
