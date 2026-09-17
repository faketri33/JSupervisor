cmake_minimum_required(VERSION 3.20)
project(notify_native)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

add_library(notify_native SHARED linux_notify.mm linux_notify.h)

target_link_libraries(notify_native ${JNI_LIBRARIES})

if(APPLE)
    target_link_libraries(notify_native "-framework Cocoa" "-framework UserNotifications")
endif()