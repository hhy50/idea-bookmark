package io.github.hhy.bookmark.element

data class BookmarkElement(
    var fileDescriptor: String,
    var linenumber: Int,
    var name: String,
    var bookmarkType: String
) : Element() {

    fun key(): String = "${this.fileDescriptor}${SEPARATOR}${this.linenumber}"

    fun clone(): BookmarkElement = BookmarkElement(
        this.fileDescriptor,
        this.linenumber,
        this.name,
        this.bookmarkType
    )
}

