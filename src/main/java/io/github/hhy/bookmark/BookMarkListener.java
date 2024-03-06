package io.github.hhy.bookmark;

import com.intellij.ide.bookmarks.Bookmark;
import com.intellij.ide.bookmarks.BookmarksListener;
import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.notify.Notify;
import io.github.hhy.bookmark.storage.Storage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class BookMarkListener implements BookmarksListener {

    private Project project;

    public BookMarkListener(Project project) {
        this.project = project;
    }

    @Override
    public void bookmarkAdded(@NotNull Bookmark b) {
        storage();
    }

    @Override
    public void bookmarkRemoved(@NotNull Bookmark b) {
        storage();
    }

    @Override
    public void bookmarkChanged(@NotNull Bookmark b) {
        storage();
    }

    public void storage() {
        Storage storage = Storage.getStorage(project);
        try {
            storage.storage();
        } catch (IOException e) {
            Notify.error(e.getMessage());
        }
    }
}