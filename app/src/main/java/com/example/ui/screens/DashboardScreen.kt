package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.ui.CampusSpaceTab
import com.example.ui.CampusSpaceUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    uiState: CampusSpaceUiState,
    onNavigateTab: (CampusSpaceTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val report = uiState.demandReport
    var selectedPersona by remember { mutableStateOf("All Roles") }

    val personas = listOf(
        "All Roles",
        "Student",
        "Club Coordinator",
        "Faculty",
        "Facilities Admin"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Visual Banner with Campus Architecture & Network Layer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    // Campus aerial architectural render
                    Image(
                        painter = painterResource(id = R.drawable.img_campus_hero),
                        contentDescription = "CampusSpace Digital Twin Network",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Multi-stop gradient overlay for high-contrast typography
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        DeepNavy.copy(alpha = 0.25f),
                                        DeepNavy.copy(alpha = 0.75f),
                                        DeepNavy.copy(alpha = 0.96f)
                                    )
                                )
                            )
                    )

                    // Text & Action Overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = DeepNavy.copy(alpha = 0.85f),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(AccentTeal, TealLight))
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(StatusSuccess)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CAMPUS DIGITAL TWIN ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.6.sp
                                    )
                                }
                            }

                            StatusBadge(
                                text = "120 Facilities",
                                backgroundColor = AccentTeal.copy(alpha = 0.25f),
                                textColor = TealLight
                            )
                        }

                        Column {
                            Text(
                                text = "Intelligent Campus Space Allocation",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Explainable constraint optimization & ML demand forecasting across 6 university complexes.",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onNavigateTab(CampusSpaceTab.ROOM_FINDER) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("hero_find_room_button")
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Find Space", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onNavigateTab(CampusSpaceTab.OPTIMIZER) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.horizontalGradient(listOf(TealLight, AccentTeal))
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("hero_optimize_button")
                                ) {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = TealLight, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Optimize Batch", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Persona Quick-Switch Bar
        item {
            Column {
                Text(
                    text = "Tailor Experience by Campus Persona",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(personas) { p ->
                        val isSelected = selectedPersona == p
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPersona = p },
                            label = { Text(p, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepNavy,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceLight,
                                labelColor = TextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) AccentTeal else CardBorderLight
                            )
                        )
                    }
                }
            }
        }

        // Persona Dynamic Contextual Insight
        if (selectedPersona != "All Roles") {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SlateNavy.copy(alpha = 0.08f),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(AccentTeal.copy(alpha = 0.5f), AccentTeal)))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (selectedPersona) {
                                "Student" -> Icons.Default.School
                                "Club Coordinator" -> Icons.Default.Groups
                                "Faculty" -> Icons.Default.MenuBook
                                else -> Icons.Default.Engineering
                            },
                            contentDescription = null,
                            tint = DeepNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (selectedPersona) {
                                    "Student" -> "Student Study & Review Guidance"
                                    "Club Coordinator" -> "Club & Workshop Planning Mode"
                                    "Faculty" -> "Faculty Departmental Session Advisory"
                                    else -> "Facilities Maintenance & Space Governance"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (selectedPersona) {
                                    "Student" -> "Top pick: Library study lounges and Computer Lab CMP-205 have >4 hours unscheduled between 14:00 - 18:00 on Fridays."
                                    "Club Coordinator" -> "High demand peak detected between 17:00 - 19:00 on Wednesdays. Check Science Hall (SCI) for underused lecture halls with projectors."
                                    "Faculty" -> "Priority 4 & 5 bookings are protected with highest weight in the Global Optimizer engine."
                                    else -> "Underutilized rooms identified with <20% utilization ready for HVAC power-down or term maintenance windows."
                                },
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 4 Primary Dataset KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiCard(
                    title = "rooms.csv",
                    value = "${uiState.auditSummary?.roomsCount ?: 120}",
                    subtitle = "120 Facilities • 6 Complexes",
                    icon = Icons.Default.MeetingRoom,
                    badgeColor = AccentTeal,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "class_schedule",
                    value = "${uiState.auditSummary?.classScheduleCount ?: 5000}",
                    subtitle = "5,000 Timetable Blocks",
                    icon = Icons.Default.CalendarMonth,
                    badgeColor = StatusInfo,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricKpiCard(
                    title = "room_requests",
                    value = "${uiState.auditSummary?.roomRequestsCount ?: 5000}",
                    subtitle = "5,000 Historical Requests",
                    icon = Icons.Default.PendingActions,
                    badgeColor = StatusWarning,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "events.csv",
                    value = "${uiState.auditSummary?.eventsCount ?: 1800}",
                    subtitle = "1,800 ML Ground Truth",
                    icon = Icons.Default.Psychology,
                    badgeColor = PurpleAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Scheduled Timetable Utilization by Building
        if (report != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SectionHeader(
                                title = "Scheduled Utilization by Building",
                                subtitle = "Weekly timetable hours booked vs total operational facility capacity"
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        report.buildingUtilizations.forEach { b ->
                            LinearUtilizationBar(
                                percentage = b.utilizationPercent,
                                label = "${b.buildingName} (${b.roomCount} rooms • ${b.totalCapacity} total seats)",
                                barColor = when {
                                    b.utilizationPercent >= 60 -> AccentTeal
                                    b.utilizationPercent < 35 -> StatusWarning
                                    else -> StatusInfo
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            // Demand Distribution by Time of Day & Day of Week
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(
                            title = "Demand Dynamics: Peak Periods",
                            subtitle = "Peak Request Day: ${report.peakDemandDay} • Peak Window: ${report.peakDemandSlot}"
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        report.timeSlotDemands.forEach { slot ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(slot.slotLabel, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                Text("${slot.requestCount} requests (${slot.percentage}%)", fontSize = 12.sp, color = DeepNavy, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(SurfaceVariantLight, RoundedCornerShape(3.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(slot.percentage / 100f)
                                        .fillMaxHeight()
                                        .background(AccentTeal, RoundedCornerShape(3.dp))
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            // Underutilized Spaces Highlight (<25%)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Underutilized Spaces (<25% Booked)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                Text("High-availability candidates to absorb club & workshop overflow", fontSize = 12.sp, color = TextSecondary)
                            }
                            StatusBadge(
                                text = "${report.topUnderutilizedRooms.size} Found",
                                backgroundColor = StatusWarningContainer,
                                textColor = Color(0xFFB45309)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        report.topUnderutilizedRooms.take(5).forEach { rm ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${rm.roomId} • ${rm.building}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${rm.roomType} • Capacity: ${rm.capacity} seats • ${rm.status}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                StatusBadge(
                                    text = "${rm.utilizationPercent}% booked (${rm.scheduledHoursPerWeek}h/wk)",
                                    backgroundColor = SurfaceVariantLight,
                                    textColor = TextSecondary
                                )
                            }
                            Divider(color = CardBorderLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }

            // Data Science & Responsible Reporting Caveat
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftCyan.copy(alpha = 0.45f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AccentTeal.copy(alpha = 0.5f), AccentTeal)))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TealDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Dataset Integrity & Reporting Standard", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = report.educationalCaveat,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
