package ai.multica.app;

import java.util.List;

final class WorkspaceSelection {
    private WorkspaceSelection() {
    }

    static Models.Workspace chooseWorkspace(List<Models.Workspace> workspaces, String preferredWorkspaceId) {
        if (workspaces == null || workspaces.isEmpty()) return null;
        if (preferredWorkspaceId != null && !preferredWorkspaceId.isEmpty()) {
            for (Models.Workspace workspace : workspaces) {
                if (preferredWorkspaceId.equals(workspace.id)) return workspace;
            }
        }
        return workspaces.get(0);
    }
}
