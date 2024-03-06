package io.github.hhy.bookmark.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.ide.bookmarks.Bookmark;
import com.intellij.ide.bookmarks.BookmarkManager;
import com.intellij.openapi.project.Project;
import groovy.json.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LocalFileStorage implements Storage {

    public static String DEFAULT_FILE = ".idea" + File.separator + "bookmarks.json";

    private Project project;

    private Path file;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public LocalFileStorage(Project project) {
        this.project = project;
        this.file = Path.of(project.getBasePath(), DEFAULT_FILE);
    }

    @Override
    public void init() throws IOException {
        if (!Files.exists(file)) {
            this.storage();
        }
    }

    @Override
    public void storage() throws IOException {
        BookmarkManager bookmarkManager = project.getService(BookmarkManager.class);
        Collection<Bookmark> bookmarks = bookmarkManager.getAllBookmarks();

        if (bookmarks == null || bookmarks.size() == 0) {
            Files.write(file, new byte[0]);
            return;
        }
        Files.write(file, formatJson(bookmarks).getBytes());
    }

    @Override
    public List<Element> read() throws IOException {
        String fileStr = Files.readString(file);
        if (StringUtils.isEmpty(fileStr)) {
            return Collections.emptyList();
        }
        return Arrays.asList(gson.fromJson(fileStr, Element[].class));
    }

    private @NotNull String formatJson(Collection<Bookmark> bookmarks) {
        return gson.toJson(bookmarks.stream().map(Element::new).collect(Collectors.toList()));
    }
}
