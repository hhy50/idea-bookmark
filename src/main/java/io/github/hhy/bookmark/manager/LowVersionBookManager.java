package io.github.hhy.bookmark.manager;

import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.Element;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LowVersionBookManager implements MyBookmarkManager {

    @Override
    public List<Element> getAllBookmarks(Project project) {
        List<Element> result = new ArrayList<>();
        return result;
    }

    @Override
    public void addBookmarks(@NotNull Project project, @NotNull List<? extends Element> elements) {
    }

    @NotNull
    @Override
    public List<Pair<String, Integer>> removeInvalid(@Nullable Project project) {
        return new ArrayList<>();
    }
}