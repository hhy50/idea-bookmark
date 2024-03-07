package io.github.hhy.bookmark.notify;

import com.intellij.notification.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Notify {
    private static final NotificationGroup NOTIFY_GROUP = NotificationGroupManager.getInstance().getNotificationGroup(NotifyGroupName.BALLOON);

    private static final NotificationGroup LOG_GROUP = NotificationGroupManager.getInstance().getNotificationGroup(NotifyGroupName.NONE);

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public static void info(String msg) {
        msg = String.format("[Bookmark] [%s] %s", now(), msg);
        Notification notify = LOG_GROUP.createNotification(msg, NotificationType.INFORMATION);
        Notifications.Bus.notify(notify);
    }

    public static void success(String msg) {
        Notification notify = NOTIFY_GROUP.createNotification(msg, NotificationType.INFORMATION);
        Notifications.Bus.notify(notify);
    }

    public static void success() {
        success("success");
    }

    public static void error(String msg) {
        msg = msg == null ? "Error" : msg;
        Notification notify = NOTIFY_GROUP.createNotification(msg, NotificationType.ERROR);
        Notifications.Bus.notify(notify);
    }

    public static String now() {
        return SIMPLE_DATE_FORMAT.get().format(new Date());
    }
}
