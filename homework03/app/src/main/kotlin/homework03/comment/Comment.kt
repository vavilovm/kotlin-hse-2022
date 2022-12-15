package homework03.comment

data class Comment(
    val createdTime: Double,
    val likes: Long,
    val dislikes: Long,
    val text: String,
    val authorFullname: String,
    val replyTo: ULong?,
    val replies: List<Comment>,
    val depth: Int,
    val id: ULong
)