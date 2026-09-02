package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.FormEntryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RecordListScreen
import com.example.ui.viewmodel.FormViewModel

enum class AppScreen {
    Login,
    RecordList,
    FormEntry
}

@Composable
fun MainApp(
    viewModel: FormViewModel = viewModel(),
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.RecordList) }

    // Navigation controller based on Login state
    val targetScreen = if (!isLoggedIn) {
        AppScreen.Login
    } else {
        if (currentScreen == AppScreen.Login) {
            AppScreen.RecordList
        } else {
            currentScreen
        }
    }

    Crossfade(
        targetState = targetScreen,
        animationSpec = tween(durationMillis = 300),
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            AppScreen.Login -> {
                LoginScreen(viewModel = viewModel)
            }
            AppScreen.RecordList -> {
                RecordListScreen(
                    viewModel = viewModel,
                    onNavigateToForm = { currentScreen = AppScreen.FormEntry }
                )
            }
            AppScreen.FormEntry -> {
                FormEntryScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.RecordList }
                )
            }
        }
    }
}
