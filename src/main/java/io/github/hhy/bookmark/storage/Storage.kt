package io.github.hhy.bookmark.storage

import com.intellij.openapi.extensions.DefaultPluginDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.impl.ProjectImpl
import io.github.hhy.bookmark.PluginInfo
import io.github.hhy.bookmark.element.BookmarkElement
import io.github.hhy.bookmark.element.GroupElement

sealed interface Storage {

    companion object {
        @Synchronized
        fun getStorage(project: Project): Storage {
            val storageService: Storage = project.getService(Storage::class.java)
                ?: return LocalFileStorage(project).also {
                    (project as ProjectImpl).registerServiceInstance(
                        Storage::class.java,
                        it,
                        DefaultPluginDescriptor(PluginInfo.ID)
                    )
                }
            return storageService
        }
    }

    /**
     * 获取存储的全部书签
     */
    fun elements(): List<GroupElement>

    /**
     * 获取某个组
     */
    fun getGroup(name: String): GroupElement?

    /**
     * 添加组
     */
    fun addGroup(ele: GroupElement)

    /**
     * 删除某个组
     */
    fun removeGroup(name: String): GroupElement?

    /**
     * 重命名组
     */
    fun renameGroup(groupName: String, newGroupName: String)

    /**
     * 获取单个书签
     */
    fun getBookmark(key: String): BookmarkElement?

    /**
     * 添加书签
     */
    fun addBookmark(groupName: String, ele: BookmarkElement)

    /**
     * 删除书签
     */
    fun removeBookmark(groupName: String, key: String): BookmarkElement?

    /**
     * 存储
     */
    fun storage(): Unit
}

