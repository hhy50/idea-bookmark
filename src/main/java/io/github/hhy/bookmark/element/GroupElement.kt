package io.github.hhy.bookmark.element

data class GroupElement(var name: String, val bookmarks: MutableMap<String, BookmarkElement>) : Element() {

    fun getBookmark(key: String): BookmarkElement? = this.bookmarks[key]

    fun addBookmark(bookmark: BookmarkElement) {
        this.bookmarks[bookmark.key()] = bookmark
    }

    /**
     * 删除书签
     */
    fun removeBookmark(key: String): BookmarkElement? = this.bookmarks.remove(key)
}