package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeasibilityResult
import com.example.data.model.Room
import com.example.ui.CampusSpaceUiState
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFinderScreen(
    uiState: CampusSpaceUiState,
    onSearch: (
        day: String,
        startTime: String,
        endTime: String,
        attendees: Int,
        roomType: String,
        projector: Boolean,
        computers: Boolean,
        accessible: Boolean,
        building: String
    ) -> Unit,
    onBookRoom: (room: Room, day: String, startTime: String, endTime: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf(uiState.searchDay) }
    var selectedStartTime by remember { mutableStateOf(uiState.searchStartTime) }
    var selectedEndTime by remember { mutableStateOf(uiState.searchEndTime) }
    var attendees by remember { mutableIntStateOf(uiState.searchAttendees) }
    var selectedRoomType by remember { mutableStateOf(uiState.searchRoomType) }
    var needProjector by remember { mutableStateOf(uiState.searchProjector) }
    var needComputers by remember { mutableStateOf(uiState.searchComputers) }
    var needAccessible by remember { mutableStateOf(uiState.searchAccessible) }
    var selectedBuilding by remember { mutableStateOf(uiState.searchBuilding) }

    var showRejectedRooms by remember { mutableStateOf(false) }
    var expandedExplanationIndex by remember { mutableIntStateOf(0) }
    var roomToConfirm by remember { mutableStateOf<Room?>(null) }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val timeSlots = listOf(
        "08:00" to "09:30",
        "09:30" to "11:00",
        "10:00" to "11:30",
        "11:30" to "13:00",
        "13:00" to "14:30",
        "14:00" to "15:30",
        "15:30" to "17:00",
        "17:00" to "18:30",
        "18:30" to "20:00"
    )
    val roomTypes = listOf("Any", "Classroom", "Lecture Hall", "Computer Lab", "Science Lab", "Seminar Room", "Conference Room")
    val buildings = listOf("Any", "Engineering Complex", "Science Hall", "Computing Center", "Business Tower", "Arts Pavilion", "University Library")
    val capacityPresets = listOf(15 to "Huddle", 30 to "Seminar", 60 to "Class", 120 to "Hall", 200 to "Auditorium")

    // Confirmation Dialog
    roomToConfirm?.let { room ->
        AlertDialog(
            onDismissRequest = { roomToConfirm = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceLight,
            title = {
                Text(
                    text = "Confirm Space Reservation",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "You are committing an immediate reservation in the live schedule:",
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaFontFamily,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = SurfaceVariantLight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = room.roomId,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${room.building}",
                                    fontFamily = PlusJakartaFontFamily,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${room.roomType} • Floor ${room.floor} • Cap: ${room.capacity} seats",
                                fontSize = 11.sp,
                                fontFamily = PlusJakartaFontFamily,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Slot: $selectedDay [$selectedStartTime - $selectedEndTime)",
                                fontSize = 12.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "This will update the in-memory timetable and prevent conflicts with future search queries and batch optimization runs.",
                        fontSize = 11.sp,
                        fontFamily = PlusJakartaFontFamily,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBookRoom(room, selectedDay, selectedStartTime, selectedEndTime)
                        roomToConfirm = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text(
                        "Confirm Reservation",
                        color = DeepNavy,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { roomToConfirm = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Cancel",
                        color = TextSecondary,
                        fontFamily = PlusJakartaFontFamily
                    )
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Criteria Form Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_finder_form_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CardBorderLight, CardBorderLight)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        title = "Smart Space Evaluation & Finder",
                        subtitle = "Multi-criteria constraint validation against 5,000 schedule blocks"
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Day of Week Chips
                    Text(
                        text = "Day of Week",
                        fontSize = 12.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(days) { d ->
                            ModernFilterChip(
                                text = d,
                                isSelected = selectedDay == d,
                                onClick = { selectedDay = d }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Time Slot Chips
                    Text(
                        text = "Time Window [Start, End)",
                        fontSize = 12.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(timeSlots) { slot ->
                            val isSelected = selectedStartTime == slot.first && selectedEndTime == slot.second
                            ModernFilterChip(
                                text = "${slot.first} - ${slot.second}",
                                isSelected = isSelected,
                                onClick = {
                                    selectedStartTime = slot.first
                                    selectedEndTime = slot.second
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Expected Attendance Slider & Quick Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Expected Attendees",
                            fontSize = 12.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$attendees People",
                            fontSize = 14.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AccentTeal
                        )
                    }
                    Slider(
                        value = attendees.toFloat(),
                        onValueChange = { attendees = it.toInt() },
                        valueRange = 5f..200f,
                        steps = 38,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentTeal,
                            activeTrackColor = AccentTeal,
                            inactiveTrackColor = SurfaceVariantLight
                        )
                    )

                    // Quick presets
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(capacityPresets) { (cap, label) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (attendees == cap) SoftCyan else SurfaceVariantLight,
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = if (attendees == cap) {
                                        Brush.horizontalGradient(listOf(AccentTeal, AccentTeal))
                                    } else {
                                        Brush.horizontalGradient(listOf(CardBorderLight, CardBorderLight))
                                    },
                                    width = 1.dp
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { attendees = cap }
                            ) {
                                Text(
                                    text = "$label ($cap)",
                                    fontSize = 11.sp,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = if (attendees == cap) FontWeight.Bold else FontWeight.Medium,
                                    color = if (attendees == cap) DeepNavy else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preferred Complex / Building
                    Text(
                        text = "Campus Complex / Building",
                        fontSize = 12.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(buildings) { bldg ->
                            ModernFilterChip(
                                text = bldg,
                                isSelected = selectedBuilding == bldg,
                                onClick = { selectedBuilding = bldg }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Room Type Requirement
                    Text(
                        text = "Room Type Requirement",
                        fontSize = 12.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(roomTypes) { rt ->
                            ModernFilterChip(
                                text = rt,
                                isSelected = selectedRoomType == rt,
                                onClick = { selectedRoomType = rt }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mandatory Facility Toggles
                    Text(
                        text = "Mandatory Equipment (Hard Constraints)",
                        fontSize = 12.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernFilterChip(
                            text = "Projector",
                            isSelected = needProjector,
                            onClick = { needProjector = !needProjector },
                            icon = Icons.Default.Videocam
                        )
                        ModernFilterChip(
                            text = "PC Lab",
                            isSelected = needComputers,
                            onClick = { needComputers = !needComputers },
                            icon = Icons.Default.Computer
                        )
                        ModernFilterChip(
                            text = "Accessible",
                            isSelected = needAccessible,
                            onClick = { needAccessible = !needAccessible },
                            icon = Icons.Default.Accessible
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Button
                    Button(
                        onClick = {
                            onSearch(
                                selectedDay,
                                selectedStartTime,
                                selectedEndTime,
                                attendees,
                                selectedRoomType,
                                needProjector,
                                needComputers,
                                needAccessible,
                                selectedBuilding
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("evaluate_spaces_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Evaluate Feasible Spaces",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Alternative Suggestions (if available)
        if (uiState.searchAlternatives.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SoftCyan,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(AccentTeal, ElectricCyan)),
                        width = 1.dp
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = TealDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Alternative Scheduling Suggestions",
                                fontSize = 14.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        uiState.searchAlternatives.forEach { alt ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    alt.title,
                                    fontSize = 12.sp,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                Text(
                                    alt.rationale,
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

        // Feasible Rooms Results
        val feasibleList = uiState.searchResults.filter { it.isFeasible }
        val rejectedList = uiState.searchResults.filter { !it.isFeasible }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Feasible Candidates (${feasibleList.size} of ${uiState.searchResults.size} rooms)",
                    fontSize = 16.sp,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
                if (rejectedList.isNotEmpty()) {
                    TextButton(onClick = { showRejectedRooms = !showRejectedRooms }) {
                        Text(
                            text = if (showRejectedRooms) "Hide Ineligible (${rejectedList.size})" else "Show Ineligible (${rejectedList.size})",
                            fontSize = 11.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AccentTeal
                        )
                    }
                }
            }
        }

        if (feasibleList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusWarningContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            "No Feasible Rooms for this Slot",
                            fontSize = 15.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = StatusWarningText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Every room in the campus network either has a class schedule conflict during [$selectedStartTime - $selectedEndTime), lacks mandatory equipment, or is below the $attendees attendee capacity threshold. Consult the Alternative Suggestions above to shift your session time or select a different day.",
                            fontSize = 12.sp,
                            fontFamily = PlusJakartaFontFamily,
                            color = TextPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        } else {
            itemsIndexed(feasibleList) { index, result ->
                FeasibleRoomCard(
                    rank = index + 1,
                    result = result,
                    isExpanded = (expandedExplanationIndex == index),
                    onToggleExpand = {
                        expandedExplanationIndex = if (expandedExplanationIndex == index) -1 else index
                    },
                    onBook = {
                        roomToConfirm = result.room
                    }
                )
            }
        }

        // Rejected Rooms Diagnostics Section (when enabled)
        if (showRejectedRooms && rejectedList.isNotEmpty()) {
            item {
                Text(
                    text = "Ineligible / Conflicted Rooms Diagnostics (${rejectedList.size})",
                    fontSize = 14.sp,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = StatusError
                )
            }
            items(rejectedList.take(15)) { rejected ->
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${rejected.room.roomId} (${rejected.room.building})",
                                fontSize = 13.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            StatusBadge("Ineligible", StatusErrorContainer, StatusErrorText)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            rejected.explanation,
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

@Composable
fun FeasibleRoomCard(
    rank: Int,
    result: FeasibilityResult,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onBook: () -> Unit,
    modifier: Modifier = Modifier
) {
    val room = result.room

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (rank == 1) AccentTeal else CardBorderLight,
                RoundedCornerShape(16.dp)
            )
            .testTag("feasible_room_card_${room.roomId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Rank, Room Code, Match Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (rank == 1) {
                                    Brush.horizontalGradient(listOf(AccentTeal, ElectricCyan))
                                } else {
                                    Brush.horizontalGradient(listOf(SlateNavy, SlateNavy))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            fontSize = 12.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (rank == 1) DeepNavy else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${room.roomId} • ${room.building}",
                            fontSize = 15.sp,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Text(
                            text = "${room.roomType} • Floor ${room.floor}",
                            fontSize = 11.sp,
                            fontFamily = PlusJakartaFontFamily,
                            color = TextSecondary
                        )
                    }
                }

                StatusBadge(
                    text = "${result.recommendationScore}/100 Match",
                    backgroundColor = if (result.recommendationScore >= 80) StatusSuccessContainer else StatusInfoContainer,
                    textColor = if (result.recommendationScore >= 80) StatusSuccessText else StatusInfoText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Capacity Utilization Progress Bar
            LinearUtilizationBar(
                percentage = result.utilizationPercent,
                label = "Seating Efficiency: ${room.capacity - result.capacityDifference}/${room.capacity} seats",
                detailText = "${result.capacityWaste} idle seats",
                barColor = if (result.isExactFit) StatusSuccess else AccentTeal
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Facility Badges Row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (room.hasProjector) StatusBadge("Projector", SoftCyan, TealDark)
                if (room.hasComputers) StatusBadge("PC Lab", SoftCyan, TealDark)
                if (room.isAccessible) StatusBadge("ADA Accessible", SoftCyan, TealDark)
                if (room.hasAudioSystem) StatusBadge("PA Audio", SurfaceVariantLight, TextSecondary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable Explainability Accordion
            AnimatedVisibility(visible = isExpanded) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceVariantLight.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Why this space was selected",
                                fontSize = 12.sp,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.explanation,
                            fontSize = 11.sp,
                            fontFamily = PlusJakartaFontFamily,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide Rationale ▲" else "Recommendation Rationale ▼",
                        fontSize = 11.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        color = SlateNavy,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("reserve_space_button_${room.roomId}")
                ) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = DeepNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Reserve Space",
                        fontSize = 12.sp,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }
            }
        }
    }
}
