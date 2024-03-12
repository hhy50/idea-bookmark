package io.github.hhy.bookmark;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkGroup;
import com.intellij.ide.bookmark.BookmarksListener;
import com.intellij.ide.bookmark.providers.FileBookmarkImpl;
import com.intellij.ide.bookmark.providers.LineBookmarkImpl;
import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.element.ElementBuilder;
import io.github.hhy.bookmark.element.Type;
import io.github.hhy.bookmark.manager.MyBookmarkManager;
import io.github.hhy.bookmark.notify.Notify;
import io.github.hhy.bookmark.storage.Storage;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookmarkListener implements BookmarksListener {

    private final Project project;

    public BookmarkListener(Project project) {
        this.project = project;
    }

    public void groupsSorted() {

    }

    public void groupAdded(@NotNull BookmarkGroup group) {
        try {
            Storage storage = Storage.getStorage(project);
            storage.addElement(Element.createGroup(group.getName()));
            storage.storage();
        } catch (IOException e) {
            Notify.error(e.getMessage());
        }
    }

    public void groupRemoved(@NotNull BookmarkGroup group) {
        try {
            Storage storage = Storage.getStorage(project);
            storage.removeElement(Element.createGroup(group.getName()));
            storage.storage();
        } catch (IOException e) {
            Notify.error(e.getMessage());
        }
    }

    public void groupRenamed(@NotNull BookmarkGroup group) {
        try {
            List<Element> affected = new ArrayList<>();
            for (Element element : MyBookmarkManager.getBookmarkManager().getAllBookmarks(project)) {
                if (element.elementType() == Type.BOOKMARK && element.group().equals(group.getName())) {
                    affected.add(element);
                }
            }
            if (CollectionUtils.isNotEmpty(affected)) {
                Storage storage = Storage.getStorage(project);
                storage.addElement(Element.createGroup(group.getName()));
                storage.addElements(affected);
                storage.storage();
            }
        } catch (IOException e) {
            Notify.error("Bookmark synchronization failed!");
        }
    }

    public void bookmarkAdded(@NotNull BookmarkGroup group, @NotNull Bookmark bookmark) {
        try {
            Element element = null;
            if (bookmark instanceof LineBookmarkImpl lineBookmark) {
                element = MyBookmarkManager.getBookmarkManager()
                        .getBookmark(project, lineBookmark.getFile().getPath(), lineBookmark.getLine());
            } else if (bookmark instanceof FileBookmarkImpl fileBookmark) {
                element = MyBookmarkManager.getBookmarkManager()
                        .getBookmark(project, fileBookmark.getFile().getPath(), -1);
            }
            if (element == null) return;
            Storage storage = Storage.getStorage(project);
            storage.addElement(element);
            storage.storage();
        } catch (IOException e) {
            Notify.error("Bookmark synchronization failed!");
        }
    }

    public void bookmarkRemoved(@NotNull BookmarkGroup group, @NotNull Bookmark bookmark) {
        try {
            Storage storage = Storage.getStorage(project);
            if (bookmark instanceof LineBookmarkImpl lineBookmark) {
                Element element = ElementBuilder.anElement()
                        .withElementType(Type.BOOKMARK)
                        .withFileDescriptor(lineBookmark.getFile().getPath())
                        .withLinenumber(lineBookmark.getLine())
                        .build();
                storage.removeElement(element);
            } else if (bookmark instanceof FileBookmarkImpl fileBookmark) {
                Element element = ElementBuilder.anElement()
                        .withElementType(Type.BOOKMARK)
                        .withFileDescriptor(fileBookmark.getFile().getPath())
                        .withLinenumber(-1)
                        .build();
                storage.removeElement(element);
            }
            storage.storage();
        } catch (IOException e) {
            Notify.error("Bookmark synchronization failed!");
        }
    }

    public void bookmarkChanged(@NotNull BookmarkGroup group, @NotNull Bookmark bookmark) {
        try {
            bookmarkChanged(bookmark);
        } catch (IOException e) {
            Notify.error("Bookmark synchronization failed!");
        }
    }

    @Override
    public void bookmarkTypeChanged(@NotNull Bookmark bookmark) {
        try {
            bookmarkChanged(bookmark);
        } catch (IOException e) {
            Notify.error("Bookmark synchronization failed!");
        }
    }

    private void bookmarkChanged(Bookmark bookmark) throws IOException {
        String fileDescriptor = null;
        int linenumber = -1;
        if (bookmark instanceof LineBookmarkImpl lineBookmark) {
            fileDescriptor = lineBookmark.getFile().getPath();
            linenumber = lineBookmark.getLine();
        } else if (bookmark instanceof FileBookmarkImpl fileBookmark) {
            fileDescriptor = fileBookmark.getFile().getPath();
        }
        Element element = MyBookmarkManager.getBookmarkManager().getBookmark(project, fileDescriptor, linenumber);
        if (element != null) {
            Storage storage = Storage.getStorage(project);
            storage.addElement(element);
            storage.storage();
        }
    }
}
