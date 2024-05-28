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
import io.github.hhy.bookmark.notify.Notify
import io.github.hhy.bookmark.storage.Storage
import java.io.IOException

class BookmarkListener(val project: Project, val storage: Storage) : BookmarksListener {

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
            storage.getGroup(group.name)!!.let {
                it.name = group.name
            }
            storage.storage()
        } catch (e: IOException) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkAdded(group: BookmarkGroup, bookmark: Bookmark) {
        try {
            val bookmarksManager = project.getService(BookmarksManager::class.java)
            // TODO
            storage.addBookmark(
                Element.withBookmark(
                    bookmark.fileDescriptor(), bookmark.linenumber(),
                    "", group.name,
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
            storage.removeBookmark(bookmark.key())
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkChanged(group: BookmarkGroup, bookmark: Bookmark) {
        try {
            storage.getBookmark(bookmark.key())!!.let {
                it.name = "" // TODO
                it.group = group.name
            }
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkTypeChanged(bookmark: Bookmark) {
        try {
            val bookmarksManager = project.getService(BookmarksManager::class.java)
            storage.getBookmark(bookmark.key())!!.let {
                it.bookmarkType = bookmarksManager.getType(bookmark)!!.toString()
            }
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }
}

