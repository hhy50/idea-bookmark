package io.github.hhy.bookmark.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.project.Project;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.element.Type;
import kotlinx.html.P;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
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
        Files.write(file, gson.toJson(elements).getBytes());
    }

    @Override
    public Element getBookmarkElement(String fileDescription, int linenumber) throws IOException {
        Element target = new Element(Type.BOOKMARK, "")
                .setFileDescriptor(fileDescription).setLinenumber(linenumber);
        for (Element element : getElements()) {
            if (elementEq(element, target)) {
                return element;
            }
        }
        return null;
    }

    public static boolean elementEq(Element ele1, Element ele2) {
        if (!ele1.getType().equals(ele2.getType())) {
            return false;
        }
        if (ele1.getType().equals(Type.GROUP.toString())) {
            return ele1.getName().equals(ele2.getName());
        }
        return ele1.getFileDescriptor().equals(ele2.getFileDescriptor())
                && ele1.getLinenumber() == ele2.getLinenumber();
    }
}
