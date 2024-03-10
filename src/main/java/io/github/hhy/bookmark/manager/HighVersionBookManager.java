package io.github.hhy.bookmark.manager;

import com.intellij.ide.bookmark.*;
import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.element.ElementBuilder;
import io.github.hhy.bookmark.element.Type;
import io.github.hhy.bookmark.notify.Notify;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HighVersionBookManager implements MyBookmarkManager {

    @Override
    public List<Element> getAllBookmarks(Project project) {
        List<Element> result = new ArrayList<>();
        if (project.getService(BookmarksManager.class) instanceof BookmarksManagerImpl bookmarkManager) {
            ManagerState managerState = bookmarkManager.getState();
            for (GroupState group : managerState.getGroups()) {
                Element gEle = Element.createGroup(group.getName());
                result.add(gEle);

                for (BookmarkState bookmark : group.getBookmarks()) {
                    Map<String, String> attr = bookmark.getAttributes();
                    Element bEle = ElementBuilder.anElement()
                            .withElementType(Type.BOOKMARK)
                            .withName(bookmark.getDescription())
                            .withFileDescriptor(Optional.ofNullable(attr.get("url")).map(HighVersionBookManager::urlToFileDescriptor).orElseGet(() -> ""))
                            .withLinenumber(Optional.ofNullable(attr.get("line")).map(Integer::parseInt).orElseGet(() -> -1))
                            .withGroup(group.getName())
                            .withBookmarkType(bookmark.getType().toString())
                            .build();
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
        for (Element element : elements) {
            try {
                switch (element.elementType()) {
                    case GROUP -> addGroup(bookmarksManager, element.name());
                    case BOOKMARK -> {
                        BookmarkGroup group = addGroup(bookmarksManager, element.group());
                        addBookmark(bookmarksManager, group, element);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Notify.error(e.getMessage());
            }
        }
    }

    @Override
    public Element getBookmark(Project project, String fileDescription, int lineNumber) {
        for (Element element : getAllBookmarks(project)) {
            if (element.elementType() != Type.BOOKMARK) continue;
            if (element.fileDescriptor().equals(fileDescription) && element.linenumber() == lineNumber) {
                return element;
            }
        }
        return null;
    }

    @Override
    public List<Element> removeInvalid(Project project) {
        List<Element> invalid = new ArrayList<>();
        BookmarksManager bookmarksManager = project.getService(BookmarksManager.class);
        for (Bookmark bookmark : bookmarksManager.getBookmarks()) {
            if (bookmark.getClass().getName().contains("InvalidBookmark")) {
                bookmarksManager.remove(bookmark);

                Map<String, String> attr = bookmark.getAttributes();
                Element ele = Element.createBookmark(Optional.ofNullable(attr.get("url")).map(HighVersionBookManager::urlToFileDescriptor).orElseGet(() -> ""),
                        Optional.ofNullable(attr.get("line")).map(Integer::parseInt).orElseGet(() -> -1));
                invalid.add(ele);
            }
        }
        return invalid;
    }

    private BookmarkGroup addGroup(BookmarksManager bookmarksManager, String groupName) {
        BookmarkGroup group = bookmarksManager.getGroup(groupName);
        if (group == null) {
            return bookmarksManager.addGroup(groupName, false);
        }
        return group;
    }

    private void addBookmark(BookmarksManager bookmarksManager, BookmarkGroup group, Element element) {
        if (StringUtils.isNotEmpty(element.fileDescriptor())) {
            BookmarkType type = BookmarkType.valueOf(element.bookmarkType());

            BookmarkState bookmarkState = new BookmarkState();
            bookmarkState.setProvider("com.intellij.ide.bookmark.providers.LineBookmarkProvider");
            bookmarkState.setDescription(element.name());
            bookmarkState.setType(type);
            bookmarkState.getAttributes().put("url", fileDescriptorToUrl(element.fileDescriptor()));
            if (element.linenumber() != -1) {
                bookmarkState.getAttributes().put("line", String.valueOf(element.linenumber()));
            }
            Bookmark bookmark = bookmarksManager.createBookmark(bookmarkState);
            if (bookmark != null) {
                boolean success = group.add(bookmark, type, element.name());
                if (!success) Notify.error("Bookmark add fail, description:[" + element + "]");
            }
        }
    }

    private static String fileDescriptorToUrl(String fileDescriptor) {
        // in jar file?
        if (fileDescriptor.contains(".jar!")) {
            return "jar://" + fileDescriptor;
        }
        return "file://" + fileDescriptor;
    }

    private static String urlToFileDescriptor(String url) {
        File file = new File(url);
        if (file.exists()) {
            return file.getPath();
        }
        if (url.startsWith("jar://")) {
            return url.substring(6);
        }
        if (url.startsWith("file://")) {
            return url.substring(7);
        }
        return url;
    }
}