package io.github.hhy.bookmark;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupManager;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.notify.Notify;
import io.github.hhy.bookmark.storage.Storage;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

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
                load(project, storage.read());

                // restore
                storage.storage();
            } catch (Exception e) {
                e.printStackTrace();
                Notify.error(e.getMessage());
            }
        });
    }

    public static void load(Project project, List<Element> backups) {
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
                // Fuzzy matching
                List<Element> diffSet = fuzzyMatching(new ArrayList<>(grouped.values()), noMatched);

                // recovery
                myBookmarkManager.addBookmarks(project, diffSet);
            }
        }
    }

    private static List<Element> fuzzyMatching(List<Element> existBookmarks, List<Element> elements) {
        return elements;
    }
}