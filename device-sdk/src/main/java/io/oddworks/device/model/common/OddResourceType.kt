package io.oddworks.device.model.common

enum class OddResourceType(val endpoint: String) {
    CONFIG("config"),
    COLLECTION("collections"),
    EVENT("events"),
    PROGRESS("videos/:id/progress"),
    PROMOTION("promotions"),
    SEARCH("search"),
    VIDEO("videos"),
    VIEW("views"),
    VIEWER("viewers"),
    WATCHLIST("viewers/:id/relationships/watchlist")
}

