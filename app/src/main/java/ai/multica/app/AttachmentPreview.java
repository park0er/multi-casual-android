package ai.multica.app;

import java.net.URI;

final class AttachmentPreview {
    static final String BASE_URL = "https://attachment-preview.local/";

    enum Kind {
        MARKDOWN,
        HTML,
        XML,
        DOWNLOAD
    }

    private AttachmentPreview() {}

    static Kind kindFor(String contentType, String filename) {
        String type = mediaType(contentType);
        if (isMarkdownType(type)) return Kind.MARKDOWN;
        if (isHtmlType(type)) return Kind.HTML;
        if (isXmlType(type)) return Kind.XML;

        String name = filename == null ? "" : filename.trim().toLowerCase(java.util.Locale.ROOT);
        if (name.endsWith(".md") || name.endsWith(".markdown")) return Kind.MARKDOWN;
        if (name.endsWith(".html") || name.endsWith(".htm")) return Kind.HTML;
        if (name.endsWith(".xml")) return Kind.XML;
        return Kind.DOWNLOAD;
    }

    static boolean isLocalPreviewUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            return "https".equalsIgnoreCase(scheme) && "attachment-preview.local".equalsIgnoreCase(host);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    static boolean isWebViewInternalDocumentUrl(String url) {
        if (url == null) return false;
        String value = url.trim().toLowerCase(java.util.Locale.ROOT);
        return value.equals("about:blank") || value.startsWith("data:text/html");
    }

    static String xmlPreviewDocument(String xml) {
        return "<!doctype html><html><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<style>body{margin:0;padding:16px;background:#fff;color:#111;font:14px/1.55 ui-monospace,SFMono-Regular,Menlo,monospace;}pre{white-space:pre-wrap;word-break:break-word;margin:0;}</style>"
                + "</head><body><pre>"
                + escapeHtml(xml == null ? "" : xml)
                + "</pre></body></html>";
    }

    private static String mediaType(String contentType) {
        if (contentType == null) return "";
        int semicolon = contentType.indexOf(';');
        String value = semicolon >= 0 ? contentType.substring(0, semicolon) : contentType;
        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static boolean isMarkdownType(String type) {
        return "text/markdown".equals(type)
                || "text/x-markdown".equals(type)
                || "application/markdown".equals(type)
                || "application/x-markdown".equals(type);
    }

    private static boolean isHtmlType(String type) {
        return "text/html".equals(type) || "application/xhtml+xml".equals(type);
    }

    private static boolean isXmlType(String type) {
        return "text/xml".equals(type) || "application/xml".equals(type) || type.endsWith("+xml");
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
