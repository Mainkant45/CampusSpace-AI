package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Digital Twin Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavy)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Campus Digital Twin Simulator",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        StatusBadge("Isolated Sandbox", StatusWarningContainer, Color(0xFFB45309))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Stress-test campus facilities under hypothetical disruption scenarios: facility renovations, emergency room closures, and club demand spikes. Recomputes global network allocations in real time.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Strict Rule: All scenario records are generated dynamically in memory and isolated from historical ML training data.",
                        fontSize = 10.sp,
                        color = TealLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Scenario Configuration Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = "Scenario Parameters",
                        subtitle = "Configure hypothetical demand surges and facility outages"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Demand Surge Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Demand Surge Rate", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("+$surgePct% Inbound Requests", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
                    }
                    Slider(
                        value = surgePct.toFloat(),
                        onValueChange = { surgePct = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 19,
                        colors = SliderDefaults.colors(thumbColor = AccentTeal, activeTrackColor = AccentTeal)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Attendance Inflation Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Attendance Inflation (+Headcount)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("+$inflationPct% Attendees", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateNavy)
                    }
                    Slider(
                        value = inflationPct.toFloat(),
                        onValueChange = { inflationPct = it.toInt() },
                        valueRange = 0f..50f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = SlateNavy, activeTrackColor = SlateNavy)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Facility Outage Toggles
                    Text("Simulate Building Maintenance / Outage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    buildingOptions.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { bName ->
                                val isOffline = offlineBuildings.contains(bName)
                                FilterChip(
                                    selected = isOffline,
                                    onClick = {
                                        offlineBuildings = if (isOffline) {
                                            offlineBuildings - bName
                                        } else {
                                            offlineBuildings + bName
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (isOffline) Icons.Default.Block else Icons.Default.Business,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    label = { Text(bName.take(16), fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StatusErrorContainer,
                                        selectedLabelColor = StatusError
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        if (uiState.isSimulating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulating Digital Twin...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Recompute Simulation Impact", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Simulation Impact Results
        if (simResult != null) {
            item {
                Text("Simulation Impact & Differential Analysis", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
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
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Displaced",
                        value = "${simResult.displacedRequestCount}",
                        subtitle = "Due to capacity crunch",
                        icon = Icons.Default.Warning,
                        badgeColor = if (simResult.displacedRequestCount > 0) StatusError else StatusSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Narrative Impact Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Digital Twin Scenario Narrative", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = simResult.narrative,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Bottleneck Facilities Absorbing Stress
            if (simResult.bottleneckBuildings.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Bottleneck Facilities Absorbing Overflow", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                            Spacer(modifier = Modifier.height(8.dp))
                            simResult.bottleneckBuildings.forEach { (bName, loadCount) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(bName, fontSize = 12.sp, color = TextPrimary)
                                    StatusBadge("$loadCount Sessions Rerouted", SoftCyan, TealDark)
                                }
                            }
                        }
                    }
                }
            }

            // Changed Assignments Log
            if (simResult.changedAssignments.isNotEmpty()) {
                item {
                    Text("Shifted Room Allocations (${simResult.changedAssignments.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                }
                items(simResult.changedAssignments) { (reqId, change) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(reqId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(change, fontSize = 11.sp, color = AccentTeal, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
