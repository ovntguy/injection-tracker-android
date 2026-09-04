package com.deeeelay.injectiontracker.domain

enum class Frequency(val intervalDays: Long) {
    EVERY_WEEK(7),
    EVERY_OTHER_WEEK(14),
}
