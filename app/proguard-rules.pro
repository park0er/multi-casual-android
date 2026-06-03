-keep class com.umeng.** { *; }
-keep class org.repackage.** { *; }
-keep class com.uyumao.** { *; }
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class **.R$* {
    public static final int *;
}
-keep class com.posthog.** { *; }
