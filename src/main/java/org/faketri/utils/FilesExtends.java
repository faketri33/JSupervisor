package org.faketri.utils;

public class FilesExtends {
    private FilesExtends() {
        /* This utility class should not be instantiated */
    }


    public static String getFileExtension(String name){
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) return "";

        return name.substring(lastIndexOf + 1);
    }
}
