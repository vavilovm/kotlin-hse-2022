package homework03.topic

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonTopicInfo(@JsonProperty("data") val data: TopicInfoData) {
    data class TopicInfoData(
        @JsonProperty("created") val creationTime: Double,
        @JsonProperty("active_user_count") val subscribersOnline: Long,
        @JsonProperty("ranking_size") val rankingSize: String?,
        @JsonProperty("public_description") val description: String,
    )
}