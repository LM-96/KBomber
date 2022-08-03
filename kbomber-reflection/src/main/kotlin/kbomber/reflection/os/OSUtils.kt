package kbomber.reflection.os

/**
 * Analyzes and returns the current operating system or `null`
 * if it is unknown
 * @return the current operating system or `null`
 * if it is unknown
 */
fun getCurrentOperatingSystem() : OperatingSystem? {
    val os = System.getProperty("os.name").lowercase()
    return when {

        os.contains("win") -> {
            OperatingSystem.WINDOWS
        }

        os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
            OperatingSystem.LINUX
        }

        os.contains("mac") -> {
            OperatingSystem.MAC
        }

        os.contains("sunOperativeSystem") -> {
            OperatingSystem.SOLARIS
        }
        else -> null
    }
}