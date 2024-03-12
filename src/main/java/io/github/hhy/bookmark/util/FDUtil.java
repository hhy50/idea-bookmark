package io.github.hhy.bookmark.util;

import java.io.File;
import java.nio.file.Path;

public class FDUtil {

    public static final String PROJECT_RELATIVE = "$";

    public static String toRelative(String fileDescriptor, String basePath) {
        fileDescriptor = fileDescriptor
                .substring(basePath.length());
        return Path.of("$", fileDescriptor).toString()
                .replaceAll("\\\\+", "/");
    }

    public static String toAbsolute(String fileDescriptor, String basePath) {
        File file = new File(fileDescriptor);
        if (file.exists() && file.isFile()) {
            return fileDescriptor;
        }
        file = new File(fileDescriptor.replace(PROJECT_RELATIVE, basePath));
        if (file.exists() && file.isFile()) {
            return file.getPath();
        }
        return fileDescriptor;
    }
}
