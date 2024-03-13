package io.github.hhy.bookmark.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import io.github.hhy.bookmark.element.Element;
import io.github.hhy.bookmark.element.ElementBuilder;
import io.github.hhy.bookmark.element.Type;
import io.github.hhy.bookmark.util.FDUtil;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.hhy.bookmark.element.Element.elementEq;
import static io.github.hhy.bookmark.util.FDUtil.PROJECT_RELATIVE;

public class LocalFileStorage implements Storage {

    public static String DEFAULT_FILE = ".idea" + File.separator + "bookmarks.json";

    private Project project;

    private final List<Element> elements = new ArrayList<>();

    private Path storeFile;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public LocalFileStorage(Project project) {
        this.project = project;
        this.storeFile = Path.of(project.getBasePath(), DEFAULT_FILE);
        this.checkFile();
    }

    private void checkFile() {
        File f = storeFile.toFile();
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            elements.addAll(readFromLocalFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Element> getElements() throws IOException {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public synchronized void addElements(List<Element> adds) throws IOException {
        List<Element> rem = new ArrayList<>();
        for (Element ele : adds) {
            List<Element> matched = elements.stream().filter(item -> elementEq(item, ele)).toList();
            rem.addAll(matched);
        }
        elements.removeAll(rem);
        elements.addAll(adds);
    }

    @Override
    public synchronized void removeElement(Element element) throws IOException {
        Optional<Element> r = elements.stream().filter(item -> elementEq(item, element)).findFirst();
        r.ifPresent(elements::remove);
    }

    @Override
    public void storage() throws IOException {
        if (elements.size() == 0) {
            Files.write(storeFile, new byte[0]);
            return;
        }
        FileUtil.writeToFile(storeFile.toFile(), gson.toJson(elements.stream().map(item -> {
            if (item.elementType() != Type.BOOKMARK) {
                return item;
            }
            if (item.fileDescriptor().startsWith(project.getBasePath())) {
                return ElementBuilder.anElement()
                        .withElementType(item.elementType())
                        .withName(item.name())
                        .withFileDescriptor(FDUtil.toRelative(item.fileDescriptor(), project.getBasePath()))
                        .withLinenumber(item.linenumber())
                        .withIndex(item.index())
                        .withBookmarkType(item.bookmarkType())
                        .withGroup(item.group())
                        .build();
            }
            return item;
        }).sorted().toList()), Charset.defaultCharset());
    }

    private List<Element> readFromLocalFile() throws IOException {
        String fileStr = Files.readString(storeFile);
        if (StringUtils.isEmpty(fileStr)) Collections.emptyList();

        return gson.fromJson(fileStr, new TypeToken<List<Element>>() {
        }).stream().map(item -> {
            if (item.elementType() != Type.BOOKMARK) {
                return item;
            }
            if (item.fileDescriptor().startsWith(PROJECT_RELATIVE)) {
                return ElementBuilder.anElement()
                        .withElementType(item.elementType())
                        .withName(item.name())
                        .withFileDescriptor(FDUtil.toAbsolute(item.fileDescriptor(), project.getBasePath()))
                        .withLinenumber(item.linenumber())
                        .withIndex(item.index())
                        .withBookmarkType(item.bookmarkType())
                        .withGroup(item.group())
                        .build();
            }
            return item;
        }).toList();
    }
}
