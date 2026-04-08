package com.example.racetracker.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.racetracker.R

// Custom Colors
val GameOrange = Color(0xFFFF9800)
val GameYellow = Color(0xFFFFEB3B)

enum class RaceScreenRoute {
    Dashboard,
    PlayerSelection,
    Race
}

@Composable
fun RaceTrackerApp(
    raceViewModel: RaceViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RaceScreenRoute.Dashboard.name
    ) {
        composable(route = RaceScreenRoute.Dashboard.name) {
            DashboardScreen(
                onStartGame = { navController.navigate(RaceScreenRoute.PlayerSelection.name) }
            )
        }
        composable(route = RaceScreenRoute.PlayerSelection.name) {
            PlayerSelectionScreen(
                onPlayerSelected = { name ->
                    raceViewModel.selectPlayer(name)
                    navController.navigate(RaceScreenRoute.Race.name)
                }
            )
        }
        composable(route = RaceScreenRoute.Race.name) {
            RaceScreen(
                raceViewModel = raceViewModel,
                onReset = {
                    raceViewModel.resetRace()
                    navController.popBackStack(RaceScreenRoute.Dashboard.name, inclusive = false)
                }
            )
        }
    }
}

@Composable
fun DashboardScreen(onStartGame: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RACE\nTRACKER",
                style = TextStyle(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = GameYellow,
                    shadow = Shadow(color = Color.Black, blurRadius = 8f)
                ),
                textAlign = TextAlign.Center,
                lineHeight = 60.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(containerColor = GameOrange),
                modifier = Modifier
                    .height(64.dp)
                    .width(220.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("START GAME", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PlayerSelectionScreen(onPlayerSelected: (String) -> Unit) {
    var selectedPlayer by remember { mutableStateOf("Player 1") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF212121))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CHOOSE YOUR RUNNER",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerCard("Player 1", selectedPlayer == "Player 1") { selectedPlayer = "Player 1" }
                PlayerCard("Player 2", selectedPlayer == "Player 2") { selectedPlayer = "Player 2" }
            }

            Spacer(modifier = Modifier.height(64.dp))
            Button(
                onClick = { onPlayerSelected(selectedPlayer) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GameOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("START RACE", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun PlayerCard(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(140.dp),
        color = if (isSelected) GameOrange else Color.Gray.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(4.dp, Color.White) else null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_walk),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = if (isSelected) Color.White else Color.LightGray
            )
            Text(name, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RaceScreen(
    raceViewModel: RaceViewModel,
    onReset: () -> Unit
) {
    val playerOne = raceViewModel.playerOne
    val playerTwo = raceViewModel.playerTwo
    val winner = raceViewModel.winner
    val isRaceStarted = raceViewModel.isRaceStarted
    val countdown = raceViewModel.countdownValue
    val speedBoost = raceViewModel.speedBoostMultiplier

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Parallax Background
        ParallaxBackground(isRaceStarted)

        // 2. Track & Runners
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            TrackRow(playerOne, color = Color.Cyan)
            Spacer(modifier = Modifier.height(60.dp))
            TrackRow(playerTwo, color = Color.Magenta)
        }

        // 3. UI Layer
        UIOverlay(
            playerOne = playerOne,
            playerTwo = playerTwo,
            winner = winner,
            isRaceStarted = isRaceStarted,
            countdown = countdown,
            speedBoost = speedBoost,
            onTap = { raceViewModel.playerTap() },
            onStart = { raceViewModel.startCountdown() },
            onReset = onReset
        )
    }
}

@Composable
fun ParallaxBackground(isRunning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "parallax")
    val xOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRunning) 2000 else 1000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF87CEEB))) {
        Box(
            modifier = Modifier
                .offset(x = 50.dp, y = 50.dp)
                .size(80.dp)
                .background(Color.Yellow, CircleShape)
        )
    }
}

@Composable
fun TrackRow(participant: RaceParticipant, color: Color) {
    val progress = participant.progressFactor
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "runner_move"
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.5f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            RunnerAnimation(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(animatedProgress.coerceIn(0.01f, 1f)),
                name = participant.name,
                color = color
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.5f))
        )
    }
}

@Composable
fun RunnerAnimation(modifier: Modifier, name: String, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceY"
    )

    Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 4f))
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_walk),
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(60.dp)
                    .graphicsLayer(translationY = bounceY)
            )
        }
    }
}

@Composable
fun UIOverlay(
    playerOne: RaceParticipant,
    playerTwo: RaceParticipant,
    winner: RaceParticipant?,
    isRaceStarted: Boolean,
    countdown: Int,
    speedBoost: Float,
    onTap: () -> Unit,
    onStart: () -> Unit,
    onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CoinDisplay(playerOne.coins)
            if (speedBoost > 1.2f) {
                Text(
                    "BOOST: x${"%.1f".format(speedBoost)}",
                    color = GameOrange,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            CoinDisplay(playerTwo.coins)
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (countdown != -1) {
                Text(
                    text = if (countdown == 0) "GO!" else countdown.toString(),
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 20f))
                )
            }

            if (winner != null) {
                WinnerBanner(winner, onReset)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isRaceStarted && winner == null && countdown == -1) {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GameOrange),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("GET READY!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        } else if (isRaceStarted && winner == null) {
            TapButton(onTap = onTap)
        }
    }
}

@Composable
fun CoinDisplay(amount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text("💰", fontSize = 20.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(amount.toString(), color = Color.Yellow, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TapButton(onTap: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Button(
        onClick = onTap,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
    ) {
        Text(
            "TAP TO RUN!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
fun WinnerBanner(winner: RaceParticipant, onReset: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Text(
            "🎉 WINNER! 🎉",
            color = Color.Yellow,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            winner.name,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "EARNED 100 💰",
            color = GameOrange,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("RESTART", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
