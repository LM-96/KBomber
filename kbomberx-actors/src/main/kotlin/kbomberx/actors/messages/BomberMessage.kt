package kbomberx.actors.messages

import kbomberx.actors.BomberActorAddress
import java.time.LocalDateTime

data class BomberMessage(
    val id : String,
    val senderAddress : BomberActorAddress,
    val destinationAddress : BomberActorAddress,
    val type : BomberMessageType,
    val content : String,
    val creationTime : LocalDateTime = LocalDateTime.now()
)
