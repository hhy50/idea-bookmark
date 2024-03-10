package io.github.hhy.bookmark.storage;

import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.Element;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public interface Storage {

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
     * @param elements
     * @throws IOException
     */
    public void storage(List<Element> elements) throws IOException;

    /**
     *
     * @return
     */
    Element getBookmarkElement(String fileDescription, int linenumber) throws IOException;

    /**
     *
     * @param project
     * @return
     */
    static Storage getStorage(Project project) {
        return new LocalFileStorage(project);
    }
}
