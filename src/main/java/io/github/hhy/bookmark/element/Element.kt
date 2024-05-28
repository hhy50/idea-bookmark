package io.github.hhy.bookmark.element

import io.github.hhy.bookmark.util.FDUtil


open class Element(
    val elementType: ElementType,
    val name: String,
) {
    companion object {
        const val SEPARATOR = "#"

        @JvmStatic
        fun withGroup(groupName: String): GroupElement = GroupElement(groupName)

        @JvmStatic
        fun withBookmark(
            fileDescriptor: String, linenumber: Int,
            name: String ,
            group: String,
            bookmarkType: String,
        ): BookmarkElement =
            BookmarkElement(fileDescriptor, linenumber, name, group, bookmarkType)
    }

    fun groupByKey(): String {
        return if (this is GroupElement) {
            "GROUP_${SEPARATOR}$name"
        } else if (this is BookmarkElement) {
            "${FDUtil.formatSeparator(fileDescriptor)}$SEPARATOR$linenumber"
        } else {
            ""
        }
    }
}