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
import java.util.function.Function
import java.util.stream.Collectors


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

    override fun projectClosing(project: Project) {
        Storage.removeStoreCache(project)
    }

    @Throws(IOException::class)
    fun load(project: Project?, storage: Storage) {
        val myBookmarkManager = MyBookmarkManager.getBookmarkManager()
        val noMatched: MutableList<Element?> = ArrayList()
        val grouped = myBookmarkManager.getAllBookmarks(project).stream()
                .collect(Collectors.toMap(Function { obj: Element -> obj.groupByKey() }, Function.identity()))
        for (element in storage.elements) {
            val existEle = grouped[element.groupByKey()]
            if (existEle != null) {
                grouped.remove(element.groupByKey())
            } else noMatched.add(element)
        }
        if (CollectionUtils.isNotEmpty(noMatched)) {
            // recovery
            myBookmarkManager.addBookmarks(project, noMatched)
        }
        val invalids = myBookmarkManager.removeInvalid(project)
        if (grouped.isNotEmpty()) {
            storage.addElements(ArrayList(grouped.values))
        }
        for (invalid in invalids) {
            storage.removeElement(invalid)
        }
        storage.storage()
    }
}