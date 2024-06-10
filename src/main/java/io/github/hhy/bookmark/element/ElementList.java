package io.github.hhy.bookmark.element;

import java.util.ArrayList;


@Deprecated
public class ElementList extends ArrayList<ElementList.Item> {

    /**
     * @param elementType    Element type
     * @param name           Bookmark description
     * @param index          Bookmark index
     * @param fileDescriptor File descriptor
     * @param linenumber     Line number
     * @param group          Bookmark group
     * @param bookmarkType   Bookmark type
     */
    public static record Item(ElementType elementType, String name,
                              int index, String fileDescriptor, int linenumber, String group, String bookmarkType
    ) {
        public BookmarkElement toElement() {
            if (this.elementType == ElementType.GROUP) {
                return null;
            } else {
                return Element.withBookmark(this.fileDescriptor, this.linenumber, this.name, this.bookmarkType);
            }
        }
    }
}
