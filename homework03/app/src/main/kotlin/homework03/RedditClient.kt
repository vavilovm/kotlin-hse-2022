package homework03

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.soywiz.korio.async.async
import homework03.comment.CommentsSnapshot
import homework03.topic.JsonTopicInfo
import homework03.topic.JsonTopics
import homework03.topic.TopicSnapshot
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers

class RedditClient {
    suspend fun getTopic(name: String): TopicSnapshot {
        val info = async(Dispatchers.Default) {
            httpClient.get("https://www.reddit.com/r/$name/about.json").body<String>().let {
                objectMapper.readValue(it, JsonTopicInfo::class.java)
            }
        }
        val topics = async(Dispatchers.Default) {
            httpClient.get("https://www.reddit.com/r/$name/.json").body<String>().let {
                objectMapper.readValue(it, JsonTopics::class.java)
            }
        }
        return TopicSnapshot.create(info.await(), topics.await())
    }

    suspend fun getComments(topicName: String, title: String): CommentsSnapshot =
        httpClient.get("https://www.reddit.com/r/$topicName/comments/$title/.json").body<String>().let {
            CommentsSnapshot.create(objectMapper, it)
        }

    private val httpClient = HttpClient(CIO)
    private val objectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}