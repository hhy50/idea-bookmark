package io.github.hhy.bookmark

import com.intellij.ide.bookmark.*
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.element.*
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
                    storage.getBookmark(bookmarks[0].key())?.group?.let { groupName ->
                        storage.getGroup(groupName)?.also {
                            storage.renameGroup(groupName, group.name)
                        }
                    }
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
                Element.withBookmark(
                    bookmark.fileDescriptor(), bookmark.linenumber(),
                    getBookmarkName(group.name, bookmark.key()), group.name,
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
                it.name = getBookmarkName(group.name, bookmark.key())
                it.group = group.name
            }
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    override fun bookmarkTypeChanged(bookmark: Bookmark) {
        try {
            storage.getBookmark(bookmark.key())!!.let {
                it.bookmarkType = bookmarksManager.getType(bookmark)!!.toString()
            }
            storage.storage()
        } catch (e: Exception) {
            Notify.error("Bookmark synchronization failed! msg=${e.message}")
        }
    }

    /**
     * 获取指定书签的持久化数据
     */
    private fun getBookmarkName(groupName: String, key: String): String {
//        fun BookmarksManagerImpl.getBookmarkName(bookmark: Bookmark) {
//            this.groups
//        }

        val allBookmarks = MyBookmarkManager.bookmarkManager.getAllBookmarks(project)
        return allBookmarks[groupName]?.get(key)?.name ?: ""
    }
}



