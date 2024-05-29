package io.github.hhy.bookmark.manager;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkGroup;
import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.BookmarkElement;
import io.github.hhy.bookmark.element.GroupElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LowVersionBookManager implements MyBookmarkManager {

    @NotNull
    @Override
    public Map<String, Map<String, BookmarkElement>> getAllBookmarks(@NotNull Project project) {
        return null;
    }

    @NotNull
    @Override
    public BookmarkGroup addGroup(@NotNull Project project, @NotNull GroupElement ele) {
        return null;
    }

    @NotNull
    @Override
    public Bookmark addBookmark(@NotNull Project project, @NotNull String groupName, @NotNull BookmarkElement ele) {
        return null;
    }

    @NotNull
    @Override
    public List<Bookmark> removeInvalid(@NotNull Project project) {
        return Collections.emptyList();
    }
}