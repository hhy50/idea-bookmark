package io.github.hhy.bookmark.storage;

import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.Element;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Storage {

    Map<Project, Storage> STORAGE_MAP = new ConcurrentHashMap<>();

    /**
     *
     * @return
     * @throws IOException
     */
    public List<Element> getElements() throws IOException;

    /**
     * Add
     * @param element
     */
    public void addElements(List<Element> element) throws IOException;

    default void addElement(Element element) throws IOException {
        addElements(Arrays.asList(element));
    }

    /**
     * Remove
     * @param element
     */
    public void removeElement(Element element) throws IOException;

    /**
     * storage
     * @throws IOException
     */
    public void storage() throws IOException;

    /**
     *
     * @param project
     * @return
     */
    static Storage getStorage(Project project)  {
        return STORAGE_MAP.compute(project, (k, storage) -> {
            if (storage == null) {
                storage = new LocalFileStorage(project);
            }
            return storage;
        });
    }

    static void removeStoreCache(Project project) {
        Storage remove = STORAGE_MAP.remove(project);
        try {
            remove.storage();
        } catch (IOException ignore) {

        }
    }
}
