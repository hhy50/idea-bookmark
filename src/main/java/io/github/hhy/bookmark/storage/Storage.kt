package io.github.hhy.bookmark.storage

import com.intellij.openapi.extensions.DefaultPluginDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.impl.ProjectImpl
import io.github.hhy.bookmark.PluginInfo
import io.github.hhy.bookmark.element.Element

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
    fun elements(): List<Element>

    /**
     * 获取单个
     */
    fun findElement(withGroup: Element): Element?

    /**
     * 添加
     */
    fun addElement(ele: Element): Unit

    /**
     * 删除
     */
    fun removeElement(ele: Element): Element?

    /**
     * 存储
     */
    fun storage(): Unit
}

