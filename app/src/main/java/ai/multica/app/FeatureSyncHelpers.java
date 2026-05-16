package ai.multica.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class AgentMentionMarkdown {
    static String markdown(String name, String agentId) {
        String cleanName = name == null ? "" : name.trim()
                .replace("[", "\\[")
                .replace("]", "\\]");
        if (cleanName.isEmpty()) cleanName = Models.shortId(agentId);
        return "[@" + cleanName + "](mention://agent/" + agentId + ")";
    }

    private AgentMentionMarkdown() {
    }
}

final class InboxNotificationDeduper {
    static List<Models.InboxItem> deduplicateByIssue(Collection<Models.InboxItem> items) {
        Map<String, Models.InboxItem> newestByIssue = new LinkedHashMap<>();
        for (Models.InboxItem item : items) {
            if (item == null || item.archived) continue;
            String key = item.issueId == null || item.issueId.isEmpty() ? item.id : item.issueId;
            Models.InboxItem existing = newestByIssue.get(key);
            if (existing == null || compareCreatedAt(item, existing) > 0) {
                newestByIssue.put(key, item);
            }
        }
        ArrayList<Models.InboxItem> deduped = new ArrayList<>(newestByIssue.values());
        deduped.sort((left, right) -> compareCreatedAt(right, left));
        return deduped;
    }

    private static int compareCreatedAt(Models.InboxItem left, Models.InboxItem right) {
        String leftCreatedAt = left.createdAt == null ? "" : left.createdAt;
        String rightCreatedAt = right.createdAt == null ? "" : right.createdAt;
        return leftCreatedAt.compareTo(rightCreatedAt);
    }

    private InboxNotificationDeduper() {
    }
}

final class SkillFileTree {
    static final class Node {
        final String name;
        final String path;
        final boolean directory;
        final Models.SkillFile file;
        final List<Node> children = new ArrayList<>();

        Node(String name, String path, boolean directory, Models.SkillFile file) {
            this.name = name;
            this.path = path;
            this.directory = directory;
            this.file = file;
        }
    }

    static List<Node> build(List<Models.SkillFile> files) {
        Node root = new Node("", "", true, null);
        for (Models.SkillFile file : files) {
            String normalized = normalizePath(file.path);
            if (normalized.isEmpty()) continue;
            String[] parts = normalized.split("/");
            Node parent = root;
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].isEmpty()) continue;
                if (path.length() > 0) path.append('/');
                path.append(parts[i]);
                boolean leaf = i == parts.length - 1;
                Node existing = findChild(parent, parts[i], leaf ? path.toString() : path + "/");
                if (existing == null) {
                    existing = new Node(parts[i], leaf ? path.toString() : path + "/", !leaf, leaf ? file : null);
                    parent.children.add(existing);
                }
                parent = existing;
            }
        }
        sortRecursively(root.children, true);
        return root.children;
    }

    static List<String> flattenDisplayPaths(List<Node> roots) {
        ArrayList<String> paths = new ArrayList<>();
        flattenInto(roots, paths);
        return paths;
    }

    static boolean isMarkdownPath(String path) {
        String lower = normalizePath(path).toLowerCase(Locale.US);
        return lower.endsWith(".md") || lower.endsWith(".markdown");
    }

    private static void flattenInto(List<Node> nodes, List<String> paths) {
        for (Node node : nodes) {
            paths.add(node.path);
            if (node.directory) flattenInto(node.children, paths);
        }
    }

    private static Node findChild(Node parent, String name, String path) {
        for (Node child : parent.children) {
            if (child.name.equals(name) && child.path.equals(path)) return child;
        }
        return null;
    }

    private static void sortRecursively(List<Node> nodes, boolean root) {
        for (Node node : nodes) {
            if (node.directory) sortRecursively(node.children, false);
        }
        nodes.sort(Comparator
                .comparingInt((Node node) -> sortBucket(node, root))
                .thenComparing(node -> node.name.toLowerCase(Locale.US)));
    }

    private static int sortBucket(Node node, boolean root) {
        if (root && !node.directory && node.name.equalsIgnoreCase("SKILL.md")) return 0;
        if (node.directory) return 1;
        return root ? 2 : 3;
    }

    private static String normalizePath(String path) {
        if (path == null) return "";
        return path.trim().replace('\\', '/').replaceAll("/+", "/").replaceAll("^/|/$", "");
    }

    private SkillFileTree() {
    }
}

final class ChatTimelineState {
    final String taskId;
    final String status;
    final boolean locallyPending;

    private ChatTimelineState(String taskId, String status, boolean locallyPending) {
        this.taskId = taskId == null ? "" : taskId;
        this.status = status == null ? "" : status;
        this.locallyPending = locallyPending;
    }

    static ChatTimelineState afterLocalSend(String taskId, String status) {
        return new ChatTimelineState(taskId, status == null || status.isEmpty() ? "queued" : status, true);
    }

    boolean shouldShowPendingRow() {
        return locallyPending || !taskId.isEmpty();
    }
}
