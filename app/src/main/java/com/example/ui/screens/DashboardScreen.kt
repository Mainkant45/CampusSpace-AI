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
        "All Roles" to Icons.Default.AllInclusive,
        "Student" to Icons.Default.School,
        "Club Coordinator" to Icons.Default.Groups,
        "Faculty" to Icons.Default.MenuBook,
        "Facilities Admin" to Icons.Default.Engineering
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Visual Banner with High-Tech Scrim and Dual Quick Actions
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(230.dp)) {
                    // Campus architectural digital twin render
                    Image(
                        painter = painterResource(id = R.drawable.img_campus_hero),
                        contentDescription = "CampusSpace Digital Twin Network",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Triple-stop gradient overlay for crisp contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        DeepNavy.copy(alpha = 0.30f),
                                        DeepNavy.copy(alpha = 0.80f),
                                        DeepNavy.copy(alpha = 0.98f)
                                    )
                                )
                            )
                    )

                    // Content & Controls
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
                                        text = "TWIN SYSTEM ONLINE",
                                        fontSize = 10.sp,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }

                            StatusBadge(
                                text = "120 Facilities • R²=0.94",
                                backgroundColor = AccentTeal.copy(alpha = 0.25f),
                                textColor = ElectricCyan
                            )
                        }

                        Column {
                            Text(
                                text = "Intelligent Campus Space Allocation",
                                fontSize = 19.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-0.2).sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Constraint satisfaction & ML demand forecasting across 6 university complexes.",
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onNavigateTab(CampusSpaceTab.ROOM_FINDER) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("hero_find_room_button")
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = DeepNavy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Find Space",
                                        color = DeepNavy,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onNavigateTab(CampusSpaceTab.OPTIMIZER) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.horizontalGradient(listOf(TealLight, AccentTeal)),
                                        width = 1.dp
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("hero_optimize_button")
                                ) {
                                    Icon(
                                        Icons.Default.AutoFixHigh,
                                        contentDescription = null,
                                        tint = TealLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Run Optimizer",
                                        color = Color.White,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Persona Quick-Switch Bar with Modern Chips
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROLE INSIGHTS",
                        fontSize = 11.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Filter Recommendations",
                        fontSize = 11.sp,
                        fontFamily = PlusJakartaFontFamily,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(personas) { (p, icon) ->
                        ModernFilterChip(
                            text = p,
                            isSelected = selectedPersona == p,
                            onClick = { selectedPersona = p },
                            icon = icon
                        )
                    }
                }
            }
        }

        // Persona Dynamic Contextual Insight Glass Card
        if (selectedPersona != "All Roles") {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceLight,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(AccentTeal.copy(alpha = 0.4f), ElectricCyan.copy(alpha = 0.7f))),
                        width = 1.dp
                    ),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SoftCyan),
                            contentAlignment = Alignment.Center
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
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (selectedPersona) {
                                    "Student" -> "Student Study & Review Guidance"
                                    "Club Coordinator" -> "Club & Workshop Planning Mode"
                                    "Faculty" -> "Faculty Departmental Session Advisory"
                                    else -> "Facilities Maintenance & Space Governance"
                                },
                                fontSize = 13.sp,
                                fontFamily = SpaceGroteskFontFamily,
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
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 4 Primary Dataset Telemetry KPIs
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
                    deltaText = "100% Active",
                    isDeltaPositive = true,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "class_schedule",
                    value = "${uiState.auditSummary?.classScheduleCount ?: 5000}",
                    subtitle = "5,000 Timetable Blocks",
                    icon = Icons.Default.CalendarMonth,
                    badgeColor = StatusInfo,
                    deltaText = "Academic Cal",
                    isDeltaPositive = true,
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
                    deltaText = "Real Demand",
                    isDeltaPositive = true,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "events.csv",
                    value = "${uiState.auditSummary?.eventsCount ?: 1800}",
                    subtitle = "1,800 ML Ground Truth",
                    icon = Icons.Default.Psychology,
                    badgeColor = PurpleAccent,
                    deltaText = "R² = 0.94",
                    isDeltaPositive = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Scheduled Timetable Utilization by Building
        if (report != null) {
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
                            title = "Scheduled Utilization by Building",
                            subtitle = "Weekly timetable hours booked vs total operational facility capacity",
                            actionButtonText = "View Audit",
                            onActionClick = { onNavigateTab(CampusSpaceTab.DATA_AUDIT) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        report.buildingUtilizations.forEach { b ->
                            LinearUtilizationBar(
                                percentage = b.utilizationPercent,
                                label = b.buildingName,
                                detailText = "${b.roomCount} rooms • ${b.totalCapacity} seats",
                                barColor = when {
                                    b.utilizationPercent >= 60 -> AccentTeal
                                    b.utilizationPercent < 35 -> StatusWarning
                                    else -> StatusInfo
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Demand Distribution by Time of Day & Day of Week
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
                            title = "Peak Demand Windows",
                            subtitle = "Peak Day: ${report.peakDemandDay} • Peak Slot: ${report.peakDemandSlot}"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        report.timeSlotDemands.forEach { slot ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = slot.slotLabel,
                                    fontSize = 12.sp,
                                    fontFamily = PlusJakartaFontFamily,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${slot.requestCount} requests (${slot.percentage}%)",
                                    fontSize = 12.sp,
                                    fontFamily = SpaceGroteskFontFamily,
                                    color = DeepNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceVariantLight)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(slot.percentage / 100f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(AccentTeal, ElectricCyan)
                                            )
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Underutilized Spaces Highlight (<25%)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Underutilized Spaces (<25%)",
                                    fontSize = 16.sp,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                Text(
                                    text = "Ready to absorb club & workshop overflow",
                                    fontSize = 12.sp,
                                    fontFamily = PlusJakartaFontFamily,
                                    color = TextSecondary
                                )
                            }
                            StatusBadge(
                                text = "${report.topUnderutilizedRooms.size} Found",
                                backgroundColor = StatusWarningContainer,
                                textColor = StatusWarningText
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        report.topUnderutilizedRooms.take(5).forEach { rm ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceVariantLight.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = rm.roomId,
                                                fontSize = 14.sp,
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepNavy
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = SurfaceLight
                                            ) {
                                                Text(
                                                    text = rm.building,
                                                    fontSize = 10.sp,
                                                    fontFamily = PlusJakartaFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${rm.roomType} • Capacity: ${rm.capacity} seats",
                                            fontSize = 11.sp,
                                            fontFamily = PlusJakartaFontFamily,
                                            color = TextSecondary
                                        )
                                    }
                                    StatusBadge(
                                        text = "${rm.utilizationPercent}% booked (${rm.scheduledHoursPerWeek}h/wk)",
                                        backgroundColor = SurfaceLight,
                                        textColor = DeepNavy
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Responsible Reporting Caveat Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoftCyan.copy(alpha = 0.5f),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(AccentTeal.copy(alpha = 0.4f), ElectricCyan.copy(alpha = 0.6f))),
                        width = 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dataset Integrity & Reporting Standard",
                                fontSize = 13.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = report.educationalCaveat,
                                fontSize = 11.sp,
                                fontFamily = PlusJakartaFontFamily,
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
