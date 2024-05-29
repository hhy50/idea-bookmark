package io.github.hhy.bookmark

import com.intellij.ide.bookmarks.Bookmark
import com.intellij.ide.bookmarks.BookmarksListener
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.element.Element.Companion.withBookmark
import io.github.hhy.bookmark.element.fileDescriptor
import io.github.hhy.bookmark.element.key
import io.github.hhy.bookmark.element.linenumber
import io.github.hhy.bookmark.notify.Notify
import io.github.hhy.bookmark.storage.Storage

class LowVersionBookmarkListener(private val project: Project, private val storage: Storage) : BookmarksListener {
    override fun bookmarkAdded(b: Bookmark) {
        try {
            storage.addBookmark(
                project.name, withBookmark(
                    b.fileDescriptor(),
                    b.linenumber(),
                    b.description,
                    b.type.toString(),
                )
            )
            storage.storage()
        } catch (e: Exception) {
            e.printStackTrace()
            Notify.error(e.message)
        }
    }

    override fun bookmarkRemoved(b: Bookmark) {
        try {
            storage.removeBookmark(project.name, b.key())
            storage.storage()
        } catch (e: Exception) {
            e.printStackTrace()
            Notify.error(e.message)
        }
    }

    override fun bookmarkChanged(b: Bookmark) {
        try {
            storage.getBookmark(b.key())?.let {
                it.name = b.description
                it.bookmarkType = b.type.toString()
            }
            storage.storage()
        } catch (e: Exception) {
            e.printStackTrace()
            Notify.error(e.message)
        }
    }
}