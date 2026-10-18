package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.CampusSpaceUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DigitalTwinScreen(
    uiState: CampusSpaceUiState,
    onRunSimulation: (
        surgePct: Int,
        offlineBuildings: Set<String>,
        offlineRooms: Set<String>,
        inflationPct: Int
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var surgePct by remember { mutableIntStateOf(uiState.simulationSurge) }
    var inflationPct by remember { mutableIntStateOf(uiState.simulationInflation) }

    val buildingOptions = listOf(
        "Engineering Complex",
        "Science Hall",
        "Computing Center",
        "Business Tower",
        "Arts Pavilion",
        "University Library"
    )
    var offlineBuildings by remember { mutableStateOf(uiState.simulationOfflineBuildings) }

    val simResult = uiState.simulationResult

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Digital Twin Hero 3D Graphic Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("digital_twin_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    // 3D Isometric architectural digital twin visual
                    Image(
                        painter = painterResource(id = R.drawable.img_digital_twin_hub),
                        contentDescription = "Campus Digital Twin Simulator Hub",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Scrim gradient for text contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        DeepNavy.copy(alpha = 0.25f),
                                        DeepNavy.copy(alpha = 0.70f),
                                        DeepNavy.copy(alpha = 0.96f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = DeepNavy.copy(alpha = 0.9f),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(AccentTeal, ElectricCyan)),
                                    width = 1.dp
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(StatusSuccess)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ISOLATED TWIN SANDBOX",
                                        fontSize = 10.sp,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }

                            StatusBadge(
                                text = "Zero Training Pollution",
                                backgroundColor = StatusWarningContainer,
                                textColor = StatusWarningText
                            )
                        }

                        Column {
                            Text(
                                text = "Campus Digital Twin Simulator",
                                fontSize = 19.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-0.2).sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Stress-test 120 campus facilities under demand surges, outages, and emergency closures in real time.",
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Scenario Configuration Controls Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        title = "Scenario Stress Parameters",
                        subtitle = "Configure hypothetical demand surges and facility maintenance blackouts"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Demand Surge Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Demand Surge Rate",
                            fontSize = 13.sp,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "+$surgePct% Inbound Requests",
                            fontSize = 13.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AccentTeal
                        )
                    }
                    Slider(
                        value = surgePct.toFloat(),
                        onValueChange = { surgePct = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentTeal,
                            activeTrackColor = AccentTeal,
                            inactiveTrackColor = SurfaceVariantLight
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Attendance Inflation Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attendance Inflation (+Headcount)",
                            fontSize = 13.sp,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "+$inflationPct% Attendees",
                            fontSize = 13.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                    }
                    Slider(
                        value = inflationPct.toFloat(),
                        onValueChange = { inflationPct = it.toInt() },
                        valueRange = 0f..50f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = DeepNavy,
                            activeTrackColor = DeepNavy,
                            inactiveTrackColor = SurfaceVariantLight
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Facility Outage Toggles
                    Text(
                        text = "Simulate Facility Maintenance & Outages",
                        fontSize = 13.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    buildingOptions.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { bName ->
                                val isOffline = offlineBuildings.contains(bName)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isOffline) StatusErrorContainer else SurfaceVariantLight.copy(alpha = 0.6f),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = if (isOffline) {
                                            Brush.horizontalGradient(listOf(StatusError, StatusError))
                                        } else {
                                            Brush.horizontalGradient(listOf(CardBorderLight, CardBorderLight))
                                        },
                                        width = 1.dp
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            offlineBuildings = if (isOffline) {
                                                offlineBuildings - bName
                                            } else {
                                                offlineBuildings + bName
                                            }
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isOffline) Icons.Default.Block else Icons.Default.Business,
                                            contentDescription = null,
                                            tint = if (isOffline) StatusError else TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = bName.take(16),
                                            fontSize = 11.sp,
                                            fontFamily = PlusJakartaFontFamily,
                                            fontWeight = if (isOffline) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isOffline) StatusErrorText else TextPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onRunSimulation(
                                surgePct,
                                offlineBuildings,
                                emptySet(),
                                inflationPct
                            )
                        },
                        enabled = !uiState.isSimulating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_simulation_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        if (uiState.isSimulating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ElectricCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Recomputing Digital Twin Network...",
                                color = Color.White,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Run Stress-Test Simulation",
                                color = Color.White,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Simulation Impact Results
        if (simResult != null) {
            item {
                SectionHeader(
                    title = "Simulation Differential Analysis",
                    subtitle = "Dynamic recomputation result against baseline schedule"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricKpiCard(
                        title = "Accommodated",
                        value = "${simResult.simulatedAccommodated}",
                        subtitle = "Baseline: ${simResult.baselineAccommodated}",
                        icon = Icons.Default.AssignmentTurnedIn,
                        badgeColor = AccentTeal,
                        deltaText = if (simResult.simulatedAccommodated >= simResult.baselineAccommodated) "Stable" else "-${simResult.baselineAccommodated - simResult.simulatedAccommodated}",
                        isDeltaPositive = simResult.simulatedAccommodated >= simResult.baselineAccommodated,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Displaced",
                        value = "${simResult.displacedRequestCount}",
                        subtitle = "Capacity shortfall",
                        icon = Icons.Default.Warning,
                        badgeColor = if (simResult.displacedRequestCount > 0) StatusError else StatusSuccess,
                        deltaText = if (simResult.displacedRequestCount > 0) "Crunch" else "0 Shortfall",
                        isDeltaPositive = simResult.displacedRequestCount == 0,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Narrative Impact Glass Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SoftCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Insights,
                                    contentDescription = null,
                                    tint = AccentTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Digital Twin Scenario Narrative",
                                fontSize = 14.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = simResult.narrative,
                            fontSize = 12.sp,
                            fontFamily = PlusJakartaFontFamily,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Bottleneck Facilities Absorbing Stress
            if (simResult.bottleneckBuildings.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Bottleneck Facilities Absorbing Rerouted Load",
                                fontSize = 14.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            simResult.bottleneckBuildings.forEach { (bName, loadCount) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceVariantLight.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            bName,
                                            fontSize = 12.sp,
                                            fontFamily = PlusJakartaFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        StatusBadge(
                                            "$loadCount Sessions Rerouted",
                                            SoftCyan,
                                            TealDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Shifted Allocations Log
            if (simResult.changedAssignments.isNotEmpty()) {
                item {
                    Text(
                        "Shifted Room Allocations (${simResult.changedAssignments.size})",
                        fontSize = 15.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }
                items(simResult.changedAssignments.take(8)) { (reqId, change) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceLight,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)),
                            width = 1.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                reqId,
                                fontSize = 12.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                change,
                                fontSize = 11.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = AccentTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
