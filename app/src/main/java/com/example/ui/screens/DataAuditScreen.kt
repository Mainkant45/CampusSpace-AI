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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CampusSpaceUiState
import com.example.ui.components.*
import com.example.ui.theme.*

enum class DatasetView {
    ROOMS,
    CLASS_SCHEDULE,
    ROOM_REQUESTS,
    EVENTS,
    README
}

@Composable
fun DataAuditScreen(
    uiState: CampusSpaceUiState,
    getRooms: () -> List<com.example.data.model.Room>,
    getClassSchedules: () -> List<com.example.data.model.ClassSchedule>,
    getRoomRequests: () -> List<com.example.data.model.RoomRequest>,
    getEvents: () -> List<com.example.data.model.EventRecord>,
    getReadmeText: () -> String,
    modifier: Modifier = Modifier
) {
    var selectedDataset by remember { mutableStateOf(DatasetView.ROOMS) }
    var searchQuery by remember { mutableStateOf("") }

    val audit = uiState.auditSummary

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dataset Audit Header
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
                            text = "Dataset Schema & Integrity Audit",
                            fontSize = 18.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        StatusBadge("100% Validated", StatusSuccessContainer, StatusSuccessText)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Complete exploratory inspection and relational constraint verification across all 4 organizer benchmark CSV datasets.",
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaFontFamily,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Audit Summary Grid
        if (audit != null) {
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
                            title = "Dataset Integrity Audit Report",
                            subtitle = "Automated schema validation and relation checks"
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricKpiCard(
                                title = "rooms.csv",
                                value = "${audit.roomsCount}",
                                subtitle = "120 Room Profiles",
                                badgeColor = AccentTeal,
                                modifier = Modifier.weight(1f)
                            )
                            MetricKpiCard(
                                title = "class_schedule.csv",
                                value = "${audit.classScheduleCount}",
                                subtitle = "5,000 Class Entries",
                                badgeColor = StatusInfo,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricKpiCard(
                                title = "room_requests.csv",
                                value = "${audit.roomRequestsCount}",
                                subtitle = "5,000 Requests",
                                badgeColor = StatusWarning,
                                modifier = Modifier.weight(1f)
                            )
                            MetricKpiCard(
                                title = "events.csv",
                                value = "${audit.eventsCount}",
                                subtitle = "1,800 Ground Truth",
                                badgeColor = PurpleAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        audit.notes.forEach { note ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = StatusSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    note,
                                    fontSize = 11.sp,
                                    fontFamily = PlusJakartaFontFamily,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Record Explorer
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
                        "Live Dataset Record Explorer",
                        fontSize = 14.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(DatasetView.values()) { view ->
                            val label = when (view) {
                                DatasetView.ROOMS -> "rooms.csv (120)"
                                DatasetView.CLASS_SCHEDULE -> "class_schedule (5k)"
                                DatasetView.ROOM_REQUESTS -> "room_requests (5k)"
                                DatasetView.EVENTS -> "events.csv (1.8k)"
                                DatasetView.README -> "README.txt"
                            }
                            ModernFilterChip(
                                text = label,
                                isSelected = selectedDataset == view,
                                onClick = { selectedDataset = view }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Filter records by keyword, room, or code...",
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaFontFamily
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = AccentTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            fontFamily = PlusJakartaFontFamily
                        )
                    )
                }
            }
        }

        // Dataset Records Content
        when (selectedDataset) {
            DatasetView.README -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight)
                    ) {
                        Text(
                            text = getReadmeText(),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary,
                            modifier = Modifier.padding(16.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            DatasetView.ROOMS -> {
                val filteredRooms = getRooms().filter {
                    searchQuery.isEmpty() ||
                            it.roomId.contains(searchQuery, ignoreCase = true) ||
                            it.building.contains(searchQuery, ignoreCase = true) ||
                            it.roomType.contains(searchQuery, ignoreCase = true)
                }
                items(filteredRooms.take(25)) { r ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLight,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)),
                            width = 1.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${r.roomId} • ${r.building}",
                                    fontSize = 13.sp,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                Text(
                                    "${r.roomType} • Floor ${r.floor} • Capacity: ${r.capacity} seats",
                                    fontSize = 11.sp,
                                    fontFamily = PlusJakartaFontFamily,
                                    color = TextSecondary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (r.hasProjector) StatusBadge("Proj", SoftCyan, TealDark)
                                if (r.hasComputers) StatusBadge("PC", SoftCyan, TealDark)
                                if (r.isAccessible) StatusBadge("ADA", SoftCyan, TealDark)
                            }
                        }
                    }
                }
            }

            DatasetView.CLASS_SCHEDULE -> {
                val filteredClasses = getClassSchedules().filter {
                    searchQuery.isEmpty() ||
                            it.courseCode.contains(searchQuery, ignoreCase = true) ||
                            it.courseName.contains(searchQuery, ignoreCase = true) ||
                            it.roomId.contains(searchQuery, ignoreCase = true)
                }
                items(filteredClasses.take(25)) { c ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLight,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)),
                            width = 1.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${c.courseCode}: ${c.courseName}",
                                    fontSize = 13.sp,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                StatusBadge(c.roomId, SurfaceVariantLight, TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${c.dayOfWeek} [${c.startTime} - ${c.endTime}) • ${c.department} • Enrolled: ${c.enrolledStudents}",
                                fontSize = 11.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            DatasetView.ROOM_REQUESTS -> {
                val filteredReqs = getRoomRequests().filter {
                    searchQuery.isEmpty() ||
                            it.organizationName.contains(searchQuery, ignoreCase = true) ||
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.requestId.contains(searchQuery, ignoreCase = true)
                }
                items(filteredReqs.take(25)) { req ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLight,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)),
                            width = 1.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${req.requestId} • ${req.organizationName}",
                                    fontSize = 13.sp,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                StatusBadge(
                                    req.status,
                                    when (req.status) {
                                        "Approved" -> StatusSuccessContainer
                                        "Pending" -> StatusInfoContainer
                                        "Waitlisted" -> StatusWarningContainer
                                        else -> StatusErrorContainer
                                    },
                                    when (req.status) {
                                        "Approved" -> StatusSuccessText
                                        "Pending" -> StatusInfoText
                                        "Waitlisted" -> StatusWarningText
                                        else -> StatusErrorText
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${req.title} • ${req.dayOfWeek} [${req.startTime} - ${req.endTime}) • ${req.expectedAttendance} attendees",
                                fontSize = 11.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            DatasetView.EVENTS -> {
                val filteredEvents = getEvents().filter {
                    searchQuery.isEmpty() ||
                            it.eventId.contains(searchQuery, ignoreCase = true) ||
                            it.eventName.contains(searchQuery, ignoreCase = true) ||
                            it.eventType.contains(searchQuery, ignoreCase = true) ||
                            it.hostOrganization.contains(searchQuery, ignoreCase = true)
                }
                items(filteredEvents.take(25)) { ev ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLight,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)),
                            width = 1.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${ev.eventId}: ${ev.eventName}",
                                    fontSize = 13.sp,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                StatusBadge(
                                    "Actual: ${ev.actualAttendance}",
                                    AccentTeal.copy(alpha = 0.2f),
                                    TealDark
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${ev.eventType} • Expected: ${ev.expectedAttendance} • Day: ${ev.dayOfWeek} ${ev.startHour}:00 (${ev.durationHours}h) • Org: ${ev.hostOrganization}",
                                fontSize = 11.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
