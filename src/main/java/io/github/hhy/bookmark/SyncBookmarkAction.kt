package io.github.hhy.bookmark

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import io.github.hhy.bookmark.notify.Notify

class SyncBookmarkAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        try {
            event.project?.reload()
        } catch (e: Exception) {
            e.printStackTrace()
            Notify.error(e.message)
        }
    }
}