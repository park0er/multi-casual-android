package ai.multica.app;

final class ChatExperiencePolicy {
    private static final float FOCUSED_COMPOSER_RATIO = 0.60f;
    private static final int MIN_FOCUSED_COMPOSER_HEIGHT = 280;

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

    static boolean requiresTitleBeforeEnteringChat() {
        return false;
    }

    static int focusedComposerHeightPx(int usableHeightPx) {
        int target = Math.round(Math.max(0, usableHeightPx) * FOCUSED_COMPOSER_RATIO);
        return Math.max(MIN_FOCUSED_COMPOSER_HEIGHT, target);
    }

    static boolean isFocusedComposerRoute(Route route) {
        return route == Route.CHAT || route == Route.ISSUE_COMMENT || route == Route.ISSUE_REPLY;
    }
}
