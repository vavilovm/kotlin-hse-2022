package homework03.topic

import homework03.topic.JsonTopics.JsonTopicList.JsonTopicWrapper.Topic

data class TopicSnapshot(
    val creationTime: Double,
    val subscribersOnline: Long,
    val rankingSize: String?,
    val description: String,
    val topics: List<Topic>
) {
    companion object {
        fun create(topicInfo: JsonTopicInfo, topics: JsonTopics) = with(topicInfo.data) {
            TopicSnapshot(
                creationTime = creationTime,
                subscribersOnline = subscribersOnline,
                rankingSize = rankingSize,
                description = description,
                topics = topics.data.children.map { it.topic }
            )
        }
    }
}