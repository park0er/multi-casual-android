package ai.multica.app;

final class ChatExperiencePolicy {
    private static final float CHAT_FOCUSED_COMPOSER_RATIO = 0.22f;
    private static final float ISSUE_FOCUSED_COMPOSER_RATIO = 0.18f;
    private static final int MIN_CHAT_FOCUSED_COMPOSER_HEIGHT = 132;
    private static final int MAX_CHAT_FOCUSED_COMPOSER_HEIGHT = 184;
    private static final int MIN_ISSUE_FOCUSED_COMPOSER_HEIGHT = 112;
    private static final int MAX_ISSUE_FOCUSED_COMPOSER_HEIGHT = 152;

    enum Route {
        CHAT,
        ISSUE_COMMENT,
        ISSUE_REPLY
    }

    private ChatExperiencePolicy() {
    }

    static String newChatInitialTitle() {
        return "";
    }

    static String firstChatTitle(String content) {
        String clean = content == null ? "" : content.trim();
        if (clean.isEmpty()) return "";
        StringBuilder title = new StringBuilder();
        clean.codePoints().limit(15).forEach(title::appendCodePoint);
        return title.toString();
    }

    static boolean requiresTitleBeforeEnteringChat() {
        return false;
    }

    static int focusedComposerHeightPx(Route route, int usableHeightPx) {
        int height = Math.max(0, usableHeightPx);
        if (route == Route.ISSUE_COMMENT || route == Route.ISSUE_REPLY) {
            int target = Math.round(height * ISSUE_FOCUSED_COMPOSER_RATIO);
            return clamp(target, MIN_ISSUE_FOCUSED_COMPOSER_HEIGHT, MAX_ISSUE_FOCUSED_COMPOSER_HEIGHT);
        }
        int target = Math.round(height * CHAT_FOCUSED_COMPOSER_RATIO);
        return clamp(target, MIN_CHAT_FOCUSED_COMPOSER_HEIGHT, MAX_CHAT_FOCUSED_COMPOSER_HEIGHT);
    }

    static int focusedComposerHeightPx(int usableHeightPx) {
        return focusedComposerHeightPx(Route.CHAT, usableHeightPx);
    }

    static boolean isFocusedComposerRoute(Route route) {
        return route == Route.CHAT || route == Route.ISSUE_COMMENT || route == Route.ISSUE_REPLY;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
