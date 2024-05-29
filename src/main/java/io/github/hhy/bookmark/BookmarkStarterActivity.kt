package io.github.hhy.bookmark

import com.intellij.ide.bookmark.BookmarksListener
import com.intellij.ide.bookmark.BookmarksManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.element.GroupElement
import io.github.hhy.bookmark.element.key
import io.github.hhy.bookmark.manager.MyBookmarkManager
import io.github.hhy.bookmark.notify.Notify
import io.github.hhy.bookmark.storage.Storage
import java.io.IOException


class BookmarkStarterActivity : ProjectActivity, ProjectManagerListener {

    val myBookmarkManager = MyBookmarkManager.bookmarkManager

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
        val bookmarkManager = project.getService(BookmarksManager::class.java)
        val existedGroup = myBookmarkManager.getAllBookmarks(project)
        val backups = storage.elements()

        for (group in backups) {
            val existBookmarks = existedGroup[group.name] ?: emptyMap()
            if (existBookmarks.isEmpty()) {
                recoveryGroup(project, group)
                continue
            }

            val notExist = group.bookmarks.filterKeys {
                (existBookmarks as MutableMap).remove(it)
                it !in existBookmarks
            }

            // recover
            if (notExist.isNotEmpty()) {
                notExist.values.forEach {
                    myBookmarkManager.addBookmark(project, group.name, it)
                }
            }
        }

        // 多的
        existedGroup.forEach { (groupName, group) ->
            for (bookmark in group.values) {
                storage.addBookmark(groupName, bookmark)
            }
        }
        for (invalid in myBookmarkManager.removeInvalid(project)) {
            val groups = bookmarkManager.getGroups(invalid)
            storage.removeBookmark(groups[0].name, invalid.key())
        }
        storage.storage()
    }

    /**
     * 恢复整个组
     */
    private fun recoveryGroup(project: Project, group: GroupElement) {
        myBookmarkManager.addGroup(project, Element.withGroup(group.name))
        for (bookmarkEle in group.bookmarks.values) {
            myBookmarkManager.addBookmark(project, group.name, bookmarkEle)
        }
    }
}