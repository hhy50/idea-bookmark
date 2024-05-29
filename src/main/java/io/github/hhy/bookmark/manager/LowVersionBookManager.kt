package io.github.hhy.bookmark.manager

import com.intellij.ide.bookmark.BookmarkGroup
import com.intellij.ide.bookmarks.BookmarkManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import io.github.hhy.bookmark.element.*
import java.nio.file.Path


class LowVersionBookManager : MyBookmarkManager {

    override fun getAllBookmarks(project: Project): Map<String, Map<String, BookmarkElement>> {
        val bookmarkManager = project.getService(BookmarkManager::class.java)
        val bookmarks: Map<String, BookmarkElement> = bookmarkManager.allBookmarks.associate {
            it.key() to (Element.withBookmark(
                it.fileDescriptor(), it.linenumber(), it.description, it.type.toString(),
            ))
        }
        return mapOf(project.name to bookmarks)
    }

    override fun addGroup(project: Project, ele: GroupElement): BookmarkGroup {
        TODO("Not yet implemented")
    }

    override fun addBookmark(project: Project, groupName: String, ele: BookmarkElement) {
        val bookmarkManager = project.getService(BookmarkManager::class.java)
        val virtualFileManager = ApplicationManager.getApplication().getService(VirtualFileManager::class.java)

        val virtualFile = virtualFileManager.findFileByNioPath(Path.of(ele.fileDescriptor))
            ?: throw RuntimeException("not found file ${ele.fileDescriptor}")
        bookmarkManager.addTextBookmark(virtualFile, ele.linenumber, ele.name)
    }
}