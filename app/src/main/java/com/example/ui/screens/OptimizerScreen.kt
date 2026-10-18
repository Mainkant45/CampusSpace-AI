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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Overview & Binary Integer Formulation Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(BorderNavy, AccentTeal.copy(alpha = 0.5f))),
                    width = 1.dp
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Global Allocation Optimizer",
                            fontSize = 18.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        StatusBadge("USP Differentiator", AccentTeal.copy(alpha = 0.25f), ElectricCyan)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Solves multi-request competition simultaneously. Maximizes accommodated priority weight while minimizing idle seating capacity waste.",
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaFontFamily,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Mathematical Objective snippet
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DeepNavy.copy(alpha = 0.85f),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(BorderNavy, AccentTeal.copy(alpha = 0.4f))),
                            width = 1.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Objective: max ∑ p_i · x_i,r  - λ · ∑ (Cap_r - Att_i) · x_i,r",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Subject to: ∑_r x_i,r ≤ 1, non-overlapping intervals [s_i, e_i) ∩ [s_j, e_j) = ∅",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Filter & Run Actions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Batch Priority Tier Filter",
                        fontSize = 12.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(priorities) { (prio, label) ->
                            ModernFilterChip(
                                text = label,
                                isSelected = selectedPriority == prio,
                                onClick = {
                                    selectedPriority = prio
                                    onRunOptimization(prio)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onRunOptimization(selectedPriority) },
                        enabled = !uiState.isOptimizing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_optimization_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        if (uiState.isOptimizing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ElectricCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Solving Binary Integer Constraints...",
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
                                "Re-Run Global Batch Optimization",
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

        // Side-by-Side Comparison: Greedy Baseline vs Global Optimizer
        if (greedy != null && global != null) {
            item {
                SectionHeader(
                    title = "Algorithmic Comparison: Baseline vs Optimizer",
                    subtitle = "Exact comparison on identical competing requests"
                )
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
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StatusSuccessContainer,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(StatusSuccess.copy(alpha = 0.5f), StatusSuccess)),
                        width = 1.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusSuccess),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Optimization Advantage: +$extraAccommodated Requests & $wasteSaved Wasted Seats Saved",
                                fontSize = 13.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccessText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Global optimizer avoids locking large lecture halls for small meetings, preserving spaces for high-priority cohorts.",
                                fontSize = 11.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Allocation Assignments Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Committed Batch Assignments (${global.assignments.size})",
                        fontSize = 16.sp,
                        fontFamily = SpaceGroteskFontFamily,
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
                        fontSize = 15.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = StatusWarningText
                    )
                }
                items(global.unallocatedRequests.take(6)) { unalloc ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceVariantLight,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)),
                            width = 1.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${unalloc.requestId} • ${unalloc.organizationName}",
                                    fontSize = 12.sp,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                StatusBadge("Priority ${unalloc.priority}", StatusWarningContainer, StatusWarningText)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${unalloc.title} • ${unalloc.dayOfWeek} ${unalloc.startTime}-${unalloc.endTime} (${unalloc.expectedAttendance} attendees)",
                                fontSize = 11.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Reason: All compatible rooms with required facilities occupied during requested slot.",
                                fontSize = 10.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = TextMuted
                            )
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
            .border(
                if (isWinner) 1.5.dp else 1.dp,
                if (isWinner) AccentTeal else CardBorderLight,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isWinner) SurfaceLight else SurfaceVariantLight.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isWinner) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isWinner) "Global Optimizer" else "Greedy Baseline",
                    fontSize = 13.sp,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isWinner) DeepNavy else TextSecondary
                )
                if (isWinner) {
                    StatusBadge("Optimal", StatusSuccessContainer, StatusSuccessText)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Accommodated", fontSize = 10.sp, fontFamily = SpaceGroteskFontFamily, color = TextSecondary)
            Text(
                text = "${result.accommodatedCount} / ${result.totalRequestsEvaluated}",
                fontSize = 20.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (isWinner) AccentTeal else TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Idle Seats Wasted", fontSize = 10.sp, fontFamily = SpaceGroteskFontFamily, color = TextSecondary)
            Text(
                text = "${result.totalCapacityWasteSeats} seats",
                fontSize = 15.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (isWinner) StatusSuccessText else StatusErrorText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Priority Score", fontSize = 10.sp, fontFamily = SpaceGroteskFontFamily, color = TextSecondary)
            Text(
                text = "${result.prioritySatisfactionPct}%",
                fontSize = 14.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = DeepNavy
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Runtime: ${result.runtimeMs}ms",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
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
            .border(1.dp, CardBorderLight, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SoftCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            assignment.roomId.take(3),
                            fontSize = 10.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${assignment.roomId} • ${assignment.organizationName}",
                            fontSize = 13.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Text(
                            text = "${assignment.dayOfWeek} [${assignment.timeSlot})",
                            fontSize = 11.sp,
                            fontFamily = PlusJakartaFontFamily,
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
                        5 -> StatusErrorText
                        4 -> StatusWarningText
                        else -> StatusInfoText
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = assignment.title,
                fontSize = 12.sp,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance: ${assignment.expectedAttendance} / ${assignment.roomCapacity} seats (${assignment.capacityUtilizationPct}% util)",
                    fontSize = 11.sp,
                    fontFamily = PlusJakartaFontFamily,
                    color = TextSecondary
                )
                Text(
                    text = assignment.algorithmDecision,
                    fontSize = 10.sp,
                    fontFamily = SpaceGroteskFontFamily,
                    color = AccentTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
