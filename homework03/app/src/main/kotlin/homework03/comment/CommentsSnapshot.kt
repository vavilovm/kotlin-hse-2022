package homework03.comment

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper


data class CommentsSnapshot(val comments: List<Comment>) {
    fun commentList(): List<Comment> {
        val result: MutableList<Comment> = arrayListOf()

        fun addAllDfs(comm: Comment) {
            result.add(comm)
            comm.replies.forEach(::addAllDfs)
        }

        comments.forEach(::addAllDfs)
        return result
    }

    companion object {
        fun create(objectMapper: ObjectMapper, json: String): CommentsSnapshot {
            val idGenerator = IdGenerator()
            return objectMapper.readTree(json).get(1).get("data").get("children").map { comment: JsonNode ->
                createCommentDfs(comment.get("data"), null, 0, idGenerator)
            }.toList().let {
                CommentsSnapshot(it)
            }
        }

        private class IdGenerator {
            private var id = 0UL
            fun next() = id++
        }

        private fun createCommentDfs(
            comment: JsonNode, parentId: ULong?, depth: Int, idGenerator: IdGenerator
        ): Comment {
            val id = idGenerator.next()
            val replies = (comment.get("replies")?.get("data")?.get("children") ?: emptyList()).map {
                createCommentDfs(it.get("data"), id, depth + 1, idGenerator)
            }.toList()

            return Comment(
                id = id,
                depth = depth,
                replies = replies,
                replyTo = parentId,
                createdTime = comment.get("created").asDouble(),
                likes = comment.get("ups").asLong(),
                dislikes = comment.get("downs").asLong(),
                text = comment.get("body").toPrettyString(),
                authorFullname = comment.get("author_fullname").toPrettyString()
            )
        }
    }
}