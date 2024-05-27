package io.github.hhy.bookmark.element

class BookmarkElement(
    var fileDescriptor: String,
    var linenumber: Int,
    var name: String = "",
    var group: String = "",
    var bookmarkType: String = "",
    var index: Int = 0,
) : Element(ElementType.BOOKMARK) {

}