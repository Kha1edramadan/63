package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label         = "logoAlpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(1600)
        onTimeout()
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter            = painterResource(id = R.drawable.ic_igym_logo),
            contentDescription = null,
            tint               = Color.Unspecified,
            modifier           = Modifier
                .size(120.dp)
                .alpha(alpha)
        )
    }
}
