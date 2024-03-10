package io.github.hhy.bookmark;

import com.intellij.ide.bookmark.BookmarksListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.util.messages.MessageBusConnection;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.manager.MyBookmarkManager;
import io.github.hhy.bookmark.storage.Storage;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BookmarkProject implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull Project project) {
        StartupManager.getInstance(project).runAfterOpened(() -> {
            // start listener
            MessageBusConnection connect = project.getMessageBus().connect();
            connect.subscribe(BookmarksListener.TOPIC, new BookmarkListener(project));
        });
    }

    public static void load(Project project, Storage storage) throws IOException {
        MyBookmarkManager myBookmarkManager = MyBookmarkManager.getBookmarkManager();
        List<Element> noMatched = new ArrayList<>();
        Map<String, Element> grouped = myBookmarkManager.getAllBookmarks(project).stream()
                .collect(Collectors.toMap(Element::groupByKey, Function.identity()));

        for (Element element : storage.getElements()) {
            Element existEle = grouped.get(element.groupByKey());
            if (existEle != null) {
                grouped.remove(element.groupByKey());
            } else noMatched.add(element);
        }

        if (CollectionUtils.isNotEmpty(noMatched)) {
            // recovery
            myBookmarkManager.addBookmarks(project, noMatched);
        }

        List<Element> invalids = myBookmarkManager.removeInvalid(project);
        if (grouped.size() > 0) {
            storage.storage(new ArrayList<>(grouped.values()));
        }

        for (Element invalid : invalids) {
            storage.removeElement(invalid);
        }
    }
}