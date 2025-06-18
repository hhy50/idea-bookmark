package io.github.hhy.bookmark

import com.intellij.ide.bookmark.BookmarksListener
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import io.github.hhy.bookmark.notify.Notify


class BookmarkStarterActivity : ProjectActivity, ProjectManagerListener {
    companion object {
        val LOG: Logger = Logger.getInstance(BookmarkStarterActivity::class.java)
    }

    override suspend fun execute(project: Project) {
        try {
            // compare and recovery
            project.reload()

            // start listener
            val connect = project.messageBus.connect()
            connect.subscribe(BookmarksListener.TOPIC, BookmarkListener(project))
        } catch (e: Exception) {
            e.printStackTrace()
            Notify.error(e.message)
        }
    }
}