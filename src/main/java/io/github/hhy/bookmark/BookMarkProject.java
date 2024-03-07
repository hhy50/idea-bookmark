package io.github.hhy.bookmark;

import com.intellij.ide.bookmarks.Bookmark;
import com.intellij.ide.bookmarks.BookmarkManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import io.github.hhy.bookmark.notify.Notify;
import io.github.hhy.bookmark.storage.Element;
import io.github.hhy.bookmark.storage.Storage;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
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
                Notify.error(e.getMessage());
            }
        });
    }

    /**
     * compare and recovery
     *
     * @param project
     * @param backups
     */
    public static void load(Project project, List<Element> backups) {
        if (CollectionUtils.isNotEmpty(backups)) {
            BookmarkManager bookmarkManager = project.getService(BookmarkManager.class);
            List<Element> noMatched = new ArrayList<>();
            Map<String, Bookmark> grouped = bookmarkManager.getAllBookmarks().stream()
                    .collect(Collectors.toMap(item -> item.getFile().getPath() + SEPARATOR + item.getLine(), Function.identity()));
            for (Element element : backups) {
                Bookmark bookmark = grouped.get(element.getFileDescriptor() + SEPARATOR + element.getLinenumber());
                if (bookmark != null) {
                    grouped.remove(element.getFileDescriptor() + SEPARATOR + element.getLinenumber());
                    element.assimilate(bookmark);
                } else noMatched.add(element);
            }

            if (CollectionUtils.isNotEmpty(noMatched)) {
                // Fuzzy matching
                List<Element> diffSet = fuzzyMatching(new ArrayList<>(grouped.values()), noMatched);

                // recovery
                if (CollectionUtils.isEmpty(diffSet)) return;
                VirtualFileManager virtualFileManager = ApplicationManager.getApplication().getService(VirtualFileManager.class);
                for (Element d : diffSet) {
                    try {
                        VirtualFile virtualFile = virtualFileManager.findFileByNioPath(Path.of(d.getFileDescriptor()));
                        if (virtualFile != null) {
                            bookmarkManager.addTextBookmark(virtualFile, d.getLinenumber(), d.getDescription());
                        }
                    } catch (Exception e) {
                        Notify.error(e.getMessage());
                    }
                }
            }
        }
    }

    private static List<Element> fuzzyMatching(List<Bookmark> existBookmarks, List<Element> elements) {
        return elements;
    }
}