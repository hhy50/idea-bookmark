package io.github.hhy.bookmark.manager;

import com.intellij.ide.bookmarks.Bookmark;
import com.intellij.ide.bookmarks.BookmarkManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.element.Type;
import io.github.hhy.bookmark.notify.Notify;
import io.github.hhy.bookmark.util.ReflectionUtil;
import org.apache.commons.collections.CollectionUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LowVersionBookManager implements MyBookmarkManager {

    @Override
    public List<Element> getAllBookmarks(Project project) {
        List<Element> result = new ArrayList<>();
        BookmarkManager bookmarkManager = project.getService(BookmarkManager.class);
        for (Bookmark bookmark : bookmarkManager.getAllBookmarks()) {
            Element element = new Element(Type.BOOKMARK, bookmark.getDescription())
                    .setIndex(ReflectionUtil.getInt(bookmark, "index"))
                    .setBookmarkType(bookmark.getType().toString())
                    .setLinenumber(bookmark.getLine())
                    .setFileDescriptor(bookmark.getFile().getPath());
            result.add(element);
        }
        return result;
    }

    @Override
    public void addBookmarks(Project project, List<Element> elements) {
        if (CollectionUtils.isEmpty(elements)) return;
        BookmarkManager bookmarkManager = project.getService(BookmarkManager.class);
        VirtualFileManager virtualFileManager = ApplicationManager.getApplication().getService(VirtualFileManager.class);
        for (Element d : elements) {
            try {
                VirtualFile virtualFile = virtualFileManager.findFileByNioPath(Path.of(d.getFileDescriptor()));
                if (virtualFile != null)
                    bookmarkManager.addTextBookmark(virtualFile, d.getLinenumber(), d.getName());
            } catch (Exception e) {
                Notify.error(e.getMessage());
            }
        }
    }
}