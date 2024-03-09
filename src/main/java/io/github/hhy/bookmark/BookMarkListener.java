package io.github.hhy.bookmark;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkGroup;
import com.intellij.ide.bookmark.BookmarksListener;
import com.intellij.ide.bookmark.providers.FileBookmarkImpl;
import com.intellij.ide.bookmark.providers.LineBookmarkImpl;
import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.element.Type;
import io.github.hhy.bookmark.manager.MyBookmarkManager;
import io.github.hhy.bookmark.notify.Notify;
import io.github.hhy.bookmark.storage.Storage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookMarkListener implements BookmarksListener {

    private final Project project;

    public BookMarkListener(Project project) {
        this.project = project;
    }

    public void groupsSorted() {

    }

    public void groupAdded(@NotNull BookmarkGroup group) {
        Storage storage = Storage.getStorage(project);
        try {
            storage.addElement(new Element(Type.GROUP, group.getName())
                    .setFileDescriptor(group.getName()));
        } catch (IOException e) {
            Notify.error(e.getMessage());
        }
    }

    public void groupRemoved(@NotNull BookmarkGroup group) {
        Storage storage = Storage.getStorage(project);
        try {
            storage.removeElement(new Element(Type.GROUP, group.getName()));
        } catch (IOException e) {
            Notify.error(e.getMessage());
        }
    }

    public void groupRenamed(@NotNull BookmarkGroup group) {
        try {
            List<Element> affected = new ArrayList<>();
            for (Element element : MyBookmarkManager.getBookmarkManager().getAllBookmarks(project)) {
                if (StringUtils.isNotEmpty(element.getGroup()) && element.getGroup().equals(group.getName())) {
                    affected.add(element);
                }
            }
            if (CollectionUtils.isNotEmpty(affected)) {
                Storage storage = Storage.getStorage(project);
                storage.addElement(new Element(Type.GROUP, group.getName()));
                storage.addElements(affected);
            }
        } catch (IOException e) {
            Notify.error("Bookmark synchronization failed!");
        }
    }

    public void bookmarkAdded(@NotNull BookmarkGroup group, @NotNull Bookmark bookmark) {
        try {
            if (bookmark instanceof LineBookmarkImpl lineBookmark) {
                for (Element element : MyBookmarkManager.getBookmarkManager().getAllBookmarks(project)) {
                    if (element.getFileDescriptor().equals(lineBookmark.getFile().getPath())
                            && element.getLinenumber() == lineBookmark.getLine()) {
                        Storage storage = Storage.getStorage(project);
                        storage.addElement(element);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Notify.error("Bookmark synchronization failed!");
        }
    }

    public void bookmarkRemoved(@NotNull BookmarkGroup group, @NotNull Bookmark bookmark) {
        try {
            Storage storage = Storage.getStorage(project);
            if (bookmark instanceof LineBookmarkImpl lineBookmark) {
                storage.removeElement(new Element(Type.BOOKMARK, "")
                        .setGroup(group.getName())
                        .setFileDescriptor(lineBookmark.getFile().getPath())
                        .setLinenumber(lineBookmark.getLine())
                );
            } else if (bookmark instanceof FileBookmarkImpl fileBookmark) {
                storage.removeElement(new Element(Type.BOOKMARK, "")
                        .setGroup(group.getName())
                        .setFileDescriptor(fileBookmark.getFile().getPath())
                        .setLinenumber(-1));
            }
        } catch (IOException e) {
            Notify.error("Bookmark synchronization failed!");
        }
    }

    public void bookmarkChanged(@NotNull BookmarkGroup group, @NotNull Bookmark bookmark) {
        try {
            List<Element> affected = new ArrayList<>();
            for (Element element : MyBookmarkManager.getBookmarkManager().getAllBookmarks(project)) {
                if (StringUtils.isNotEmpty(element.getGroup()) && element.getGroup().equals(group.getName())) {
                    affected.add(element);
                }
            }
            if (CollectionUtils.isNotEmpty(affected)) {
                Storage storage = Storage.getStorage(project);
                storage.addElements(affected);
            }
        } catch (IOException e) {
            Notify.error("Bookmark synchronization failed!");
        }
    }

    public void structureChanged(@Nullable Object node) {
    }
}
