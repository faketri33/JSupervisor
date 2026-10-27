package org.faketri.utils.data;

import java.util.UUID;

public final class DataTypeDefine {

    public static boolean isUUID(String v) {
        try {
            UUID.fromString(v);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean isPid(String v) {
        try {
            var l = Long.parseLong(v);
            return l >= 0;
        } catch (Exception ignored) {
        }
        return false;
    }
}
