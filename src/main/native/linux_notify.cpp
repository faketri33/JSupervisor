#include <jni.h>
#include "linux_notify.h"
#include <iostream>
#include <dbus/dbus.h>
#include <cstdint>


extern "C" {
    JNIEXPORT void JNICALL Java_org_faketri_utils_NotificationSystem_notify(JNIEnv* env, jclass clazz, jstring title, jstring message) {
        const char* titleStr = env->GetStringUTFChars(title, nullptr);
        const char* bodyStr = env->GetStringUTFChars(message, nullptr);

        DBusConnection* connection = dbus_bus_get(DBUS_BUS_SESSION, nullptr);
        if (!connection) {
            env->ReleaseStringUTFChars(title, titleStr);
            env->ReleaseStringUTFChars(message, bodyStr);
            return;
        }

        DBusMessage* dbusMessage = dbus_message_new_method_call(
                "org.freedesktop.Notifications",
                "/org/freedesktop/Notifications",
                "org.freedesktop.Notifications",
                "Notify"
        );

        if (!dbusMessage) {
            env->ReleaseStringUTFChars(title, titleStr);
            env->ReleaseStringUTFChars(message, bodyStr);
            return;
        }

        DBusMessageIter iter;
        dbus_message_iter_init_append(dbusMessage, &iter);

        const char* appName = "JSupervisor";
        const char* appIcon = "";
        dbus_uint32_t replacesId = 0;;
        dbus_int32_t expireTimeout = -1;

        dbus_message_iter_append_basic(&iter, DBUS_TYPE_STRING, &appName);
        dbus_message_iter_append_basic(&iter, DBUS_TYPE_UINT32, &replacesId);
        dbus_message_iter_append_basic(&iter, DBUS_TYPE_STRING, &appIcon);
        dbus_message_iter_append_basic(&iter, DBUS_TYPE_STRING, &titleStr);
        dbus_message_iter_append_basic(&iter, DBUS_TYPE_STRING, &bodyStr);

        DBusMessageIter actions;
        dbus_message_iter_open_container(
                &iter,
                DBUS_TYPE_ARRAY,
                "s",
                &actions
        );
        dbus_message_iter_close_container(&iter, &actions);

        // hints: {}
        DBusMessageIter hints;
        dbus_message_iter_open_container(
                &iter,
                DBUS_TYPE_ARRAY,
                "{sv}",
                &hints
        );
        dbus_message_iter_close_container(&iter, &hints);

        // timeout
        dbus_message_iter_append_basic(
                &iter,
                DBUS_TYPE_INT32,
                &expireTimeout
        );


        dbus_connection_send(connection, dbusMessage, nullptr);
        dbus_connection_flush(connection);

        dbus_message_unref(dbusMessage);

        env->ReleaseStringUTFChars(title, titleStr);
        env->ReleaseStringUTFChars(message, bodyStr);
    }
}