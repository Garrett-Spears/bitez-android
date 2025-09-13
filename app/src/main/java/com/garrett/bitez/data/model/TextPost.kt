package com.garrett.bitez.data.model

// Data for a feed text post
data class TextPost(
    override val postId: String,
    override val userId: String,
    override val userName: String,
    private val postText: String,
) : Post() {
}
