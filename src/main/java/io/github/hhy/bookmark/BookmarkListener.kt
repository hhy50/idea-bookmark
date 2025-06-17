package io.github.hhy.bookmark

import com.intellij.ide.bookmark.Bookmark
import com.intellij.ide.bookmark.BookmarkGroup
import com.intellij.ide.bookmark.BookmarksListener
import com.intellij.ide.bookmark.BookmarksManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.element.fileDescriptor
import io.github.hhy.bookmark.element.key
import io.github.hhy.bookmark.element.linenumber
import io.github.hhy.bookmark.notify.Notify
import io.github.hhy.bookmark.storage.Storage
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class BookmarkListener(val listener: Listener) : BookmarksListener by listener {
    companion object {
        val InSync: AtomicBoolean = AtomicBoolean(false)
    }

    override fun groupAdded(group: BookmarkGroup) {
        if (InSync.get()) return
        listener.groupAdded(group)
    }

    override fun groupRemoved(group: BookmarkGroup) {
        if (InSync.get()) return
        listener.groupRemoved(group)
    }

    override fun groupRenamed(group: BookmarkGroup) {
        if (InSync.get()) return
        listener.groupRenamed(group)
    }

    override fun bookmarkAdded(
        group: BookmarkGroup,
        bookmark: Bookmark
    ) {
        if (InSync.get()) return
        listener.bookmarkAdded(group, bookmark)
    }

    override fun bookmarkRemoved(
        group: BookmarkGroup,
        bookmark: Bookmark
    ) {
        if (InSync.get()) return
        listener.bookmarkRemoved(group, bookmark)
    }

    override fun bookmarkChanged(
        group: BookmarkGroup,
        bookmark: Bookmark
    ) {
        if (InSync.get()) return
        listener.bookmarkChanged(group, bookmark)
    }

    override fun bookmarkTypeChanged(bookmark: Bookmark) {
        if (InSync.get()) return
        listener.bookmarkTypeChanged(bookmark)
    }
}

class Listener(val project: Project) : BookmarksListener {
    companion object {
        val LOG: Logger = Logger.getInstance(BookmarkListener::class.java)
    }


    val bookmarksManager: BookmarksManager = project.getService(BookmarksManager::class.java)
    val storage: Storage = Storage.getStorage(project)

    override fun groupAdded(group: BookmarkGroup) {
        LOG.info("groupAdded - " + group.name)
        try {
            storage.addGroup(Element.withGroup(group.name))
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun groupRemoved(group: BookmarkGroup) {
        LOG.info("groupRemoved - " + group.name)
        try {
            storage.removeGroup(group.name)
            storage.storage()
        } catch (e: IOException) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun groupRenamed(group: BookmarkGroup) {
        LOG.info("groupRenamed - " + group.name)
        try {
            val bookmarks = group.getBookmarks()
            when (bookmarks.size) {
                0 -> groupAdded(group)
                else -> {
                    val first = storage.elements().firstOrNull {
                        it.bookmarks.any { b -> b.key() == bookmarks[0].key() }
                    }
                    first?.also {
                        storage.renameGroup(it.name, group.name)
                    }
                }
            }
            storage.storage()
        } catch (e: IOException) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkAdded(group: BookmarkGroup, bookmark: Bookmark) {
        LOG.info("bookmarkAdded - " + group.name + ", " + bookmark.fileDescriptor() + ":" + bookmark.linenumber())
        try {
            storage.addBookmark(
                group.name,
                Element.withBookmark(
                    bookmark.fileDescriptor(), bookmark.linenumber(),
                    getBookmarkName(group.name, bookmark),
                    bookmarksManager.getType(bookmark)!!.toString()
                )
            )
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkRemoved(group: BookmarkGroup, bookmark: Bookmark) {
        LOG.info("bookmarkRemoved - " + group.name + ", " + bookmark.fileDescriptor() + ":" + bookmark.linenumber())
        try {
            storage.removeBookmark(group.name, bookmark.key())
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkChanged(group: BookmarkGroup, bookmark: Bookmark) {
        LOG.info("bookmarkChanged - " + group.name + ", " + bookmark.fileDescriptor() + ":" + bookmark.linenumber())
        try {
            storage.getBookmark(bookmark.key())!!.let {
                it.name = getBookmarkName(group.name, bookmark)
                it.bookmarkType = bookmarksManager.getType(bookmark)!!.toString()
            }
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkTypeChanged(bookmark: Bookmark) {
        LOG.info("bookmarkTypeChanged - " + bookmark.fileDescriptor() + ":" + bookmark.linenumber())
        val groups = bookmarksManager.getGroups(bookmark)
        bookmarkChanged(groups[0], bookmark)
    }

    /**
     * 获取书签名称
     */
    private fun getBookmarkName(groupName: String, bookmark: Bookmark): String {
        val group = project.getService(BookmarksManager::class.java).getGroup(groupName)
        return group?.getDescription(bookmark) ?: ""
    }
}
