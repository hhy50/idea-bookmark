package io.github.hhy.bookmark.element

data class GroupElement(var name: String, val bookmarks: MutableList<BookmarkElement>) : Element() {

}