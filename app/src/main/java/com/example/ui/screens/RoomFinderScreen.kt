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
            title = {
                Text(
                    text = "Confirm Reservation",
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "You are committing a real-time reservation for:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = SurfaceVariantLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${room.roomId} • ${room.building}",
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${room.roomType} • Floor ${room.floor} • Cap: ${room.capacity}",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Slot: $selectedDay [$selectedStartTime - $selectedEndTime)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentTeal
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This will immediately update the in-memory scheduling timetable and prevent conflicts with future search queries and batch optimization runs.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBookRoom(room, selectedDay, selectedStartTime, selectedEndTime)
                        roomToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("Confirm Booking", color = DeepNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { roomToConfirm = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Criteria Form Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_finder_form_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = "Smart Space Evaluation & Finder",
                        subtitle = "Multi-criteria constraint validation against 5,000 schedule records"
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Day of Week Chips
                    Text("Select Day of Week", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(days) { d ->
                            val isSelected = selectedDay == d
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDay = d },
                                label = { Text(d, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Time Slot Chips
                    Text("Select Time Window [Start, End)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(timeSlots) { slot ->
                            val isSelected = selectedStartTime == slot.first && selectedEndTime == slot.second
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedStartTime = slot.first
                                    selectedEndTime = slot.second
                                },
                                label = { Text("${slot.first} - ${slot.second}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SlateNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expected Attendance Slider & Quick Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Expected Attendees", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("$attendees People", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
                    }
                    Slider(
                        value = attendees.toFloat(),
                        onValueChange = { attendees = it.toInt() },
                        valueRange = 5f..200f,
                        steps = 38,
                        colors = SliderDefaults.colors(thumbColor = AccentTeal, activeTrackColor = AccentTeal)
                    )

                    // Quick presets
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(capacityPresets) { (cap, label) ->
                            SuggestionChip(
                                onClick = { attendees = cap },
                                label = { Text("$label ($cap)", fontSize = 10.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (attendees == cap) SoftCyan else SurfaceVariantLight
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preferred Complex / Building
                    Text("Campus Complex / Building", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(buildings) { bldg ->
                            FilterChip(
                                selected = selectedBuilding == bldg,
                                onClick = { selectedBuilding = bldg },
                                label = { Text(bldg, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SlateNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Room Type Requirement
                    Text("Room Type Requirement", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(roomTypes) { rt ->
                            FilterChip(
                                selected = selectedRoomType == rt,
                                onClick = { selectedRoomType = rt },
                                label = { Text(rt, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentTeal,
                                    selectedLabelColor = DeepNavy
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mandatory Facility Toggles
                    Text("Mandatory Facilities (Hard Constraints)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = needProjector,
                            onClick = { needProjector = !needProjector },
                            leadingIcon = { Icon(Icons.Default.Videocam, null, modifier = Modifier.size(14.dp)) },
                            label = { Text("Projector", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = needComputers,
                            onClick = { needComputers = !needComputers },
                            leadingIcon = { Icon(Icons.Default.Computer, null, modifier = Modifier.size(14.dp)) },
                            label = { Text("PC Lab", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = needAccessible,
                            onClick = { needAccessible = !needAccessible },
                            leadingIcon = { Icon(Icons.Default.Accessible, null, modifier = Modifier.size(14.dp)) },
                            label = { Text("Accessible", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                            .testTag("evaluate_spaces_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Evaluate Feasible Spaces", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Alternative Suggestions (if available)
        if (uiState.searchAlternatives.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftCyan),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AccentTeal))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = TealDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Alternative Scheduling Suggestions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.searchAlternatives.forEach { alt ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(alt.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                Text(alt.rationale, fontSize = 11.sp, color = TextSecondary)
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
                if (rejectedList.isNotEmpty()) {
                    TextButton(onClick = { showRejectedRooms = !showRejectedRooms }) {
                        Text(
                            text = if (showRejectedRooms) "Hide Ineligible (${rejectedList.size})" else "Show Ineligible (${rejectedList.size})",
                            fontSize = 11.sp,
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
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusWarningContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("No Feasible Rooms for this Slot", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Every room in the campus network either has a class schedule conflict during [$selectedStartTime - $selectedEndTime), lacks mandatory facilities, or is below the $attendees attendee capacity threshold. Consult the Alternative Suggestions above to shift your session time or select a different day.",
                            fontSize = 12.sp,
                            color = TextPrimary
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
                    fontWeight = FontWeight.Bold,
                    color = StatusError
                )
            }
            items(rejectedList.take(15)) { rejected ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderLight))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${rejected.room.roomId} (${rejected.room.building})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            StatusBadge("Ineligible", StatusErrorContainer, StatusError)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(rejected.explanation, fontSize = 11.sp, color = TextSecondary)
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
            .border(1.dp, if (rank == 1) AccentTeal else CardBorderLight, RoundedCornerShape(12.dp))
            .testTag("feasible_room_card_${room.roomId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Rank, Room Code, Match Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (rank == 1) AccentTeal else SlateNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rank == 1) DeepNavy else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${room.roomId} • ${room.building}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Text(
                            text = "${room.roomType} • Floor ${room.floor}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                StatusBadge(
                    text = "${result.recommendationScore}/100 Match",
                    backgroundColor = if (result.recommendationScore >= 80) StatusSuccessContainer else StatusInfoContainer,
                    textColor = if (result.recommendationScore >= 80) Color(0xFF065F46) else Color(0xFF1E40AF)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Capacity Utilization Progress Bar
            LinearUtilizationBar(
                percentage = result.utilizationPercent,
                label = "Seating Efficiency: ${room.capacity - result.capacityDifference}/${room.capacity} seats (${result.capacityWaste} idle seats)",
                barColor = if (result.isExactFit) StatusSuccess else AccentTeal
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariantLight)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Why this space was selected", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.explanation,
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                        text = if (isExpanded) "Hide Rationale ▲" else "View Recommendation Rationale ▼",
                        fontSize = 11.sp,
                        color = SlateNavy,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onBook,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("reserve_space_button_${room.roomId}")
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reserve Space", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                }
            }
        }
    }
}
