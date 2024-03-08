package io.github.hhy.bookmark;

import com.intellij.ide.bookmark.BookmarksListener;
import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.notify.Notify;
import io.github.hhy.bookmark.storage.Storage;

import java.io.IOException;

public class BookMarkListener implements BookmarksListener {

    private final Project project;

    public BookMarkListener(Project project) {
        this.project = project;
    }

    private void storage() {
        var storage = Storage.getStorage(project);
        try {
            storage.storage();
        } catch (IOException e) {
            Notify.error(e.getMessage());
        }
    }
}
