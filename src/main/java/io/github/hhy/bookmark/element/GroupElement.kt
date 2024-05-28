package io.github.hhy.bookmark.element

data class GroupElement(var name: String, val bookmarks: MutableMap<String /* BookmarkElementKey */, BookmarkElement>) : Element() {

}