package io.github.hhy.bookmark.storage;

import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.Element;

import java.io.IOException;
import java.util.List;

public interface Storage {


    /**
     * project in opened call
     */
    default void init() throws Exception {

    }

    /**
     *
     */
    public void storage() throws IOException;

    /**
     *
     * @return
     * @throws IOException
     */
    public List<Element> read() throws IOException;

    /**
     *
     * @param project
     * @return
     */
    static Storage getStorage(Project project) {
        return new LocalFileStorage(project);
    }
}
