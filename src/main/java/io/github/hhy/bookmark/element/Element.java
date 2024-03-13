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

    /**
     * createNewGroup
     *
     * @param name
     */
    public static Element createGroup(String name) {
        return ElementBuilder.anElement()
                .withElementType(Type.GROUP)
                .withName(name)
                .build();
    }

    /**
     * createBookmark
     *
     * @param fileDescriptor
     * @param linenumber
     * @return
     */
    public static Element createBookmark(String fileDescriptor, int linenumber) {
        return ElementBuilder.anElement()
                .withElementType(Type.BOOKMARK)
                .withFileDescriptor(fileDescriptor)
                .withLinenumber(linenumber)
                .build();
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

    public static boolean elementEq(Element ele1, Element ele2) {
        if (!ele1.elementType().equals(ele2.elementType())) {
            return false;
        }
        if (ele1.elementType() == Type.GROUP) {
            return ele1.name().equals(ele2.name());
        }
        return pathEq(ele1.fileDescriptor(), ele2.fileDescriptor())
                && ele1.linenumber() == ele2.linenumber();
    }

    private static boolean pathEq(String fd1, String fd2) {
        return fd1.replaceAll("\\\\+", "/").equals(fd2.replaceAll("\\\\+", "/"));
    }
}
