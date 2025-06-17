package io.github.hhy.bookmark.element

import com.intellij.ide.bookmark.Bookmark
import com.intellij.ide.bookmark.providers.FileBookmarkImpl
import com.intellij.ide.bookmark.providers.LineBookmarkImpl
import io.github.hhy.bookmark.util.FDUtil

fun Bookmark.key(): String = "${this.fileDescriptor()}${Element.SEPARATOR}${this.linenumber()}"

fun Bookmark.fileDescriptor(): String {
    return when (this) {
        is LineBookmarkImpl -> FDUtil.formatSeparator(this.descriptor.file.path)
        is FileBookmarkImpl -> FDUtil.formatSeparator(this.descriptor.file.path)
        else -> throw RuntimeException()
    }
}

fun Bookmark.linenumber(): Int {
    return when (this) {
        is LineBookmarkImpl -> this.line
        is FileBookmarkImpl -> -1
        else -> throw RuntimeException()
    }
}

fun Bookmark.invalid() = this::class.java.name.contains("InvalidBookmark")

fun com.intellij.ide.bookmarks.Bookmark.key(): String =
    "${this.fileDescriptor()}${Element.SEPARATOR}${this.linenumber()}"

fun com.intellij.ide.bookmarks.Bookmark.fileDescriptor(): String =
    FDUtil.formatSeparator(this.file.path)

fun com.intellij.ide.bookmarks.Bookmark.linenumber() = this.line

