package ai.multica.app;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public final class AppAnalytics {
    private static final String TAG = "MultiCasualAnalytics";
    static final String EVENT_APP_OPEN = "app_open";
    private static volatile boolean initialized;
    private static volatile boolean appOpenTracked;
    private static AnalyticsProvider provider = AnalyticsProvider.NO_OP;

    private AppAnalytics() {}

    public static boolean shouldPromptForConsent(Context context) {
        return !new AnalyticsConsentStore(context).hasDecision();
    }

    public static void grantConsentAndInitialize(Context context) {
        new AnalyticsConsentStore(context).grant();
        initializeIfAllowed(context);
    }

    public static void denyConsent(Context context) {
        new AnalyticsConsentStore(context).deny();
        provider.optOut();
    }

    public static void initializeIfAllowed(Context context) {
        Context appContext = context.getApplicationContext();
        preInitializeIfNeeded(appContext);
        if (!new AnalyticsConsentStore(appContext).isGranted()) {
            Log.i(TAG, "Analytics disabled until user grants consent.");
            return;
        }
        if (initialized) {
            trackAppOpenOnce(appContext);
            return;
        }
        synchronized (AppAnalytics.class) {
            if (initialized) {
                trackAppOpenOnce(appContext);
                return;
            }
            provider = createProvider(appContext);
            initialized = true;
        }
        trackAppOpenOnce(appContext);
    }

    public static void preInitializeIfNeeded(Context context) {
        if (!BuildConfig.UMENG_ENABLED || TextUtils.isEmpty(safe(BuildConfig.UMENG_APP_KEY))) {
            return;
        }
        try {
            Class.forName("com.umeng.commonsdk.UMConfigure")
                    .getMethod("preInit", Context.class, String.class, String.class)
                    .invoke(null, context.getApplicationContext(), BuildConfig.UMENG_APP_KEY, BuildConfig.UMENG_CHANNEL);
            Log.i(TAG, "Umeng preInit completed.");
        } catch (Throwable error) {
            Log.w(TAG, "Umeng preInit failed; app launch continues.", error);
        }
    }

    static String selectedRouteForTesting(Context context) {
        return selectRoute(context);
    }

    private static AnalyticsProvider createProvider(Context context) {
        String route = selectRoute(context);
        Log.i(TAG, "Analytics route=" + route + " environment=" + BuildConfig.MULTICA_ENVIRONMENT);
        if ("cn".equals(route)) {
            return UmengAnalyticsProvider.create(context);
        }
        if ("global".equals(route)) {
            return PostHogAnalyticsProvider.create(context);
        }
        return AnalyticsProvider.NO_OP;
    }

    private static String selectRoute(Context context) {
        String forced = safe(BuildConfig.ANALYTICS_ROUTE).toLowerCase(Locale.US);
        if ("cn".equals(forced) || "global".equals(forced) || "none".equals(forced)) {
            return forced;
        }
        if (isLikelyMainlandChina(context)) {
            return "cn";
        }
        return "global";
    }

    private static boolean isLikelyMainlandChina(Context context) {
        Locale locale = Build.VERSION.SDK_INT >= 24
                ? context.getResources().getConfiguration().getLocales().get(0)
                : context.getResources().getConfiguration().locale;
        String country = locale == null ? "" : safe(locale.getCountry()).toUpperCase(Locale.US);
        if ("CN".equals(country)) {
            return true;
        }
        String language = locale == null ? "" : safe(locale.getLanguage()).toLowerCase(Locale.US);
        String timeZone = TimeZone.getDefault().getID();
        return "zh".equals(language)
                && ("Asia/Shanghai".equals(timeZone)
                || "Asia/Chongqing".equals(timeZone)
                || "Asia/Harbin".equals(timeZone)
                || "Asia/Urumqi".equals(timeZone));
    }

    private static void trackAppOpenOnce(Context context) {
        if (appOpenTracked) {
            return;
        }
        synchronized (AppAnalytics.class) {
            if (appOpenTracked) {
                return;
            }
            appOpenTracked = true;
        }
        provider.trackAppOpen(appOpenProperties(context));
    }

    private static Map<String, Object> appOpenProperties(Context context) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("app_version", BuildConfig.VERSION_NAME);
        params.put("build_flavor", BuildConfig.MULTICA_ENVIRONMENT);
        params.put("distribution_channel", "public");
        params.put("analytics_route", selectRoute(context));
        return params;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    interface AnalyticsProvider {
        AnalyticsProvider NO_OP = new AnalyticsProvider() {
            @Override
            public void trackAppOpen(Map<String, Object> properties) {}

            @Override
            public void optOut() {}
        };

        void trackAppOpen(Map<String, Object> properties);

        void optOut();
    }

    private static final class UmengAnalyticsProvider implements AnalyticsProvider {
        private final Context context;
        private final Method onEventObjectMethod;

        private UmengAnalyticsProvider(Context context, Method onEventObjectMethod) {
            this.context = context.getApplicationContext();
            this.onEventObjectMethod = onEventObjectMethod;
        }

        static AnalyticsProvider create(Context context) {
            if (!BuildConfig.UMENG_ENABLED || TextUtils.isEmpty(safe(BuildConfig.UMENG_APP_KEY))) {
                Log.w(TAG, "Umeng selected but UMENG_APP_KEY is empty; analytics not started.");
                return NO_OP;
            }
            Context appContext = context.getApplicationContext();
            try {
                Class<?> configureClass = Class.forName("com.umeng.commonsdk.UMConfigure");
                Class<?> mobclickClass = Class.forName("com.umeng.analytics.MobclickAgent");
                Class<?> pageModeClass = Class.forName("com.umeng.analytics.MobclickAgent$PageMode");
                Object manualMode = Enum.valueOf((Class<Enum>) pageModeClass.asSubclass(Enum.class), "MANUAL");
                mobclickClass.getMethod("setPageCollectionMode", pageModeClass).invoke(null, manualMode);
                mobclickClass.getMethod("setCatchUncaughtExceptions", boolean.class).invoke(null, false);
                configureClass.getMethod("setLogEnabled", boolean.class).invoke(null, BuildConfig.DEBUG);
                configureClass.getMethod("setProcessEvent", boolean.class).invoke(null, true);
                configureClass.getMethod("init", Context.class, String.class, String.class, int.class, String.class)
                        .invoke(null, appContext, BuildConfig.UMENG_APP_KEY, BuildConfig.UMENG_CHANNEL,
                                configureClass.getField("DEVICE_TYPE_PHONE").getInt(null), null);
                Method onEventObject = mobclickClass.getMethod("onEventObject", Context.class, String.class, Map.class);
                Log.i(TAG, "Umeng analytics initialized.");
                return new UmengAnalyticsProvider(appContext, onEventObject);
            } catch (Throwable error) {
                Log.w(TAG, "Umeng initialization failed; app launch continues.", error);
                return NO_OP;
            }
        }

        @Override
        public void trackAppOpen(Map<String, Object> properties) {
            try {
                onEventObjectMethod.invoke(null, context, EVENT_APP_OPEN, properties);
                Log.i(TAG, "Umeng app_open tracked.");
            } catch (Throwable error) {
                Log.w(TAG, "Umeng app_open failed; app continues.", error);
            }
        }

        @Override
        public void optOut() {}
    }

    private static final class PostHogAnalyticsProvider implements AnalyticsProvider {
        private final Object postHog;
        private final Method captureMethod;
        private final Method optOutMethod;

        private PostHogAnalyticsProvider(Object postHog, Method captureMethod, Method optOutMethod) {
            this.postHog = postHog;
            this.captureMethod = captureMethod;
            this.optOutMethod = optOutMethod;
        }

        static AnalyticsProvider create(Context context) {
            if (!BuildConfig.POSTHOG_ENABLED || TextUtils.isEmpty(safe(BuildConfig.POSTHOG_API_KEY))) {
                Log.w(TAG, "PostHog selected but POSTHOG_API_KEY is empty; analytics not started.");
                return NO_OP;
            }
            try {
                Class<?> configClass = Class.forName("com.posthog.android.PostHogAndroidConfig");
                Object config = configClass.getConstructor(String.class, String.class)
                        .newInstance(BuildConfig.POSTHOG_API_KEY, BuildConfig.POSTHOG_HOST);
                invokeSetter(configClass, config, "setCaptureApplicationLifecycleEvents", false);
                invokeSetter(configClass, config, "setCaptureScreenViews", false);
                invokeSetter(configClass, config, "setCaptureDeepLinks", false);
                invokeSetter(configClass, config, "setSessionReplay", false);
                invokeSetter(configClass, config, "setPreloadFeatureFlags", false);
                invokeSetter(configClass, config, "setRemoteConfig", false);
                invokeSetter(configClass, config, "setSurveys", false);
                invokeSetter(configClass, config, "setSetDefaultPersonProperties", false);
                invokeSetter(configClass, config, "setSendFeatureFlagEvent", false);
                invokeSetter(configClass, config, "setDebug", BuildConfig.DEBUG);

                Class<?> postHogAndroidClass = Class.forName("com.posthog.android.PostHogAndroid");
                Field companionField = postHogAndroidClass.getField("Companion");
                Object companion = companionField.get(null);
                Object shared = companion.getClass()
                        .getMethod("with", Context.class, configClass)
                        .invoke(companion, context.getApplicationContext(), config);
                Class<?> interfaceClass = Class.forName("com.posthog.PostHogInterface");
                Method capture = interfaceClass.getMethod(
                        "capture",
                        String.class,
                        String.class,
                        Map.class,
                        Map.class,
                        Map.class,
                        Map.class,
                        Date.class
                );
                Method optOut = interfaceClass.getMethod("optOut");
                Log.i(TAG, "PostHog analytics initialized.");
                return new PostHogAnalyticsProvider(shared, capture, optOut);
            } catch (Throwable error) {
                Log.w(TAG, "PostHog initialization failed; app launch continues.", error);
                return NO_OP;
            }
        }

        @Override
        public void trackAppOpen(Map<String, Object> properties) {
            try {
                captureMethod.invoke(postHog, EVENT_APP_OPEN, null, properties, null, null, null, null);
                Log.i(TAG, "PostHog app_open tracked.");
            } catch (Throwable error) {
                Log.w(TAG, "PostHog app_open failed; app continues.", error);
            }
        }

        @Override
        public void optOut() {
            try {
                optOutMethod.invoke(postHog);
            } catch (Throwable error) {
                Log.w(TAG, "PostHog optOut failed; app continues.", error);
            }
        }

        private static void invokeSetter(Class<?> configClass, Object config, String methodName, boolean value) {
            try {
                configClass.getMethod(methodName, boolean.class).invoke(config, value);
            } catch (Throwable ignored) {
            }
        }
    }
}
