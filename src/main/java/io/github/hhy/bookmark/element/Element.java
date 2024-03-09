package io.github.hhy.bookmark.element;

import org.jetbrains.annotations.NotNull;

public class Element implements Comparable<Element> {

    /**
     * Bookmark Index
     */
    private int index;

    /**
     * Bookmark description
     */
    private String name;

    /**
     * File descriptor
     */
    private String fileDescriptor;

    /**
     * Line number
     */
    private int linenumber = -1;

    /**
     * Element type
     */
    private String type;

    /**
     * BookmarkType
     */
    private String bookmarkType;

    /**
     * Bookmark group
     */
    private String group;


    /**
     * @param type
     * @param name
     */
    public Element(Type type, String name) {
        this.setType(type.toString())
                .setName(name);
    }

    public int getIndex() {
        return index;
    }

    public Element setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getName() {
        return name;
    }

    public Element setName(String name) {
        this.name = name;
        return this;
    }

    public String getFileDescriptor() {
        return fileDescriptor;
    }

    public Element setFileDescriptor(String fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
        return this;
    }

    public int getLinenumber() {
        return linenumber;
    }

    public Element setLinenumber(int linenumber) {
        this.linenumber = linenumber;
        return this;
    }

    public String getType() {
        return type;
    }

    public Element setType(String type) {
        this.type = type;
        return this;
    }

    public String getBookmarkType() {
        return bookmarkType;
    }

    public Element setBookmarkType(String bookmarkType) {
        this.bookmarkType = bookmarkType;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public Element setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public int compareTo(@NotNull Element o) {
        if (this.type.equals(Type.GROUP.toString())) {
            return 1;
        } else if (o.type.equals(Type.GROUP.toString())) {
            return -1;
        }
        return this.group.compareTo(o.group);
    }
}
