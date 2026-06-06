package ai.multica.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AgentMentionMarkdown {
    static String markdown(String name, String agentId) {
        return mentionMarkdown("agent", name, agentId);
    }

    static String memberMarkdown(String name, String memberId) {
        return mentionMarkdown("member", name, memberId);
    }

    static String squadMarkdown(String name, String squadId) {
        return mentionMarkdown("squad", name, squadId);
    }

    static String displayText(String name, String id) {
        String cleanName = cleanName(name);
        if (cleanName.isEmpty()) cleanName = Models.shortId(id);
        return "@" + cleanName;
    }

    static String markdownFromDraft(String draft, List<Models.Agent> agents) {
        return markdownFromDraft(draft, agents, Collections.emptyList(), Collections.emptyList());
    }

    static String markdownFromDraft(String draft, List<Models.Agent> agents,
                                    List<Models.Member> members, List<Models.Squad> squads) {
        String content = draft == null ? "" : draft.trim();
        if (content.isEmpty() || content.contains("mention://")) return content;
        ArrayList<MentionReplacement> replacements = new ArrayList<>();
        if (agents != null) {
            for (Models.Agent agent : agents) {
                replacements.add(new MentionReplacement(displayText(agent.name, agent.id), markdown(agent.name, agent.id)));
            }
        }
        if (members != null) {
            for (Models.Member member : members) {
                replacements.add(new MentionReplacement(displayText(member.displayName, member.id), memberMarkdown(member.displayName, member.id)));
                if (member.userId != null && !member.userId.isEmpty() && !member.userId.equals(member.id)) {
                    replacements.add(new MentionReplacement(displayText(member.displayName, member.userId), memberMarkdown(member.displayName, member.userId)));
                }
            }
        }
        if (squads != null) {
            for (Models.Squad squad : squads) {
                replacements.add(new MentionReplacement(displayText(squad.name, squad.id), squadMarkdown(squad.name, squad.id)));
            }
        }
        replacements.sort((left, right) -> Integer.compare(right.display.length(), left.display.length()));
        String normalized = content;
        for (MentionReplacement replacement : replacements) {
            if (replacement.display.length() <= 1) continue;
            normalized = normalized.replace(replacement.display, replacement.markdown);
        }
        return normalized;
    }

    private static String mentionMarkdown(String type, String name, String id) {
        String cleanName = cleanName(name)
                .replace("[", "\\[")
                .replace("]", "\\]");
        if (cleanName.isEmpty()) cleanName = Models.shortId(id);
        return "[@" + cleanName + "](mention://" + type + "/" + id + ")";
    }

    private static String cleanName(String name) {
        if (name == null) return "";
        String trimmed = name.trim();
        return "null".equalsIgnoreCase(trimmed) ? "" : trimmed;
    }

    private static final class MentionReplacement {
        final String display;
        final String markdown;

        MentionReplacement(String display, String markdown) {
            this.display = display;
            this.markdown = markdown;
        }
    }

    private AgentMentionMarkdown() {
    }
}

final class IssueCommentRichText {
    private static final Pattern ISSUE_IDENTIFIER = Pattern.compile("[A-Z][A-Z0-9]+-\\d+");

    static String mentionDisplayLabel(String mentionType, String mentionId, String fallbackLabel,
                                      List<Models.Member> members, List<Models.Agent> agents,
                                      List<Models.Squad> squads, Models.User currentUser) {
        String resolved = resolveMentionName(mentionType, mentionId, members, agents, squads, currentUser);
        String label = resolved.isEmpty() ? cleanMentionFallback(fallbackLabel, mentionId) : resolved;
        if (label.startsWith("@")) return label;
        return "@" + label;
    }

    static String mentionDisplayLabel(String mentionType, String mentionId, String fallbackLabel,
                                      List<Models.Member> members, List<Models.Agent> agents,
                                      Models.User currentUser) {
        return mentionDisplayLabel(mentionType, mentionId, fallbackLabel, members, agents, Collections.emptyList(), currentUser);
    }

    static int issueIdentifierEndAt(String value, int start) {
        if (value == null || start < 0 || start >= value.length()) return -1;
        Matcher matcher = ISSUE_IDENTIFIER.matcher(value);
        matcher.region(start, value.length());
        if (!matcher.lookingAt()) return -1;
        int end = matcher.end();
        if (!hasIdentifierBoundary(value, start, end)) return -1;
        return end;
    }

    static List<String> issueIdentifiersOutsideCodeAndLinks(String value) {
        ArrayList<String> identifiers = new ArrayList<>();
        if (value == null || value.isEmpty()) return identifiers;
        int i = 0;
        while (i < value.length()) {
            if (value.charAt(i) == '`') {
                int end = value.indexOf('`', i + 1);
                i = end > i ? end + 1 : i + 1;
                continue;
            }
            int markdownLinkEnd = markdownLinkEndAt(value, i);
            if (markdownLinkEnd > i) {
                i = markdownLinkEnd;
                continue;
            }
            int end = issueIdentifierEndAt(value, i);
            if (end > i) {
                identifiers.add(value.substring(i, end));
                i = end;
            } else {
                i++;
            }
        }
        return identifiers;
    }

    private static String resolveMentionName(String mentionType, String mentionId,
                                             List<Models.Member> members, List<Models.Agent> agents,
                                             List<Models.Squad> squads, Models.User currentUser) {
        String type = mentionType == null ? "" : mentionType.toLowerCase(Locale.US);
        String id = mentionId == null ? "" : mentionId;
        if ("agent".equals(type)) {
            for (Models.Agent agent : agents) {
                if (id.equals(agent.id)) return clean(agent.name);
            }
        }
        if ("member".equals(type) || "user".equals(type)) {
            if (currentUser != null && id.equals(currentUser.id)) return clean(currentUser.name);
            for (Models.Member member : members) {
                if (id.equals(member.id) || id.equals(member.userId)) return clean(member.displayName);
            }
        }
        if ("squad".equals(type)) {
            for (Models.Squad squad : squads) {
                if (id.equals(squad.id)) return clean(squad.name);
            }
        }
        for (Models.Agent agent : agents) {
            if (id.equals(agent.id)) return clean(agent.name);
        }
        if (currentUser != null && id.equals(currentUser.id)) return clean(currentUser.name);
        for (Models.Member member : members) {
            if (id.equals(member.id) || id.equals(member.userId)) return clean(member.displayName);
        }
        for (Models.Squad squad : squads) {
            if (id.equals(squad.id)) return clean(squad.name);
        }
        return "";
    }

    private static String cleanMentionFallback(String fallbackLabel, String mentionId) {
        String fallback = clean(fallbackLabel);
        while (fallback.startsWith("@")) fallback = fallback.substring(1).trim();
        return fallback.isEmpty() ? Models.shortId(mentionId) : fallback;
    }

    private static boolean hasIdentifierBoundary(String value, int start, int end) {
        boolean left = start == 0 || !isIssueIdentifierNeighbor(value.charAt(start - 1));
        boolean right = end >= value.length() || !isIssueIdentifierNeighbor(value.charAt(end));
        return left && right;
    }

    private static boolean isIssueIdentifierNeighbor(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '-';
    }

    private static int markdownLinkEndAt(String value, int start) {
        if (start < 0 || start >= value.length() || value.charAt(start) != '[') return -1;
        int labelEnd = value.indexOf("](", start + 1);
        if (labelEnd <= start + 1) return -1;
        int urlEnd = value.indexOf(')', labelEnd + 2);
        return urlEnd > labelEnd ? urlEnd + 1 : -1;
    }

    private static String clean(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        return "null".equalsIgnoreCase(trimmed) ? "" : trimmed;
    }

    private IssueCommentRichText() {
    }
}

final class IssueCommentThreads {
    static final class Thread {
        final Models.Comment root;
        final List<Models.Comment> replies;

        Thread(Models.Comment root, List<Models.Comment> replies) {
            this.root = root;
            this.replies = Collections.unmodifiableList(replies);
        }
    }

    static List<Thread> build(List<Models.Comment> comments, boolean descending) {
        LinkedHashMap<String, Models.Comment> byId = new LinkedHashMap<>();
        for (Models.Comment comment : comments) {
            if (comment != null && !clean(comment.id).isEmpty()) byId.put(comment.id, comment);
        }

        LinkedHashMap<String, ArrayList<Models.Comment>> repliesByRoot = new LinkedHashMap<>();
        ArrayList<Models.Comment> roots = new ArrayList<>();
        for (Models.Comment comment : comments) {
            if (comment == null) continue;
            Models.Comment root = rootFor(comment, byId);
            if (root == comment || !byId.containsKey(clean(root.id))) {
                if (!roots.contains(comment)) roots.add(comment);
                repliesByRoot.putIfAbsent(clean(comment.id), new ArrayList<>());
            } else {
                repliesByRoot.putIfAbsent(clean(root.id), new ArrayList<>());
                repliesByRoot.get(clean(root.id)).add(comment);
            }
        }

        roots.sort((left, right) -> descending
                ? compareCreatedAt(right, left)
                : compareCreatedAt(left, right));
        ArrayList<Thread> threads = new ArrayList<>();
        for (Models.Comment root : roots) {
            ArrayList<Models.Comment> replies = repliesByRoot.getOrDefault(clean(root.id), new ArrayList<>());
            replies.sort(IssueCommentThreads::compareCreatedAt);
            threads.add(new Thread(root, replies));
        }
        return threads;
    }

    static List<String> replyIds(Thread thread) {
        ArrayList<String> ids = new ArrayList<>();
        if (thread == null) return ids;
        for (Models.Comment reply : thread.replies) ids.add(reply.id);
        return ids;
    }

    static String replyContentForThread(Thread thread, String draft, List<Models.Agent> agents) {
        String content = clean(draft);
        if (content.contains("mention://agent/")) return content;
        String agentId = firstAgentParticipantId(thread);
        if (agentId.isEmpty()) return content;
        String mention = AgentMentionMarkdown.markdown(agentName(agentId, agents), agentId);
        return content.isEmpty() ? mention : mention + "\n\n" + content;
    }

    private static Models.Comment rootFor(Models.Comment comment, Map<String, Models.Comment> byId) {
        Models.Comment current = comment;
        ArrayList<String> seen = new ArrayList<>();
        while (current != null && !isRoot(current)) {
            String parentId = clean(current.parentId);
            if (seen.contains(parentId)) return comment;
            seen.add(parentId);
            Models.Comment parent = byId.get(parentId);
            if (parent == null) return comment;
            current = parent;
        }
        return current == null ? comment : current;
    }

    private static String firstAgentParticipantId(Thread thread) {
        if (thread == null) return "";
        String rootAgentId = agentParticipantId(thread.root);
        if (!rootAgentId.isEmpty()) return rootAgentId;
        for (Models.Comment reply : thread.replies) {
            String agentId = agentParticipantId(reply);
            if (!agentId.isEmpty()) return agentId;
        }
        return "";
    }

    private static String agentParticipantId(Models.Comment comment) {
        if (comment == null || !"agent".equalsIgnoreCase(clean(comment.authorType))) return "";
        return clean(comment.authorId);
    }

    private static String agentName(String agentId, List<Models.Agent> agents) {
        if (agents != null) {
            for (Models.Agent agent : agents) {
                if (agentId.equals(agent.id)) return clean(agent.name);
            }
        }
        return Models.shortId(agentId);
    }

    private static boolean isRoot(Models.Comment comment) {
        String parentId = clean(comment.parentId);
        return parentId.isEmpty() || "null".equalsIgnoreCase(parentId);
    }

    private static int compareCreatedAt(Models.Comment left, Models.Comment right) {
        String leftCreatedAt = left == null ? "" : clean(left.createdAt);
        String rightCreatedAt = right == null ? "" : clean(right.createdAt);
        int compared = leftCreatedAt.compareTo(rightCreatedAt);
        if (compared != 0) return compared;
        return clean(left == null ? "" : left.id).compareTo(clean(right == null ? "" : right.id));
    }

    private static String clean(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        return "null".equalsIgnoreCase(trimmed) ? "" : trimmed;
    }

    private IssueCommentThreads() {
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
