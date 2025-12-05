package com.example.clockscreensaver.ui.clock

enum class ClockStyle(val id: String) {
    BASIC("basic"),
    SPLIT("split"),
    MINIMAL("minimal");

    companion object {
        fun fromId(id: String?): ClockStyle =
            values().firstOrNull { it.id == id } ?: BASIC
    }
}
