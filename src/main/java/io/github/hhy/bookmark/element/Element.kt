package io.github.hhy.bookmark.element


open class Element {
    companion object {
        const val SEPARATOR = "#"

        @JvmStatic
        fun withGroup(groupName: String, bookmarks: MutableList<BookmarkElement> = arrayListOf()): GroupElement =
            GroupElement(groupName, bookmarks)

        @JvmStatic
        fun withBookmark(
            fileDescriptor: String, linenumber: Int,
            name: String,
            bookmarkType: String,
        ): BookmarkElement =
            BookmarkElement(fileDescriptor, linenumber, name, bookmarkType)
    }
}