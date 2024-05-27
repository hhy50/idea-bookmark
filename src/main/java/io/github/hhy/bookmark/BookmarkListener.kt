package io.github.hhy.bookmark

import com.intellij.ide.bookmark.Bookmark
import com.intellij.ide.bookmark.BookmarkGroup
import com.intellij.ide.bookmark.BookmarksListener
import com.intellij.ide.bookmark.providers.FileBookmarkImpl
import com.intellij.ide.bookmark.providers.LineBookmarkImpl
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.notify.Notify
import io.github.hhy.bookmark.storage.Storage
import java.io.IOException

class BookmarkListener(val project: Project) : BookmarksListener {

    override fun groupAdded(group: BookmarkGroup) {
        try {
            val storage = Storage.getStorage(project)
            storage.addElement(Element.withGroup(group.name))
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun groupRemoved(group: BookmarkGroup) {
        try {
            val storage = Storage.getStorage(project)
            storage.removeElement(Element.withGroup(group.name))
            storage.storage()
        } catch (e: IOException) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun groupRenamed(group: BookmarkGroup) {
        try {
            val storage = Storage.getStorage(project)
            for (bookmark in group.getBookmarks()) {
                val element = bookmark.toEle()
                if (element != null) {
                    storage.findElement(element)?.let {
                        it.group = group.name
                    }
                }
            }
            storage.addElement(Element.withGroup(group.name))
            storage.storage()
        } catch (e: IOException) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkAdded(group: BookmarkGroup, bookmark: Bookmark) {
        try {
            val element = bookmark.toEle()
            if (element != null) {
                val storage = Storage.getStorage(project)
                storage.addElement(element)
                storage.storage()
            }
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkRemoved(group: BookmarkGroup, bookmark: Bookmark) {
        try {
            val element = bookmark.toEle()
            if (element != null) {
                val storage = Storage.getStorage(project)
                storage.removeElement(element)
                storage.storage()
            }
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkChanged(group: BookmarkGroup, bookmark: Bookmark) = changed(group, bookmark)
    override fun bookmarkTypeChanged(bookmark: Bookmark) = changed(null, bookmark)

    private fun changed(group: BookmarkGroup?, bookmark: Bookmark) {
        try {
            val element = bookmark.toEle()?.also {
                // TODO
                it.group = group?.name
            }
            if (element != null) {
                val storage = Storage.getStorage(project)
                storage.addElement(element)
                storage.storage()
            }
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }
}


fun Bookmark.toEle(): Element? {
    return when (this) {
        is LineBookmarkImpl -> Element.withBookmark(this.descriptor.file.path, this.line)
        is FileBookmarkImpl -> Element.withBookmark(this.descriptor.file.path, -1)
        else -> null
    }
}
