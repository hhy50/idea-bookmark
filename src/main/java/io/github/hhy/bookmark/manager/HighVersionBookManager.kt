package io.github.hhy.bookmark.manager

import com.intellij.ide.bookmark.*
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.element.BookmarkElement
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.element.GroupElement
import io.github.hhy.bookmark.util.FDUtil
import java.io.File

class HighVersionBookManager : MyBookmarkManager {

    override fun getAllBookmarks(project: Project): Map<String, Map<String, BookmarkElement>> {
        val bookmarkManager = project.getService(BookmarksManager::class.java)
        if (bookmarkManager is BookmarksManagerImpl) {
            return bookmarkManager.state.groups.associate { group ->
                group.name to group.bookmarks.associate {
                    val attr = it.attributes
                    val ele = Element.withBookmark(
                        attr["url"]!!.let(HighVersionBookManager::urlToFileDescriptor),
                        attr["line"]?.let(Integer::parseInt) ?: -1,
                        it.description ?: "",
                        group.name,
                        it.type.toString()
                    )
                    ele.key() to ele
                }
            }
        }
        return emptyMap()
    }

    override fun addGroup(project: Project, ele: GroupElement): BookmarkGroup {
        val bookmarksManager = project.getService(BookmarksManager::class.java)
        return bookmarksManager.getGroup(ele.name) ?: return bookmarksManager.addGroup(ele.name, false)!!
    }

    override fun addBookmark(project: Project, ele: BookmarkElement): Bookmark {
        val bookmarksManager = project.getService(BookmarksManager::class.java)
        val group: BookmarkGroup = addGroup(project, Element.withGroup(ele.group))
        val type: BookmarkType = BookmarkType.valueOf(ele.bookmarkType)
        val bookmarkState = BookmarkState().also {
            it.provider = "com.intellij.ide.bookmark.providers.LineBookmarkProvider"
            it.description = ele.name
            it.type = type
            it.attributes["url"] = fileDescriptorToUrl(ele.fileDescriptor)
            if (ele.linenumber != -1) {
                it.attributes["line"] = java.lang.String.valueOf(ele.linenumber)
            }
        }
        val bookmark = bookmarksManager.createBookmark(bookmarkState)
        return bookmark?.also {
            group.add(bookmark, type, ele.name)
        } ?: throw RuntimeException()
    }

    override fun removeInvalid(project: Project): List<Bookmark> {
        val invalid: MutableList<Bookmark> = ArrayList()
        val bookmarksManager = project.getService(BookmarksManager::class.java)
        for (bookmark in bookmarksManager.bookmarks) {
            if (bookmark::class.java.name.contains("InvalidBookmark")) {
                bookmarksManager.remove(bookmark)
                invalid += bookmark
            }
        }
        return invalid
    }

    companion object {
        @JvmStatic
        fun urlToFileDescriptor(url: String): String {
            val file = File(url)
            if (file.exists()) {
                return file.path
            }
            var url = if (url.startsWith("jar://")) {
                url.substring(6)
            } else if (url.startsWith("file://")) {
                url.substring(7)
            } else {
                url
            }
            return FDUtil.formatSeparator(url)
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
