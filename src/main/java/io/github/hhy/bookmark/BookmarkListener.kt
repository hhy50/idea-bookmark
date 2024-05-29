package io.github.hhy.bookmark

import com.intellij.ide.bookmark.Bookmark
import com.intellij.ide.bookmark.BookmarkGroup
import com.intellij.ide.bookmark.BookmarksListener
import com.intellij.ide.bookmark.BookmarksManager
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.element.fileDescriptor
import io.github.hhy.bookmark.element.key
import io.github.hhy.bookmark.element.linenumber
import io.github.hhy.bookmark.manager.MyBookmarkManager
import io.github.hhy.bookmark.notify.Notify
import io.github.hhy.bookmark.storage.Storage
import java.io.IOException

class BookmarkListener(val project: Project, val storage: Storage) : BookmarksListener {

    val bookmarksManager: BookmarksManager = project.getService(BookmarksManager::class.java)

    override fun groupAdded(group: BookmarkGroup) {
        try {
            storage.addGroup(Element.withGroup(group.name))
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun groupRemoved(group: BookmarkGroup) {
        try {
            storage.removeGroup(group.name)
            storage.storage()
        } catch (e: IOException) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun groupRenamed(group: BookmarkGroup) {
        try {
            val bookmarks = group.getBookmarks()
            when (bookmarks.size) {
                0 -> groupAdded(group)
                else -> {
                    val groupElement = storage.elements().filter {
                        bookmarks[0].key() in it.bookmarks
                    }[0]
                    storage.renameGroup(groupElement.name, group.name)
                }
            }
            storage.storage()
        } catch (e: IOException) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkAdded(group: BookmarkGroup, bookmark: Bookmark) {
        try {
            storage.addBookmark(
                group.name,
                Element.withBookmark(
                    bookmark.fileDescriptor(), bookmark.linenumber(),
                    getBookmarkName(group.name, bookmark.key()),
                    bookmarksManager.getType(bookmark)!!.toString()
                )
            )
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkRemoved(group: BookmarkGroup, bookmark: Bookmark) {
        try {
            storage.removeBookmark(group.name, bookmark.key())
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkChanged(group: BookmarkGroup, bookmark: Bookmark) {
        try {
            storage.getBookmark(bookmark.key())!!.let {
                it.name = getBookmarkName(group.name, bookmark.key())
                it.bookmarkType = bookmarksManager.getType(bookmark)!!.toString()
            }
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkTypeChanged(bookmark: Bookmark) {
        val groups = bookmarksManager.groups
        bookmarkChanged(groups[0], bookmark)
    }

    /**
     * 获取书签名称
     */
    private fun getBookmarkName(groupName: String, key: String): String {
        val allBookmarks = MyBookmarkManager.bookmarkManager.getAllBookmarks(project)
        return allBookmarks[groupName]?.get(key)?.name ?: ""
    }
}



