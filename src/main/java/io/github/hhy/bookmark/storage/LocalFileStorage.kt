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
    val elements: MutableList<Element> = arrayListOf()

    init {
        this.checkFile()
        this.elements.addAll(readLocal())
    }

    override fun elements(): List<Element> = elements

    override fun findElement(element: Element): Element? {
        val equalFunc: (Element) -> Boolean = fun(target: Element): Boolean {
            if (target.elementType != element.elementType) {
                return false
            }

            return if (target is GroupElement && element is GroupElement) {
                target.name == element.name
            } else if (target is BookmarkElement && element is BookmarkElement) {
                FDUtil.toRelative(target.fileDescriptor, project.basePath) == FDUtil.toRelative(
                    element.fileDescriptor,
                    project.basePath
                )
                        && target.linenumber == element.linenumber
            } else {
                false
            }
        }
        return elements.find { it ->
            equalFunc(it)
        }
    }

    override fun addElement(ele: Element) {
        removeElement(ele).let {
            elements.add(ele)
        }
    }

    override fun removeElement(condition: (Element) -> Boolean) {
        this.elements.removeIf(condition)
    }

    override fun storage() {
        if (elements.size == 0) {
            Files.writeString(storeFile, "[]")
            return
        }
        FileUtil.writeToFile(
            storeFile.toFile(),
            GSON.toJson(elements.map {
                if (it is BookmarkElement) {
                    it.fileDescriptor = FDUtil.toRelative(it.fileDescriptor, project.basePath)
                }
                it
            }),
            StandardCharsets.UTF_8
        )
    }

    private fun checkFile() {
        val file = storeFile.toFile()
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            Files.writeString(storeFile, "[]");
        }
    }

    @Throws(IOException::class)
    private fun readLocal(): List<Element> {
        val fileStr = Files.readString(storeFile)
        if (StringUtils.isEmpty(fileStr)) emptyList<Element>()

        val elements: List<Map<String, String>> =
            GSON.fromJson(fileStr, object : TypeToken<List<Map<String, String>>>() {})
                ?: return emptyList()

        return elements.map { item ->
            if (item["elementType"] == "BOOKMARK") {
                Element.withBookmark(
                    item["fileDescriptor"]?.let { FDUtil.toAbsolute(it, project.basePath) } ?: "",
                    item["linenumber"]?.toInt() ?: -1,
                    item["name"] ?: "",
                    item["group"] ?: "",
                    item["bookmarkType"] ?: "",
                )
            } else {
                Element.withGroup(item["name"] ?: "")
            }
        }
    }
}
