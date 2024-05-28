package io.github.hhy.bookmark

import com.intellij.ide.bookmark.BookmarksListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.manager.MyBookmarkManager
import io.github.hhy.bookmark.notify.Notify
import io.github.hhy.bookmark.storage.Storage
import org.apache.commons.collections.CollectionUtils
import java.io.IOException


class BookmarkStarterActivity : ProjectActivity, ProjectManagerListener {
    override suspend fun execute(project: Project) {
        val storage = Storage.getStorage(project)
        try {
            // compare and recovery
            load(project, storage)

            // start listener
            val connect = project.messageBus.connect()
            connect.subscribe(BookmarksListener.TOPIC, BookmarkListener(project))
        } catch (e: Exception) {
            e.printStackTrace()
            Notify.error(e.message)
        }
    }


    @Throws(IOException::class)
    fun load(project: Project, storage: Storage) {
        val myBookmarkManager = MyBookmarkManager.bookmarkManager
        val noMatched: MutableList<Element> = ArrayList()
        val grouped: MutableMap<String, Element> = myBookmarkManager
            .getAllBookmarks(project).associateBy { it.groupByKey() } as MutableMap<String, Element>

        for (element in storage.elements()) {
            if (element.groupByKey() in grouped) {
                grouped.remove(element.groupByKey())
            } else {
                noMatched.add(element)
            }
        }
        if (CollectionUtils.isNotEmpty(noMatched)) {
            // recovery
            myBookmarkManager.addBookmarks(project, noMatched)
        }
        if (grouped.isNotEmpty()) {
            grouped.values.forEach {
                storage.addElement(it)
            }
        }
        for (invalid in myBookmarkManager.removeInvalid(project)) {
            storage.removeElement {
                false
            }
        }
        storage.storage()
    }
}