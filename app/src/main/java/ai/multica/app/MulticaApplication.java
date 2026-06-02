package ai.multica.app;

import android.app.Application;

public final class MulticaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppAnalytics.initializeIfAllowed(this);
    }
}
