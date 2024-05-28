package io.github.hhy.bookmark.manager

import com.intellij.ide.bookmark.Bookmark
import com.intellij.ide.bookmark.BookmarkGroup
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.element.BookmarkElement
import io.github.hhy.bookmark.element.GroupElement

interface MyBookmarkManager {

    /**
     * get all
     *
     * @return
     */
    fun getAllBookmarks(project: Project): Map<String, Map<String, BookmarkElement>>

    /**
     * Add
     *
     * @param project
     * @param elements
     */
    fun addGroup(project: Project, ele: GroupElement): BookmarkGroup

    /**
     *
     */
    fun addBookmark(project: Project, ele: BookmarkElement): Bookmark

    /**
     * 删除无效的书签
     *
     * @param project
     */
    fun removeInvalid(project: Project): List<Bookmark> {
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
