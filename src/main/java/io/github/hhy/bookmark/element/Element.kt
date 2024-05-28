package io.github.hhy.bookmark.element


open class Element {
    companion object {
        const val SEPARATOR = "#"

        @JvmStatic
        fun withGroup(groupName: String, bookmarks: List<BookmarkElement> = listOf()): GroupElement =
            GroupElement(groupName, HashMap(bookmarks.associateBy { it.key() }))

        @JvmStatic
        fun withBookmark(
            fileDescriptor: String, linenumber: Int,
            name: String,
            group: String,
            bookmarkType: String,
        ): BookmarkElement =
            BookmarkElement(fileDescriptor, linenumber, name, group, bookmarkType)
    }
}