package kbomberx.actors.model

data class Transition(
    val id : String,
    val source : State,
    val destination : State
)
