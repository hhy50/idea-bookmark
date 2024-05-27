package io.github.hhy.bookmark.storage

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import io.github.hhy.bookmark.element.Element
import io.github.hhy.bookmark.element.ElementType
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
        val DEFAULT_FILE = ".idea${File.pathSeparator}bookmarks.json"
    }

    val storeFile: Path = project.basePath?.let { Path.of(it, DEFAULT_FILE) }!!
    val GSON = GsonBuilder().setPrettyPrinting().create()
    val elements: MutableList<Element> = arrayListOf()

    init {
        this.checkFile()
        this.elements.addAll(readLocal())
    }

    override fun elements(): List<Element> = elements

    override fun findElement(withGroup: Element): Element? {
        TODO("Not yet implemented")
    }

    override fun addElement(ele: Element) {
        removeElement(ele).let {
            elements.add(ele)
        }
    }

    override fun removeElement(ele: Element): Element? {
        return findElement(ele)?.also {
            elements.remove(it)
        }
    }

    override fun storage() {
        if (elements.size == 0) {
            Files.writeString(storeFile, "[]")
            return
        }
        FileUtil.writeToFile(
            storeFile.toFile(),
            GSON.toJson(elements.map {
                if (it.elementType == ElementType.BOOKMARK) {
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

        val elements: List<Element> = GSON.fromJson(fileStr, object : TypeToken<List<Element>>() {})
            ?: return emptyList<Element>()
        return elements.map { item ->
            if (item.elementType == ElementType.BOOKMARK) {
                item.fileDescriptor = FDUtil.toAbsolute(item.fileDescriptor, project.basePath)
            }
            item
        }
    }
}
