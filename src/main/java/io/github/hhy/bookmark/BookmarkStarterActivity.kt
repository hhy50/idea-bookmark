package io.github.hhy.bookmark

import com.intellij.ide.bookmark.BookmarksListener
import com.intellij.ide.bookmark.BookmarksManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.element.key
import io.github.hhy.bookmark.manager.MyBookmarkManager
import io.github.hhy.bookmark.notify.Notify
import io.github.hhy.bookmark.storage.Storage
import java.io.IOException


class BookmarkStarterActivity : ProjectActivity, ProjectManagerListener {
    companion object {
        val LOG: Logger = Logger.getInstance(BookmarkStarterActivity::class.java)
    }

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

        LOG.info("Existed bookmarks, ${existedGroup.values}")
        LOG.info("Backups bookmarks from bookmarks.json, $backups")

        for (group in backups) {
            val existBookmarks = if (group.name in existedGroup) {
                existedGroup[group.name] as MutableMap
            } else {
                myBookmarkManager.addGroup(project, Element.withGroup(group.name))
                LOG.info("Synchronize group '${group.name}' to IntelliJ IDEA")
                mutableMapOf()
            }

            val notExist = group.bookmarks.associateBy { it.key() }.filterKeys {
                existBookmarks.remove(it) == null
            }

            // recover
            if (notExist.isNotEmpty()) {
                notExist.values.forEach {
                    LOG.info("Synchronize differential bookmark to IntelliJ IDEA, $it")
                    myBookmarkManager.addBookmark(project, group.name, it)
                }
            }
        }

        // 同步到文件
        existedGroup.forEach { (groupName, group) ->
            for (bookmark in group.values) {
                LOG.info("Synchronize differential bookmark to bookmarks.json, $bookmark")
                storage.addBookmark(groupName, bookmark)
            }
        }
        for (invalid in myBookmarkManager.removeInvalid(project)) {
            val groups = bookmarkManager.getGroups(invalid)
            storage.removeBookmark(groups[0].name, invalid.key())
            LOG.info("Remove invalid bookmark, $invalid")
        }
        storage.storage()
    }
}