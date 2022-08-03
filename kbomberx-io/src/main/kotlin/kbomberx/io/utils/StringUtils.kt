package kbomberx.io.utils

/**
 * Adds some tabs to the left of this string in order to realize a
 * sort of *level* for strings
 * @throws IllegalArgumentException if the level is less than `1`
 * @param level the desired level (must be greater than `0`)
 * @return the new string with the applied level
 *
 */
fun String.addLevelTab(level : Int) : String {
    if(level < 1)
        throw IllegalArgumentException("Level cannot be less then 1")

    var tabString = ""
    for(i in 1..level) {
        tabString += "\t"
    }

    return this.lines().joinToString("\n") { tabString + it }
}