package io.github.hhy.bookmark.manager

import com.intellij.ide.bookmark.Bookmark
import com.intellij.ide.bookmark.BookmarkGroup
import com.intellij.openapi.project.Project
import io.github.hhy.bookmark.element.BookmarkElement
import io.github.hhy.bookmark.element.GroupElement

interface MyBookmarkManager {

    /**
     * get all
     *
     * @return
     */
    fun getAllBookmarks(project: Project): Map<String /* groupName */, Map<String /* bookmarkElementKey */, BookmarkElement>>

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
    fun addBookmark(project: Project, groupName :String, ele: BookmarkElement)

    /**
     * 删除无效的书签
     *
     * @param project
     */
    fun removeInvalid(project: Project): List<Bookmark> {
        return emptyList()
    }

    companion object {
        val bookmarkManager: MyBookmarkManager = HighVersionBookManager()
    }
}
