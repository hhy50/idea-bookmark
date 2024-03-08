package io.github.hhy.bookmark.element;

import io.github.hhy.bookmark.exceptions.AssimilateException;

public class Element {

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
    private int linenumber;

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
     *
     * @param type
     * @param name
     */
    public Element(Type type, String name) {
        this.setType(type.toString())
                .setName(name);
    }

    public void assimilate(Element element) throws AssimilateException {
        try {
            this.setIndex(element.getIndex())
                    .setName(element.getName())
                    .setFileDescriptor(element.getFileDescriptor())
                    .setLinenumber(element.getLinenumber());
        } catch (Exception e) {
            throw new AssimilateException(e);
        }
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
}
