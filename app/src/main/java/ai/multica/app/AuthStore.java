package ai.multica.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AuthStore {
    private static final String PREFS = "multica_auth";
    private static final String TOKEN = "token";
    private static final String WORKSPACE_ID = "workspace_id";
    private static final String LANGUAGE = "language";
    private static final String INBOX_CACHE_PREFIX = "inbox_cache_";
    private static final String CLOUDFRONT_COOKIE_HEADER = "cloudfront_cookie_header";

    private final SharedPreferences prefs;

    AuthStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    String token() {
        return prefs.getString(TOKEN, null);
    }

    void saveToken(String token) {
        prefs.edit().putString(TOKEN, token).apply();
    }

    void clearToken() {
        prefs.edit().remove(TOKEN).remove(WORKSPACE_ID).remove(CLOUDFRONT_COOKIE_HEADER).apply();
    }

    String workspaceId() {
        return prefs.getString(WORKSPACE_ID, null);
    }

    void saveWorkspaceId(String workspaceId) {
        prefs.edit().putString(WORKSPACE_ID, workspaceId).apply();
    }

    boolean isChinese() {
        return "zh".equals(prefs.getString(LANGUAGE, "en"));
    }

    void setChinese(boolean enabled) {
        prefs.edit().putString(LANGUAGE, enabled ? "zh" : "en").apply();
    }

    String inboxCache(String workspaceId) {
        if (workspaceId == null || workspaceId.trim().isEmpty()) return null;
        return prefs.getString(INBOX_CACHE_PREFIX + workspaceId, null);
    }

    void saveInboxCache(String workspaceId, String json) {
        if (workspaceId == null || workspaceId.trim().isEmpty() || json == null || json.trim().isEmpty()) return;
        prefs.edit().putString(INBOX_CACHE_PREFIX + workspaceId, json).apply();
    }

    String cloudFrontCookieHeader() {
        return prefs.getString(CLOUDFRONT_COOKIE_HEADER, "");
    }

    void saveCloudFrontCookies(List<String> setCookieHeaders) {
        if (setCookieHeaders == null || setCookieHeaders.isEmpty()) return;
        Map<String, String> cookies = parseCookieHeader(cloudFrontCookieHeader());
        boolean changed = false;
        for (String header : setCookieHeaders) {
            if (header == null) continue;
            int semicolon = header.indexOf(';');
            String pair = (semicolon >= 0 ? header.substring(0, semicolon) : header).trim();
            int equals = pair.indexOf('=');
            if (equals <= 0) continue;
            String name = pair.substring(0, equals).trim();
            String value = pair.substring(equals + 1).trim();
            if (!name.startsWith("CloudFront-") || value.isEmpty()) continue;
            cookies.put(name, value);
            changed = true;
        }
        if (!changed) return;
        prefs.edit().putString(CLOUDFRONT_COOKIE_HEADER, joinCookieHeader(cookies)).apply();
    }

    private static Map<String, String> parseCookieHeader(String header) {
        Map<String, String> cookies = new LinkedHashMap<>();
        if (header == null || header.trim().isEmpty()) return cookies;
        String[] parts = header.split(";");
        for (String part : parts) {
            String pair = part == null ? "" : part.trim();
            int equals = pair.indexOf('=');
            if (equals <= 0) continue;
            cookies.put(pair.substring(0, equals).trim(), pair.substring(equals + 1).trim());
        }
        return cookies;
    }

    private static String joinCookieHeader(Map<String, String> cookies) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue().isEmpty()) continue;
            if (sb.length() > 0) sb.append("; ");
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return sb.toString();
    }
}
