package io.github.hhy.bookmark.storage

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import io.github.hhy.bookmark.element.BookmarkElement
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.element.GroupElement
import io.github.hhy.bookmark.util.FDUtil
import org.apache.commons.lang.StringUtils
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class LocalFileStorage(private val project: Project) : Storage {

    companion object {
        @JvmField
        val DEFAULT_FILE = ".idea${File.separator}bookmarks.json"
    }

    val storeFile: Path = project.basePath?.let { Path.of(it, DEFAULT_FILE) }!!
    val GSON = GsonBuilder().setPrettyPrinting().create()
    val elements: MutableMap<String, GroupElement>

    init {
        this.checkFile()
        this.elements = HashMap(readLocal())
    }

    @Synchronized
    override fun elements(): List<GroupElement> = ArrayList(elements.values)

    @Synchronized
    override fun getGroup(name: String): GroupElement? = this.elements[name]

    @Synchronized
    override fun addGroup(ele: GroupElement) {
        if (ele.name !in this.elements) {
            this.elements[ele.name] = ele
        }
    }

    @Synchronized
    override fun removeGroup(name: String): GroupElement? = this.elements.remove(name)

    @Synchronized
    override fun renameGroup(groupName: String, newGroupName: String) {
        this.elements[newGroupName] = this.elements[groupName]!!
        this.elements[newGroupName]!!.bookmarks.forEach { (key, bookmarkEle) ->
            bookmarkEle.group = newGroupName
        }
        removeGroup(groupName)
    }

    @Synchronized
    override fun getBookmark(key: String): BookmarkElement? {
        for ((_, groupEle) in this.elements) {
            groupEle.bookmarks[key]?.also {
                return@getBookmark it
            }
        }
        return null
    }

    @Synchronized
    override fun addBookmark(ele: BookmarkElement) {
        val group: GroupElement = getGroup(ele.group) ?: Element.withGroup(ele.group).also { addGroup(it) }
        if (ele.key() !in group.bookmarks) {
            group.bookmarks[ele.key()] = ele
        }
    }

    @Synchronized
    override fun removeBookmark(key: String): BookmarkElement? {
        return getBookmark(key)?.let {
            this.elements[it.group]?.bookmarks?.remove(key)
        }
    }

    @Synchronized
    override fun storage() {
        if (elements.isEmpty()) {
            Files.writeString(storeFile, "{}")
            return
        }
        val bookmarks: Map<String, List<BookmarkElement>> = elements.mapValues { (groupName, groupElement) ->
            groupElement.bookmarks.values.map {
                it.fileDescriptor = FDUtil.toRelative(it.fileDescriptor, project.basePath)
                it
            }
        }
        FileUtil.writeToFile(
            storeFile.toFile(),
            GSON.toJson(bookmarks),
            StandardCharsets.UTF_8
        )
    }

    private fun checkFile() {
        val file = storeFile.toFile()
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            Files.writeString(storeFile, "{}");
        }
    }

    @Throws(IOException::class)
    private fun readLocal(): Map<String, GroupElement> {
        val fileStr = Files.readString(storeFile)
        if (StringUtils.isEmpty(fileStr)) emptyList<Element>()

        val backups = GSON.fromJson(fileStr, object : TypeToken<Map<String, List<BookmarkElement>>>() {})
            ?: return emptyMap()
        return backups.mapValues { (groupName, bookmarks) ->
            bookmarks.forEach {
                it.fileDescriptor = FDUtil.toAbsolute(it.fileDescriptor, project.basePath)
            }
            Element.withGroup(groupName, bookmarks)
        }
    }
}


