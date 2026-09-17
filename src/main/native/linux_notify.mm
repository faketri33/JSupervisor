#include <jni.h>
#include "linux_notify.h"
#import <Foundation/Foundation.h>
#import <UserNotifications/UserNotifications.h>


extern "C" {
    JNIEXPORT void JNICALL Java_org_faketri_utils_NotificationSystem_notify(JNIEnv* env, jclass clazz, jstring title, jstring message) {
        NSLog(@"=== NotificationSystem_notify CALLED ===");
        const char *titleChars = env->GetStringUTFChars(title, NULL);
        const char *messageChars = env->GetStringUTFChars(message, NULL);
        NSString *nsTitle = [NSString stringWithUTF8String:titleChars];
        NSString *nsMessage = [NSString stringWithUTF8String:messageChars];
        env->ReleaseStringUTFChars(title, titleChars);
        env->ReleaseStringUTFChars(message, messageChars);

        @autoreleasepool {
            NSBundle *bundle = [NSBundle mainBundle];

            UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];

            dispatch_semaphore_t sema = dispatch_semaphore_create(0);

            [center requestAuthorizationWithOptions:(UNAuthorizationOptionAlert | UNAuthorizationOptionSound)
                                   completionHandler:^(BOOL granted, NSError * _Nullable error) {
                if (!granted) {
                    NSLog(@"Notification permission denied: %@", error);
                }
                dispatch_semaphore_signal(sema);
            }];

            dispatch_semaphore_wait(sema, dispatch_time(DISPATCH_TIME_NOW, 5 * NSEC_PER_SEC));

            UNMutableNotificationContent *content = [[UNMutableNotificationContent alloc] init];
            content.title = nsTitle;
            content.body = nsMessage;
            content.sound = [UNNotificationSound defaultSound];

            UNNotificationRequest *request = [UNNotificationRequest
                requestWithIdentifier:[[NSUUID UUID] UUIDString]
                content:content
                trigger:nil];

            NSLog(@"Scheduling notification: title=%@, body=%@", nsTitle, nsMessage);
           [center addNotificationRequest:request
                     withCompletionHandler:^(NSError * _Nullable error) {
                if (error) {
                    NSLog(@"Failed to schedule notification: %@", error);
                } else {
                    NSLog(@"Notification scheduled successfully: %@",
                          request.identifier);
                }
            }];
        }
    }
}