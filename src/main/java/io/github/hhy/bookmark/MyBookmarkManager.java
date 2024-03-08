package io.github.hhy.bookmark;

import com.intellij.ide.bookmark.*;
import com.intellij.ide.bookmarks.Bookmark;
import com.intellij.ide.bookmarks.BookmarkManager;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.BuildNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.element.Type;
import io.github.hhy.bookmark.notify.Notify;
import io.github.hhy.bookmark.util.ReflectionUtil;
import org.apache.commons.collections.CollectionUtils;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface MyBookmarkManager {

    static MyBookmarkManager getBookmarkManager() {
        BuildNumber build = ApplicationInfo.getInstance().getBuild();
        if (build.getBaselineVersion() >= 231) {
            return new HighVersionBookManager();
        }
        return new LowVersionBookManager();
    }

    /**
     * get all
     *
     * @return
     */
    public List<Element> getAllBookmarks(Project project);

    default void addBookmarks(Project project, List<Element> elements) {
    }

    class LowVersionBookManager implements MyBookmarkManager {

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

    class HighVersionBookManager implements MyBookmarkManager {

        @Override
        public List<Element> getAllBookmarks(Project project) {
            List<Element> result = new ArrayList<>();
            if (project.getService(BookmarksManager.class) instanceof BookmarksManagerImpl bookmarkManager) {
                ManagerState managerState = bookmarkManager.getState();
                for (GroupState group : managerState.getGroups()) {
                    Element gEle = new Element(Type.GROUP, group.getName())
                            .setFileDescriptor(group.getName());
                    result.add(gEle);

                    for (BookmarkState bookmark : group.getBookmarks()) {
                        Map<String, String> attr = bookmark.getAttributes();
                        Element bEle = new Element(Type.BOOKMARK, bookmark.getDescription())
                                .setGroup(group.getName())
                                .setBookmarkType(bookmark.getType().toString())
                                .setLinenumber(Optional.ofNullable(attr.get("line")).map(Integer::parseInt).orElseGet(() -> -1))
                                .setFileDescriptor(Optional.ofNullable(attr.get("url")).map(URI::create).map(URI::getPath).orElseGet(() -> ""));
                        result.add(bEle);
                    }
                }
            }
            return result;
        }

        @Override
        public void addBookmarks(Project project, List<Element> elements) {
            if (CollectionUtils.isEmpty(elements)) return;
            BookmarksManager bookmarksManager = project.getService(BookmarksManager.class);
            VirtualFileManager virtualFileManager = ApplicationManager.getApplication().getService(VirtualFileManager.class);

            Map<String, List<Element>> grouped = elements.stream().collect(Collectors.groupingBy(Element::getFileDescriptor));
            for (Map.Entry<String, List<Element>> entry : grouped.entrySet()) {
                String file = entry.getKey();
                VirtualFile virtualFile = virtualFileManager.findFileByNioPath(Path.of(file));
                if (virtualFile == null) continue;

                for (Element element : entry.getValue()) {
                    switch (Type.valueOf(element.getType())) {
                        case GROUP -> addGroup(bookmarksManager, element.getName());
                        case BOOKMARK -> {
                            addGroup(bookmarksManager, element.getGroup());
                            addBookmark(bookmarksManager, element);
                        }
                    }
                }
            }
        }

        public void addGroup(BookmarksManager bookmarksManager, String groupName) {
            if (bookmarksManager.getGroup(groupName) == null) {
                bookmarksManager.addGroup(groupName, false);
            }
        }

        public void addBookmark(BookmarksManager bookmarksManager, Element element) {
//            bookmarksManager.createBookmark();
        }
    }
}
