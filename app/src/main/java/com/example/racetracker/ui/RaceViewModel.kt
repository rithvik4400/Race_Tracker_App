package com.example.racetracker.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RaceViewModel : ViewModel() {
    var playerOne by mutableStateOf(RaceParticipant(name = "You", progressIncrement = 2))
        private set
    var playerTwo by mutableStateOf(RaceParticipant(name = "Rival", progressIncrement = 2))
        private set

    var winner by mutableStateOf<RaceParticipant?>(null)
        private set

    var isRaceStarted by mutableStateOf(false)
        private set

    var countdownValue by mutableIntStateOf(-1) // -1 means no countdown
        private set

    private var opponentJob: Job? = null
    
    // Speed boost logic
    private var lastTapTime = 0L
    var speedBoostMultiplier by mutableStateOf(1f)
        private set

    fun selectPlayer(name: String) {
        playerOne = RaceParticipant(name = name, progressIncrement = 1)
        playerTwo = RaceParticipant(
            name = if (name == "Player 1") "Player 2" else "Player 1", 
            progressIncrement = 1
        )
    }

    fun startCountdown() {
        if (isRaceStarted || countdownValue != -1) return
        viewModelScope.launch {
            countdownValue = 3
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            countdownValue = 0 // "GO!"
            delay(500)
            countdownValue = -1
            startRace()
        }
    }

    private fun startRace() {
        isRaceStarted = true
        winner = null
        opponentJob = viewModelScope.launch {
            while (playerTwo.currentProgress < 100 && winner == null) {
                delay(400) // Opponent base speed
                if (isRaceStarted && winner == null) {
                    playerTwo.advance()
                    checkWinner()
                }
            }
        }
        
        // Decay speed boost over time
        viewModelScope.launch {
            while (isRaceStarted) {
                delay(100)
                if (speedBoostMultiplier > 1f) {
                    speedBoostMultiplier -= 0.05f
                } else {
                    speedBoostMultiplier = 1f
                }
            }
        }
    }

    fun playerTap() {
        if (isRaceStarted && winner == null) {
            val currentTime = System.currentTimeMillis()
            val tapInterval = currentTime - lastTapTime
            
            // If tapping fast (less than 200ms), increase multiplier
            if (tapInterval < 200) {
                speedBoostMultiplier = (speedBoostMultiplier + 0.2f).coerceAtMost(3.0f)
            }
            
            lastTapTime = currentTime
            
            // Advance based on base increment + boost
            val totalIncrement = (1 * speedBoostMultiplier).toInt().coerceAtLeast(1)
            repeat(totalIncrement) {
                playerOne.advance()
            }
            checkWinner()
        }
    }

    private fun checkWinner() {
        if (winner != null) return

        if (playerOne.currentProgress >= 100) {
            winner = playerOne
            playerOne.addCoins(100)
            isRaceStarted = false
            opponentJob?.cancel()
        } else if (playerTwo.currentProgress >= 100) {
            winner = playerTwo
            playerTwo.addCoins(100)
            isRaceStarted = false
            opponentJob?.cancel()
        }
    }

    fun resetRace() {
        opponentJob?.cancel()
        playerOne.reset()
        playerTwo.reset()
        winner = null
        isRaceStarted = false
        speedBoostMultiplier = 1f
        countdownValue = -1
    }
}
