package com.deeeelay.injectiontracker.domain

import java.time.Instant

data class Regimen(
    val medicineName: String,
    val dosage: String,
    val frequency: Frequency,
    val startDateTime: Instant,
    val nextInjectionAt: Instant,
)
