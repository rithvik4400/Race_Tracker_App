/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.racetracker.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * This class represents a state holder for race participant.
 */
class RaceParticipant(
    val name: String,
    val maxProgress: Int = 100,
    val progressDelayMillis: Long = 500L,
    private val progressIncrement: Int = 1,
    private val initialProgress: Int = 0
) {
    init {
        require(maxProgress > 0) { "maxProgress=$maxProgress; must be > 0" }
        require(progressIncrement > 0) { "progressIncrement=$progressIncrement; must be > 0" }
    }

    /**
     * Indicates the race participant's current progress
     */
    var currentProgress by mutableIntStateOf(initialProgress)
        private set

    /**
     * Store coins for the player
     */
    var coins by mutableIntStateOf(0)
        private set

    /**
     * Reward coins to the player
     */
    fun addCoins(amount: Int) {
        coins += amount
    }

    /**
     * Increment progress by the participant's increment value.
     */
    fun advance() {
        if (currentProgress < maxProgress) {
            currentProgress += progressIncrement
            if (currentProgress > maxProgress) {
                currentProgress = maxProgress
            }
        }
    }

    /**
     * Updates the value of [currentProgress] by value [progressIncrement] until it reaches
     * [maxProgress]. There is a delay of [progressDelayMillis] between each update.
     * [canContinue] is a callback to check if the race should still continue (e.g., no winner yet).
     */
    suspend fun run(canContinue: () -> Boolean = { true }) {
        while (currentProgress < maxProgress && canContinue()) {
            delay(progressDelayMillis)
            advance()
        }
    }

    /**
     * Regardless of the value of [initialProgress] the reset function will reset the
     * [currentProgress] to 0
     */
    fun reset() {
        currentProgress = 0
    }
}

/**
 * The Linear progress indicator expects progress value in the range of 0-1. This property
 * calculate the progress factor to satisfy the indicator requirements.
 */
val RaceParticipant.progressFactor: Float
    get() = currentProgress / maxProgress.toFloat()
