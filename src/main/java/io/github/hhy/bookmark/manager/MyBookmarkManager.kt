package io.github.hhy.bookmark.manager

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.element.Element

interface MyBookmarkManager {
    /**
     * get all
     *
     * @return
     */
    fun getAllBookmarks(project: Project): List<Element>

    /**
     * Add
     *
     * @param project
     * @param elements
     */
    fun addBookmarks(project: Project, elements: List<Element>)

    /**
     * 删除无效的书签
     *
     * @param project
     */
    fun removeInvalid(project: Project): List<Element> {
        return emptyList()
    }

    companion object {
        val bookmarkManager: MyBookmarkManager
            get() {
                val build = ApplicationInfo.getInstance().build
                return if (build.baselineVersion >= 231) {
                    HighVersionBookManager()
                } else LowVersionBookManager()
            }
    }
}
