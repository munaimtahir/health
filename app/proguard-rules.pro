# Keep readable, line-accurate stack traces so the mapping.txt Play Console
# receives with the release AAB can deobfuscate crashes.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# WorkManager's default WorkerFactory instantiates Worker subclasses reflectively
# by fully-qualified class name (see ReminderWorker in core/notifications); R8 must
# not rename/strip them or reminder scheduling breaks silently.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
