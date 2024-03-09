package io.github.hhy.bookmark;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupManager;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.manager.MyBookmarkManager;
import io.github.hhy.bookmark.notify.Notify;
import io.github.hhy.bookmark.storage.Storage;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BookMarkProject implements ProjectManagerListener {

    private static final String SEPARATOR = "#";

    @Override
    public void projectOpened(@NotNull Project project) {
        StartupManager.getInstance(project).runAfterOpened(() -> {
            Storage storage = Storage.getStorage(project);
            try {
                // init in open project
                storage.init();

                // compare and recovery
                load(project, storage);
            } catch (Exception e) {
                e.printStackTrace();
                Notify.error(e.getMessage());
            }
        });
    }

    public static void load(Project project, Storage storage) throws IOException {
        List<Element> backups = storage.getElements();
        if (CollectionUtils.isNotEmpty(backups)) {
            MyBookmarkManager myBookmarkManager = MyBookmarkManager.getBookmarkManager();

            List<Element> noMatched = new ArrayList<>();
            Map<String, Element> grouped = myBookmarkManager.getAllBookmarks(project).stream()
                    .collect(Collectors.toMap(item -> item.getFileDescriptor() + SEPARATOR + item.getLinenumber(), Function.identity()));

            for (Element element : backups) {
                Element existEle = grouped.get(element.getFileDescriptor() + SEPARATOR + element.getLinenumber());
                if (existEle != null) {
                    grouped.remove(element.getFileDescriptor() + SEPARATOR + element.getLinenumber());
                } else noMatched.add(element);
            }

            if (CollectionUtils.isNotEmpty(noMatched)) {
                // recovery
                myBookmarkManager.addBookmarks(project, noMatched);
            }

            if (grouped.size() > 0) {
                storage.addElements(new ArrayList<>(grouped.values()));
            }
        }
    }

    private static List<Element> fuzzyMatching(List<Element> existBookmarks, List<Element> elements) {
        return elements;
    }
}