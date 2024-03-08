package io.github.hhy.bookmark.element;

public enum Type {
    BOOKMARK("bookmark"),

    GROUP("group"),

    ;

    private final String name;

    Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
