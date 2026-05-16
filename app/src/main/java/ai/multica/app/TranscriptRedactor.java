package ai.multica.app;

import java.util.regex.Pattern;

final class TranscriptRedactor {
    private static final Rule[] RULES = new Rule[] {
            new Rule("\\bAKIA[0-9A-Z]{16}\\b", "[REDACTED AWS KEY]"),
            new Rule("(?:aws_secret_access_key|secret_?access_?key)\\s*[=:]\\s*[A-Za-z0-9/+=]{40}", "[REDACTED AWS SECRET]", Pattern.CASE_INSENSITIVE),
            new Rule("-----BEGIN[A-Z\\s]*PRIVATE KEY-----[\\s\\S]*?-----END[A-Z\\s]*PRIVATE KEY-----", "[REDACTED PRIVATE KEY]"),
            new Rule("\\b(?:ghp|gho|ghu|ghs|ghr)_[A-Za-z0-9_]{36,255}\\b", "[REDACTED GITHUB TOKEN]"),
            new Rule("\\bglpat-[A-Za-z0-9_-]{20,}\\b", "[REDACTED GITLAB TOKEN]"),
            new Rule("\\bsk-[A-Za-z0-9_-]{20,}\\b", "[REDACTED API KEY]"),
            new Rule("\\bxox[bporas]-[A-Za-z0-9-]{10,}\\b", "[REDACTED SLACK TOKEN]"),
            new Rule("\\bey[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\b", "[REDACTED JWT]"),
            new Rule("\\bBearer\\s+[A-Za-z0-9\\-._~+/]+=*", "Bearer [REDACTED]", Pattern.CASE_INSENSITIVE),
            new Rule("(?:postgres|mysql|mongodb|redis|amqp)(?:ql)?://[^:\\s]+:[^@\\s]+@", "[REDACTED CONNECTION STRING]@", Pattern.CASE_INSENSITIVE),
            new Rule("(?:API_KEY|API_SECRET|SECRET_KEY|SECRET|ACCESS_TOKEN|AUTH_TOKEN|PRIVATE_KEY|DATABASE_URL|DB_PASSWORD|DB_URL|REDIS_URL|PASSWORD|TOKEN)\\s*[=:]\\s*(?!\\[REDACTED)\\S+", "[REDACTED CREDENTIAL]", Pattern.CASE_INSENSITIVE)
    };

    private TranscriptRedactor() {
    }

    static String redactSecrets(String text) {
        if (text == null || text.isEmpty()) return "";
        String result = text;
        for (Rule rule : RULES) {
            result = rule.pattern.matcher(result).replaceAll(rule.replacement);
        }
        return result;
    }

    private static final class Rule {
        final Pattern pattern;
        final String replacement;

        Rule(String regex, String replacement) {
            this(regex, replacement, 0);
        }

        Rule(String regex, String replacement, int flags) {
            this.pattern = Pattern.compile(regex, flags);
            this.replacement = replacement;
        }
    }
}
