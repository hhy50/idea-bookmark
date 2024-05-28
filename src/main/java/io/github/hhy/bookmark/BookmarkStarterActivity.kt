package io.github.hhy.bookmark

import com.intellij.ide.bookmark.BookmarksListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import io.github.hhy.bookmark.element.BookmarkElement
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.element.GroupElement
import io.github.hhy.bookmark.element.key
import io.github.hhy.bookmark.manager.MyBookmarkManager
import io.github.hhy.bookmark.notify.Notify
import io.github.hhy.bookmark.storage.Storage
import kotlinx.coroutines.delay
import java.io.IOException


class BookmarkStarterActivity : ProjectActivity, ProjectManagerListener {
    override suspend fun execute(project: Project) {
        val storage = Storage.getStorage(project)
        try {
            // compare and recovery
            load(project, storage)

            // start listener
            val connect = project.messageBus.connect()
            connect.subscribe(BookmarksListener.TOPIC, BookmarkListener(project, storage))
        } catch (e: Exception) {
            e.printStackTrace()
            Notify.error(e.message)
        }
    }


    @Throws(IOException::class)
    fun load(project: Project, storage: Storage) {
        val myBookmarkManager = MyBookmarkManager.bookmarkManager
        val existedGroup = myBookmarkManager.getAllBookmarks(project)
        val backups = storage.elements()

        // 缺少的
        val missingElements: MutableList<Element> = ArrayList()
        for (group in backups) {
            val bookmarks = existedGroup[group.name] ?: emptyMap()
            if (bookmarks.isEmpty()) {
                missingElements.add(group)
                missingElements.addAll(group.bookmarks.values)
                continue
            }
            // 少的
            for (bookmarkEntry in group.bookmarks) {
                if (bookmarkEntry.key !in bookmarks) {
                    missingElements.add(bookmarkEntry.value)
                }
                (bookmarks as MutableMap).remove(bookmarkEntry.key)
            }
        }
        // 多的
        existedGroup.forEach { (_, group) ->
            for (bookmark in group.values) {
                storage.addBookmark(bookmark)
            }
        }
        for (missingElement in missingElements) {
            when(missingElement) {
                is BookmarkElement -> myBookmarkManager.addBookmark(project, missingElement)
                is GroupElement -> myBookmarkManager.addGroup(project, missingElement)
            }
        }
        for (invalid in myBookmarkManager.removeInvalid(project)) {
            storage.removeBookmark(invalid.key())
        }
        storage.storage()
    }
}