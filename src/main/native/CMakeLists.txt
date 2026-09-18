cmake_minimum_required(VERSION 3.20)
project(notify_native)

find_package(JNI REQUIRED)
find_package(PkgConfig REQUIRED)

add_library(notify_native SHARED)

target_include_directories(notify_native
    PRIVATE
    ${JNI_INCLUDE_DIRS}
)

if(APPLE)
    target_sources(notify_native
        PRIVATE
        mac_notify.mm
        mac_notify.h
    )

    target_link_libraries(notify_native
        PRIVATE
        ${JNI_LIBRARIES}
        "-framework Cocoa"
        "-framework UserNotifications"
    )
else()
    pkg_check_modules(DBUS REQUIRED dbus-1)

    target_sources(notify_native
        PRIVATE
        linux_notify.cpp
        linux_notify.h
    )

    target_include_directories(notify_native
        PRIVATE
        ${DBUS_INCLUDE_DIRS}
    )

    target_link_directories(notify_native
        PRIVATE
        ${DBUS_LIBRARY_DIRS}
    )

    target_link_libraries(notify_native
        PRIVATE
        ${DBUS_LIBRARIES}
    )
endif()