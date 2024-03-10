package io.github.hhy.bookmark.element;

public class ElementBuilder {
    private Type elementType;
    private String name;
    private int index;
    private String fileDescriptor;
    private int linenumber;
    private String group;
    private String bookmarkType;

    private ElementBuilder() {
    }

    public static ElementBuilder anElement() {
        return new ElementBuilder();
    }

    public ElementBuilder withElementType(Type elementType) {
        this.elementType = elementType;
        return this;
    }

    public ElementBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ElementBuilder withIndex(int index) {
        this.index = index;
        return this;
    }

    public ElementBuilder withFileDescriptor(String fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
        return this;
    }

    public ElementBuilder withLinenumber(int linenumber) {
        this.linenumber = linenumber;
        return this;
    }

    public ElementBuilder withGroup(String group) {
        this.group = group;
        return this;
    }

    public ElementBuilder withBookmarkType(String bookmarkType) {
        this.bookmarkType = bookmarkType;
        return this;
    }

    public Element build() {
        return new Element(elementType, name, index, fileDescriptor, linenumber, group, bookmarkType);
    }
}