package io.github.hhy.bookmark.element;

import org.jetbrains.annotations.NotNull;


/**
 * @param elementType    Element type
 * @param name           Bookmark description
 * @param index          Bookmark index
 * @param fileDescriptor File descriptor
 * @param linenumber     Line number
 * @param group          Bookmark group
 * @param bookmarkType   Bookmark type
 */
public record Element(Type elementType, String name,
                      int index, String fileDescriptor, int linenumber, String group, String bookmarkType
) implements Comparable<Element> {

    private static final String SEPARATOR = "#";

    public Element {

    }

    public Element(Type elementType, String fileDescriptor, int linenumber) {
        this(elementType, null, 0, fileDescriptor, linenumber, null, null);
    }


    public Element(Type elementType, String name) {
        this(elementType, name, 0, null, -1, null, null);
    }


    /**
     * createNewGroup
     *
     * @param name
     */
    public static Element createGroup(String name) {
        return new Element(Type.GROUP, name);
    }

    /**
     * createBookmark
     *
     * @param fileDescriptor
     * @param linenumber
     * @return
     */
    public static Element createBookmark(String fileDescriptor, int linenumber) {
        return new Element(Type.BOOKMARK, fileDescriptor, linenumber);
    }

    public String groupByKey() {
        if (this.elementType == Type.GROUP) {
            return "GROUP_" + SEPARATOR + this.name;
        }
        return this.fileDescriptor + SEPARATOR + this.linenumber;
    }

    @Override
    public int compareTo(@NotNull Element o) {
        if (this.elementType == Type.GROUP) {
            return 1;
        } else if (o.elementType == Type.GROUP) {
            return -1;
        }
        return this.group.compareTo(o.group);
    }
}
