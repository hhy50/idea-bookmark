package io.github.hhy.bookmark.util;

import java.io.File;
import java.nio.file.Path;

public class FDUtil {

    public static final String PROJECT_RELATIVE = "$";

    public static String toRelative(String fd, String basePath) {
        basePath = formatSeparator(basePath);
        fd = formatSeparator(fd);
        if (!fd.startsWith(basePath)) {
            return fd;
        }
        fd = fd.substring(basePath.length());
        return formatSeparator(Path.of("$", fd).toString());
    }

    public static String toAbsolute(String fd, String basePath) {
        File file = new File(fd);
        if (file.exists() && file.isFile()) {
            return fd;
        }
        file = new File(fd.replace(PROJECT_RELATIVE, basePath));
        if (file.exists() && file.isFile()) {
            return file.getPath();
        }
        return fd;
    }

    public static String formatSeparator(String fd) {
        return fd.replaceAll("\\\\+", "/");
    }
}
