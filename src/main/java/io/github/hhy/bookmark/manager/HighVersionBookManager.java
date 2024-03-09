package io.github.hhy.bookmark.manager;

import com.intellij.ide.bookmark.*;
import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.element.Type;
import io.github.hhy.bookmark.notify.Notify;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HighVersionBookManager implements MyBookmarkManager {

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
        Map<String, List<Element>> grouped = elements.stream().collect(Collectors.groupingBy(Element::getFileDescriptor));
        for (Map.Entry<String, List<Element>> entry : grouped.entrySet()) {
            for (Element element : entry.getValue()) {
                try {
                    switch (Type.valueOf(element.getType())) {
                        case GROUP -> addGroup(bookmarksManager, element.getName());
                        case BOOKMARK -> {
                            BookmarkGroup group = addGroup(bookmarksManager, element.getGroup());
                            addBookmark(bookmarksManager, group, element);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Notify.error(e.getMessage());
                }
            }
        }
    }

    public BookmarkGroup addGroup(BookmarksManager bookmarksManager, String groupName) {
        BookmarkGroup group = bookmarksManager.getGroup(groupName);
        if (group == null) {
            return bookmarksManager.addGroup(groupName, false);
        }
        return group;
    }

    public void addBookmark(BookmarksManager bookmarksManager, BookmarkGroup group, Element element) {
        if (StringUtils.isNotEmpty(element.getFileDescriptor())) {
            BookmarkType type = BookmarkType.valueOf(element.getBookmarkType());

            BookmarkState bookmarkState = new BookmarkState();
            bookmarkState.setProvider("com.intellij.ide.bookmark.providers.LineBookmarkProvider");
            bookmarkState.setDescription(element.getName());
            bookmarkState.setType(type);
            bookmarkState.getAttributes().put("url", getFileDescriptorUrl(element.getFileDescriptor()));
            if (element.getLinenumber() != -1) {
                bookmarkState.getAttributes().put("line", String.valueOf(element.getLinenumber()));
            }
            Bookmark bookmark = bookmarksManager.createBookmark(bookmarkState);
            if (bookmark != null) {
                boolean success = group.add(bookmark, type, element.getName());
                if (!success) Notify.error("Bookmark add fail, description:[" + element + "]");
            }
        }
    }

    private String getFileDescriptorUrl(String fileDescriptor) {
        // in jar file?
        if (fileDescriptor.contains(".jar!")) {
            return "jar://" + fileDescriptor;
        }
        return "file://" + fileDescriptor;
    }
}