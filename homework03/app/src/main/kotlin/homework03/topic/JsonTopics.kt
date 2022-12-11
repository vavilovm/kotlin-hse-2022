package homework03.topic

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonTopics(@JsonProperty("data") val data: JsonTopicList) {
    data class JsonTopicList(@JsonProperty("children") val children: List<JsonTopicWrapper>) {
        data class JsonTopicWrapper(@JsonProperty("data") val topic: Topic) {
            data class Topic(
                @JsonProperty("author_fullname") val authorFullname: String,
                @JsonProperty("created") val createdTime: Double,
                @JsonProperty("ups") val ups: Long,
                @JsonProperty("downs") val downs: Long,
                @JsonProperty("title") val title: String,
                @JsonProperty("selftext") val selfText: String?,
                @JsonProperty("selftext_html") val selfTextHtml: String?
            )
        }
    }
}