package io.github.hhy.bookmark

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import io.github.hhy.bookmark.notify.Notify
import io.github.hhy.bookmark.storage.Storage


class BookmarkStarterActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val storage = Storage.getStorage(project)
        try {
            // compare and recovery
            BookmarkProject.load(project, storage)
        } catch (e: Exception) {
            e.printStackTrace()
            Notify.error(e.message)
        }
    }
}