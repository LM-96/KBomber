package kbomberx.actors.messages

import java.time.LocalDateTime

data class SentMessage(
    val message : BomberMessage,
    val sentTime : LocalDateTime
)