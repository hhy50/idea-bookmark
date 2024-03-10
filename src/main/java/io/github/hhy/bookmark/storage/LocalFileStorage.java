package io.github.hhy.bookmark.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.element.Type;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LocalFileStorage implements Storage {

    public static String DEFAULT_FILE = ".idea" + File.separator + "bookmarks.json";

    private Project project;

    private Path file;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public LocalFileStorage(Project project) {
        this.project = project;
        this.file = Path.of(project.getBasePath(), DEFAULT_FILE);
    }

    @Override
    public List<Element> getElements() throws IOException {
        String fileStr = Files.readString(file);
        if (StringUtils.isEmpty(fileStr)) {
            return Collections.emptyList();
        }
        return Arrays.asList(gson.fromJson(fileStr, Element[].class));
    }

    @Override
    public void addElements(List<Element> adds) throws IOException {
        List<Element> rem = new ArrayList<>();
        List<Element> all = new ArrayList<>(getElements());
        for (Element ele : adds) {
            List<Element> matched = all.stream().filter(item -> elementEq(item, ele)).toList();
            rem.addAll(matched);
        }
        all.removeAll(rem);
        all.addAll(adds);
        storage(all);
    }

    @Override
    public void removeElement(Element element) throws IOException {
        List<Element> elements = new ArrayList<>(getElements());
        Optional<Element> r = elements.stream().filter(item -> elementEq(item, element)).findFirst();
        if (r.isPresent()) {
            elements.remove(r.get());
            storage(elements);
        }
    }

    @Override
    public void storage(List<Element> elements) throws IOException {
        if (elements == null || elements.size() == 0) {
            Files.write(file, new byte[0]);
            return;
        }
        Collections.sort(elements);
        Files.writeString(file, gson.toJson(elements));
    }

    @Override
    public Element getBookmarkElement(String fileDescription, int linenumber) throws IOException {
        Element target = Element.createBookmark(fileDescription, linenumber);
        for (Element element : getElements()) {
            if (elementEq(element, target)) {
                return element;
            }
        }
        return null;
    }

    public static boolean elementEq(Element ele1, Element ele2) {
        if (!ele1.elementType().equals(ele2.elementType())) {
            return false;
        }
        if (ele1.elementType() == Type.GROUP) {
            return ele1.name().equals(ele2.name());
        }
        return ele1.fileDescriptor().equals(ele2.fileDescriptor())
                && ele1.linenumber() == ele2.linenumber();
    }
}
