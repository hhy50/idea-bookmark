package io.github.hhy.bookmark.storage;

import com.intellij.ide.bookmarks.Bookmark;
import io.github.hhy.bookmark.exceptions.AssimilateException;
import io.github.hhy.bookmark.util.ReflectionUtil;

public class Element {

    /**
     * Bookmark Index
     */
    private int index;

    /**
     * Bookmark Description
     */
    private String description;

    /**
     * File descriptor
     */
    private String fileDescriptor;

    /**
     * Line number
     */
    private int linenumber;

    public Element(Bookmark bookmark) {
        this.assimilate(bookmark);
    }

    public int getIndex() {
        return index;
    }

    public Element setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Element setDescription(String description) {
        this.description = description;
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

    public void assimilate(Bookmark bookmark) throws AssimilateException {
        try {
            this.setIndex(ReflectionUtil.getInt(bookmark, "index"))
                    .setDescription(bookmark.getDescription())
                    .setFileDescriptor(bookmark.getFile().getPath())
                    .setLinenumber(bookmark.getLine());
        } catch (Exception e) {
            throw new AssimilateException(e);
        }
    }
}
