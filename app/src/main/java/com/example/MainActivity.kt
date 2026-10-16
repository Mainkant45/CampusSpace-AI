package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CampusSpaceTab
import com.example.ui.CampusSpaceViewModel
import com.example.ui.components.AppTopBar
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusSpaceTheme {
                CampusSpaceMainScreen()
            }
        }
    }
}

@Composable
fun CampusSpaceMainScreen(
    viewModel: CampusSpaceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Feedback message display
    LaunchedEffect(uiState.feedbackMessage) {
        val msg = uiState.feedbackMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    // Predictive back handler: if on a sub-screen, return to Dashboard
    BackHandler(enabled = uiState.selectedTab != CampusSpaceTab.DASHBOARD) {
        viewModel.selectTab(CampusSpaceTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(modifier = Modifier.testTag("app_top_bar"))
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepNavy,
                contentColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val navItems = listOf(
                    CampusSpaceTab.DASHBOARD to (Icons.Default.Dashboard to "Overview"),
                    CampusSpaceTab.ROOM_FINDER to (Icons.Default.Search to "Finder"),
                    CampusSpaceTab.OPTIMIZER to (Icons.Default.AutoFixHigh to "Optimizer"),
                    CampusSpaceTab.DIGITAL_TWIN to (Icons.Default.Hub to "Simulator"),
                    CampusSpaceTab.ML_LAB to (Icons.Default.Psychology to "ML Lab"),
                    CampusSpaceTab.DATA_AUDIT to (Icons.Default.Dataset to "Audit")
                )

                navItems.forEach { (tab, iconTitle) ->
                    val isSelected = uiState.selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = iconTitle.first,
                                contentDescription = iconTitle.second,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = iconTitle.second,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepNavy,
                            selectedTextColor = AccentTeal,
                            indicatorColor = AccentTeal,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundLight)
        ) {
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AccentTeal, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading benchmark datasets & training ML model from scratch...",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            } else {
                when (uiState.selectedTab) {
                    CampusSpaceTab.DASHBOARD -> DashboardScreen(
                        uiState = uiState,
                        onNavigateTab = { viewModel.selectTab(it) }
                    )
                    CampusSpaceTab.ROOM_FINDER -> RoomFinderScreen(
                        uiState = uiState,
                        onSearch = { day, sTime, eTime, att, type, proj, comp, acc, bldg ->
                            viewModel.searchRooms(day, sTime, eTime, att, type, proj, comp, acc, bldg)
                        },
                        onBookRoom = { room, day, sTime, eTime ->
                            viewModel.bookRoom(room, day, sTime, eTime)
                        }
                    )
                    CampusSpaceTab.OPTIMIZER -> OptimizerScreen(
                        uiState = uiState,
                        onRunOptimization = { prio ->
                            viewModel.runOptimization(prio)
                        }
                    )
                    CampusSpaceTab.DIGITAL_TWIN -> DigitalTwinScreen(
                        uiState = uiState,
                        onRunSimulation = { surge, bldgs, rms, infl ->
                            viewModel.runSimulation(surge, bldgs, rms, infl)
                        }
                    )
                    CampusSpaceTab.ML_LAB -> MlLabScreen(
                        uiState = uiState,
                        onRetrainModel = { seed ->
                            viewModel.trainMLModel(seed)
                        },
                        onPredictAttendance = { exp, type, hour, dur, day ->
                            viewModel.predictAttendance(exp, type, hour, dur, day)
                        }
                    )
                    CampusSpaceTab.DATA_AUDIT -> DataAuditScreen(
                        uiState = uiState,
                        getRooms = { viewModel.getRooms() },
                        getClassSchedules = { viewModel.getClassSchedules() },
                        getRoomRequests = { viewModel.getRoomRequests() },
                        getEvents = { viewModel.getEvents() },
                        getReadmeText = { viewModel.getReadmeText() }
                    )
                }
            }
        }
    }
}
