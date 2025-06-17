package io.github.hhy.bookmark

import com.intellij.ide.bookmark.BookmarksManager
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.BookmarkStarterActivity.Companion.LOG
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.element.key
import io.github.hhy.bookmark.manager.MyBookmarkManager
import io.github.hhy.bookmark.storage.Storage
import java.io.IOException

val myBookmarkManager = MyBookmarkManager.bookmarkManager


@Throws(IOException::class)
fun Project.reload() {
    val project = this
    val storage = Storage.getStorage(project)
    storage.reload()

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
        if (!groups.isEmpty()) {
            storage.removeBookmark(groups[0].name, invalid.key())
        }
        LOG.info("Remove invalid bookmark, $invalid")
    }
    storage.storage()
}
