package io.github.hhy.bookmark.element

import io.github.hhy.bookmark.util.FDUtil


class Element(
    val elementType: ElementType,
    val name: String,
    var index: Int,
    var fileDescriptor: String?,
    var linenumber: Int?,
    var group: String?,
    var bookmarkType: String?
) {


    companion object {
        const val SEPARATOR = "#"

        @JvmStatic
        fun withGroup(groupName: String): Element = Element(ElementType.GROUP, groupName, -1, null, null, null, null)

        @JvmStatic
        fun withBookmark(fileDescriptor: String, linenumber: Int): Element =
            Element(ElementType.BOOKMARK, fileDescriptor, linenumber, null, null, null, null)
    }


    fun groupByKey(): String {
        return if (elementType == ElementType.GROUP) {
            "GROUP_${SEPARATOR}$name"
        } else
            "${FDUtil.formatSeparator(fileDescriptor)}$SEPARATOR$linenumber"
    }

}