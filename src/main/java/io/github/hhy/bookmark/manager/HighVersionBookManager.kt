package io.github.hhy.bookmark.manager

import com.intellij.ide.bookmark.*
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.element.ElementBuilder
import io.github.hhy.bookmark.element.ElementType
import io.github.hhy.bookmark.notify.Notify
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang.StringUtils
import java.io.File

class HighVersionBookManager : MyBookmarkManager {
    override fun getAllBookmarks(project: Project): List<Element> {
        val bookmarkManager = project.getService(BookmarksManager::class.java)
        if (bookmarkManager is BookmarksManagerImpl) {
            return bookmarkManager.state.groups.map { group ->
                listOf(Element.withGroup(group.name), *group.bookmarks.map { bookmark ->
                    val attr = bookmark.attributes
                    ElementBuilder.anElement()
                        .withElementType(ElementType.BOOKMARK)
                        .withName(bookmark.description)
                        .withFileDescriptor(attr["url"]!!.let(::urlToFileDescriptor))
                        .withLinenumber(attr["line"]?.let(Integer::parseInt) ?: -1)
                        .withGroup(group.name)
                        .withBookmarkType(bookmark.type.toString())
                        .build()
                }.toTypedArray())
            }.flatten()
        }
        return emptyList()
    }

    override fun addBookmarks(project: Project, elements: List<Element>) {
        if (CollectionUtils.isEmpty(elements)) return
        val bookmarksManager = project.getService(BookmarksManager::class.java)
        for (element in elements) {
            try {
                when (element.elementType) {
                    ElementType.GROUP -> addGroup(bookmarksManager, element.name)
                    ElementType.BOOKMARK -> {
                        val group: BookmarkGroup = addGroup(bookmarksManager, element.group!!)
                        addBookmark(bookmarksManager, group, element)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Notify.error(e.message)
            }
        }
    }

    override fun removeInvalid(project: Project): List<Element> {
        val invalid: MutableList<Element> = ArrayList()
        val bookmarksManager = project.getService(BookmarksManager::class.java)
        for (bookmark in bookmarksManager.bookmarks) {
            if (bookmark.javaClass.name.contains("InvalidBookmark")) {
                bookmarksManager.remove(bookmark)
                val attr = bookmark.attributes
                val ele: Element = Element.withBookmark(attr["url"] ?: "", attr["line"]?.toInt() ?: -1)
                invalid.add(ele)
            }
        }
        return invalid
    }

    private fun addGroup(bookmarksManager: BookmarksManager, groupName: String): BookmarkGroup {
        return bookmarksManager.getGroup(groupName) ?: return bookmarksManager.addGroup(groupName, false)!!
    }

    private fun addBookmark(bookmarksManager: BookmarksManager, group: BookmarkGroup, element: Element) {
        if (StringUtils.isNotEmpty(element.fileDescriptor)) {
            val type: BookmarkType = BookmarkType.valueOf(element.bookmarkType!!)
            val bookmarkState = BookmarkState()
            bookmarkState.provider = "com.intellij.ide.bookmark.providers.LineBookmarkProvider"
            bookmarkState.description = element.name
            bookmarkState.type = type

            bookmarkState.attributes["url"] = fileDescriptorToUrl(element.fileDescriptor!!)
            element.linenumber?.let {
                bookmarkState.attributes["line"] = java.lang.String.valueOf(it)
            }
            val bookmark = bookmarksManager.createBookmark(bookmarkState)
            if (bookmark != null) {
                val success = group.add(bookmark, type, element.name)
                if (!success) Notify.error("Bookmark add fail, description:[$element]")
            }
        }
    }


    companion object {
        @JvmStatic
        private fun urlToFileDescriptor(url: String): String {
            val file = File(url)
            if (file.exists()) {
                return file.path
            }
            return if (url.startsWith("jar://")) {
                url.substring(6)
            } else if (url.startsWith("file://")) {
                url.substring(7)
            } else {
                url
            }
        }

        @JvmStatic
        private fun fileDescriptorToUrl(fileDescriptor: String): String {
            // in jar file?
            return if (fileDescriptor.contains(".jar!")) {
                "jar://$fileDescriptor"
            } else "file://$fileDescriptor"
        }
    }
}