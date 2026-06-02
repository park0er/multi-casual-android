package ai.multica.app;

import android.content.Context;
import android.content.SharedPreferences;

final class AnalyticsConsentStore {
    private static final String PREFS_SUFFIX = "_analytics";
    private static final String CONSENT_DECIDED = "analytics_consent_decided";
    private static final String CONSENT_GRANTED = "analytics_consent_granted";

    private final SharedPreferences prefs;

    AnalyticsConsentStore(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(BuildConfig.MULTICA_AUTH_PREFS_NAME + PREFS_SUFFIX, Context.MODE_PRIVATE);
    }

    boolean hasDecision() {
        return prefs.getBoolean(CONSENT_DECIDED, false);
    }

    boolean isGranted() {
        return prefs.getBoolean(CONSENT_DECIDED, false)
                && prefs.getBoolean(CONSENT_GRANTED, false);
    }

    void grant() {
        prefs.edit()
                .putBoolean(CONSENT_DECIDED, true)
                .putBoolean(CONSENT_GRANTED, true)
                .apply();
    }

    void deny() {
        prefs.edit()
                .putBoolean(CONSENT_DECIDED, true)
                .putBoolean(CONSENT_GRANTED, false)
                .apply();
    }
}
