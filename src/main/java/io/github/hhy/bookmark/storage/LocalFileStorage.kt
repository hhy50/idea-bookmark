package io.github.hhy.bookmark.storage

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFileManager
import io.github.hhy.bookmark.element.*
import io.github.hhy.bookmark.util.FDUtil
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
    val groups: MutableMap<String, GroupElement> = HashMap()

    init {
        checkFile()
    }

    @Synchronized
    override fun elements(): List<GroupElement> = ArrayList(groups.values)

    @Synchronized
    override fun getGroup(name: String): GroupElement? = this.groups[name]

    @Synchronized
    override fun addGroup(ele: GroupElement) {
        if (ele.name !in this.groups) {
            this.groups[ele.name] = ele
        }
    }

    @Synchronized
    override fun removeGroup(name: String): GroupElement? = this.groups.remove(name)

    @Synchronized
    override fun renameGroup(groupName: String, newGroupName: String) {
        this.groups[newGroupName] = this.groups[groupName]!!
        removeGroup(groupName)
    }

    @Synchronized
    override fun getBookmark(key: String): BookmarkElement? {
        return this.groups.values.map { it.bookmarks }
            .flatten().firstOrNull { it.key() == key }
    }

    @Synchronized
    override fun addBookmark(groupName: String, ele: BookmarkElement) {
        val group: GroupElement = getGroup(groupName) ?: Element.withGroup(groupName).also { addGroup(it) }
        val bookmark = getBookmark(ele.key())
        if (bookmark == null) {
            group.bookmarks.add(0, ele)
        }
    }

    @Synchronized
    override fun removeBookmark(groupName: String, key: String): BookmarkElement? {
        val group: GroupElement = getGroup(groupName) ?: Element.withGroup(groupName).also { addGroup(it) }
        return getBookmark(key)?.also { group.bookmarks.remove(it) }
    }

    @Synchronized
    override fun storage() {
        if (groups.isEmpty()) {
            Files.writeString(storeFile, "{}")
            return
        }
        val bookmarks: Map<String, List<BookmarkElement>> = groups.mapValues { (_, group) ->
            group.bookmarks.map {
                val iit = it.clone()
                iit.fileDescriptor = FDUtil.toRelative(iit.fileDescriptor, project.basePath)
                iit
            }
        }
        FileUtil.writeToFile(
            storeFile.toFile(),
            GSON.toJson(bookmarks),
            StandardCharsets.UTF_8
        )
        TransactionGuard.getInstance().submitTransactionAndWait {
            val docManager = FileDocumentManager.getInstance()
            VirtualFileManager.getInstance().findFileByNioPath(storeFile)?.let {
                docManager.getDocument(it)
            }?.let {
                docManager.reloadFromDisk(it)
            }
        }
    }

    override fun reload() {
        TransactionGuard.getInstance().submitTransactionAndWait {
            val docManager = FileDocumentManager.getInstance()
            VirtualFileManager.getInstance().findFileByNioPath(storeFile)?.let {
                docManager.getDocument(it)
            }?.let {
                docManager.saveDocument(it)
            }
        }

        this.groups.clear()
        this.groups.putAll(readLocal())
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
        if (fileStr.isEmpty()) emptyList<Element>()

        val backups: Map<String, List<BookmarkElement>> = if (fileStr.startsWith("[")) {
            GSON.fromJson(fileStr, object : TypeToken<ElementList>() {})
                ?.filter { it.elementType == ElementType.BOOKMARK }
                ?.groupBy(ElementList.Item::group)?.mapValues {
                    it.value.map(ElementList.Item::toElement)
                }
                ?: return emptyMap()
        } else {
            GSON.fromJson(fileStr, object : TypeToken<Map<String, List<BookmarkElement>>>() {})
                ?: return emptyMap()
        }
        return backups.mapValues { (groupName, bookmarks) ->
            bookmarks.forEach {
                it.fileDescriptor = FDUtil.toAbsolute(it.fileDescriptor, project.basePath)
            }
            Element.withGroup(groupName, bookmarks as MutableList)
        }
    }
}


