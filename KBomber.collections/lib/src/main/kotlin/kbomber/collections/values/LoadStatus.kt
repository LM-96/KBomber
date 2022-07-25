package kbomber.collections.values

enum class LoadStatus {
    /**
     * Indicates a value that is **untouched** in the sense that
     * no write operations has been done with it
     */
    UNTOUCHED,

    /**
     * Indicates a value that has been **loaded** by applying a `loader`
     * or with other mechanism
     */
    LOADED,

    /**
     * Indicates a value that has previously been loaded, but now it has
     * been **unloaded**
     */
    UNLOADED,

    /**
     * Indicates a value that has not been loaded due to errors or that
     * has not been found (maybe because is naturally absent)
     */
    NOT_FOUND,

    /**
     * Indicates a value that has previously been loaded, and now it has
     * been **reloaded**
     */
    RELOADED
}