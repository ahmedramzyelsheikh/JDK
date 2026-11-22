package com.jdk.spring.notification;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Three implementations of one interface, to show how Spring chooses between candidates. */
public final class NotificationSenders {

    private NotificationSenders() {
    }

    public interface NotificationSender {
        String channel();

        void send(String to, String message);

        List<String> sent();
    }

    abstract static class RecordingSender implements NotificationSender {
        private final List<String> sent = new ArrayList<>();

        @Override
        public void send(String to, String message) {
            sent.add(channel() + " -> " + to + ": " + message);
        }

        @Override
        public List<String> sent() {
            return sent;
        }
    }

    @Component("email")
    @Primary                                         // default choice when nobody asks for a specific one
    public static class EmailSender extends RecordingSender {
        @Override public String channel() { return "email"; }
    }

    @Component("sms")
    @Qualifier("sms")
    public static class SmsSender extends RecordingSender {
        @Override public String channel() { return "sms"; }
    }

    @Component("push")
    public static class PushSender extends RecordingSender {
        @Override public String channel() { return "push"; }
    }
}
