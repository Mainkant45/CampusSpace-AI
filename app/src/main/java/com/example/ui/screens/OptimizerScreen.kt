package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.AllocationAssignment
import com.example.data.model.OptimizationResult
import com.example.ui.CampusSpaceUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun OptimizerScreen(
    uiState: CampusSpaceUiState,
    onRunOptimization: (priorityFilter: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPriority by remember { mutableIntStateOf(uiState.selectedPriorityFilter) }
    val greedy = uiState.greedyResult
    val global = uiState.globalResult

    val priorities = listOf(
        0 to "All Priorities",
        5 to "P5 (Urgent/Safety)",
        4 to "P4 (Faculty/Dept)",
        3 to "P3 (Clubs/Symposia)",
        2 to "P2 (General Meeting)",
        1 to "P1 (Study Session)"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Overview & Binary Integer Formulation Header
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
                            text = "Global Allocation Optimizer",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        StatusBadge("USP Differentiator", AccentTeal.copy(alpha = 0.25f), TealLight)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Solves multi-request competition simultaneously. Maximizes accommodated priority weight while minimizing idle seating capacity waste.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Mathematical Objective snippet
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DeepNavy.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Objective: max ∑ p_i · x_i,r  - λ · ∑ (Cap_r - Att_i) · x_i,r",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealLight,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Text(
                                text = "Subject to: ∑_r x_i,r ≤ 1, non-overlapping intervals [s_i, e_i) ∩ [s_j, e_j) = ∅",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Filter & Run Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Batch Filter by Priority Tier", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(priorities) { (prio, label) ->
                            FilterChip(
                                selected = selectedPriority == prio,
                                onClick = {
                                    selectedPriority = prio
                                    onRunOptimization(prio)
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onRunOptimization(selectedPriority) },
                        enabled = !uiState.isOptimizing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                    ) {
                        if (uiState.isOptimizing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepNavy, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Solving Constraints...", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Re-Run Global Batch Optimization", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Side-by-Side Comparison: Greedy Baseline vs Global Optimizer
        if (greedy != null && global != null) {
            item {
                Text("Algorithmic Comparison: Baseline vs Optimizer", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Greedy Baseline Card
                    AlgorithmComparisonCard(
                        result = greedy,
                        isWinner = false,
                        modifier = Modifier.weight(1f)
                    )
                    // Global Optimizer Card
                    AlgorithmComparisonCard(
                        result = global,
                        isWinner = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Delta Summary Banner
            item {
                val wasteSaved = greedy.totalCapacityWasteSeats - global.totalCapacityWasteSeats
                val extraAccommodated = global.accommodatedCount - greedy.accommodatedCount
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusSuccessContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Optimization Advantage: +$extraAccommodated Requests & $wasteSaved Wasted Seats Saved",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "Global optimizer avoids locking large lecture halls for small meetings, preserving spaces for high-priority student & faculty cohorts.",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Allocation Assignments Table / List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Committed Batch Assignments (${global.assignments.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    StatusBadge("100% Conflict-Free", SoftCyan, TealDark)
                }
            }

            items(global.assignments.take(15)) { assignment ->
                AssignmentCard(assignment = assignment)
            }

            // Unallocated Requests (if any)
            if (global.unallocatedRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Unaccommodated Requests (${global.unallocatedRequests.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusWarning
                    )
                }
                items(global.unallocatedRequests.take(6)) { unalloc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${unalloc.requestId} • ${unalloc.organizationName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                StatusBadge("Priority ${unalloc.priority}", StatusWarningContainer, Color(0xFFB45309))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${unalloc.title} • ${unalloc.dayOfWeek} ${unalloc.startTime}-${unalloc.endTime} (${unalloc.expectedAttendance} attendees)", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Reason: All compatible rooms with required facilities occupied during requested slot.", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlgorithmComparisonCard(
    result: OptimizationResult,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, if (isWinner) AccentTeal else CardBorderLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isWinner) SurfaceLight else SurfaceVariantLight),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isWinner) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isWinner) "Global Optimizer" else "Greedy Baseline",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isWinner) DeepNavy else TextSecondary
                )
                if (isWinner) {
                    StatusBadge("Best", StatusSuccessContainer, Color(0xFF065F46))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Accommodated", fontSize = 10.sp, color = TextSecondary)
            Text(
                text = "${result.accommodatedCount} / ${result.totalRequestsEvaluated}",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isWinner) AccentTeal else TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Idle Seats Wasted", fontSize = 10.sp, color = TextSecondary)
            Text(
                text = "${result.totalCapacityWasteSeats} seats",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWinner) StatusSuccess else StatusError
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Priority Score", fontSize = 10.sp, color = TextSecondary)
            Text(
                text = "${result.prioritySatisfactionPct}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepNavy
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Runtime: ${result.runtimeMs}ms",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: AllocationAssignment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderLight, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(assignment.roomId.take(3), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${assignment.roomId} • ${assignment.organizationName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Text(
                            text = "${assignment.dayOfWeek} [${assignment.timeSlot})",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                StatusBadge(
                    text = "P${assignment.priority}",
                    backgroundColor = when (assignment.priority) {
                        5 -> StatusErrorContainer
                        4 -> StatusWarningContainer
                        else -> StatusInfoContainer
                    },
                    textColor = when (assignment.priority) {
                        5 -> StatusError
                        4 -> Color(0xFFB45309)
                        else -> StatusInfo
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = assignment.title,
                fontSize = 12.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Attendance: ${assignment.expectedAttendance} / ${assignment.roomCapacity} seats (${assignment.capacityUtilizationPct}% util)",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = assignment.algorithmDecision,
                    fontSize = 10.sp,
                    color = AccentTeal,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
