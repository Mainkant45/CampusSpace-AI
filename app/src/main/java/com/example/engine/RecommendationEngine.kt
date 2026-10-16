package com.example.engine

import com.example.data.model.AlternativeSuggestion
import com.example.data.model.ClassSchedule
import com.example.data.model.EventRecord
import com.example.data.model.FeasibilityResult
import com.example.data.model.Room
import com.example.data.model.TimeSlot
import kotlin.math.roundToInt

class RecommendationEngine(
    private val schedulingEngine: SchedulingEngine = SchedulingEngine()
) {

    /**
     * Evaluates all candidate rooms against request criteria, enforces hard constraints,
     * computes transparent multi-factor recommendation scores, and generates explainability traces.
     */
    fun evaluateRooms(
        rooms: List<Room>,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        expectedAttendance: Int,
        requiredRoomType: String?,
        requiresProjector: Boolean,
        requiresComputers: Boolean,
        requiresAccessible: Boolean,
        preferredBuilding: String? = null,
        classSchedules: List<ClassSchedule>,
        confirmedEvents: List<EventRecord>,
        bookedReservations: List<Pair<String, Pair<String, TimeSlot>>> = emptyList(),
        offlineRoomIds: Set<String> = emptySet(),
        offlineBuildings: Set<String> = emptySet()
    ): List<FeasibilityResult> {
        val targetSlot = TimeSlot.fromStrings(startTime, endTime)
        val results = mutableListOf<FeasibilityResult>()

        for (room in rooms) {
            // Check offline simulation status
            if (offlineRoomIds.contains(room.roomId) || offlineBuildings.contains(room.building)) {
                results.add(
                    FeasibilityResult(
                        room = room,
                        isFeasible = false,
                        recommendationScore = 0,
                        capacityDifference = room.capacity - expectedAttendance,
                        capacityWaste = maxOf(0, room.capacity - expectedAttendance),
                        utilizationPercent = 0,
                        matchedFacilities = emptyList(),
                        missingFacilities = emptyList(),
                        conflicts = listOf("Facility Offline: Room or building marked unavailable for maintenance"),
                        explanation = "Room ${room.roomId} in ${room.building} is currently offline in this scenario.",
                        isExactFit = false
                    )
                )
                continue
            }

            // 1. HARD CONSTRAINTS VERIFICATION
            val missingFacilities = mutableListOf<String>()
            val matchedFacilities = mutableListOf<String>()

            // Capacity constraint
            val capacityOk = room.capacity >= expectedAttendance

            // Facility constraints
            if (requiresProjector) {
                if (room.hasProjector) matchedFacilities.add("Projector")
                else missingFacilities.add("Projector")
            }
            if (requiresComputers) {
                if (room.hasComputers) matchedFacilities.add("Computer Workstations")
                else missingFacilities.add("Computer Workstations")
            }
            if (requiresAccessible) {
                if (room.isAccessible) matchedFacilities.add("ADA Accessibility")
                else missingFacilities.add("ADA Accessibility")
            }

            // Room type compatibility
            var typeOk = true
            if (!requiredRoomType.isNullOrBlank() && !requiredRoomType.equals("Any", ignoreCase = true)) {
                typeOk = room.roomType.equals(requiredRoomType, ignoreCase = true) ||
                        (requiredRoomType.equals("Classroom", ignoreCase = true) && room.roomType.equals("Lecture Hall", ignoreCase = true))
                if (!typeOk) {
                    missingFacilities.add("Room Type Mismatch (${room.roomType} != $requiredRoomType)")
                }
            }

            // Schedule conflict check
            val conflicts = schedulingEngine.findConflicts(
                roomId = room.roomId,
                dayOfWeek = dayOfWeek,
                targetSlot = targetSlot,
                classSchedules = classSchedules,
                confirmedEvents = confirmedEvents,
                bookedReservations = bookedReservations
            )

            val isFeasible = capacityOk && missingFacilities.isEmpty() && conflicts.isEmpty()

            // 2. TRANSPARENT MULTI-FACTOR SCORE (0 - 100)
            val capDifference = room.capacity - expectedAttendance
            val capWaste = maxOf(0, capDifference)
            val utilizationPct = if (room.capacity > 0) {
                ((expectedAttendance.toDouble() / room.capacity) * 100).roundToInt()
            } else 0

            var score = 0
            val isExactFit = capDifference in 0..10

            if (isFeasible) {
                // High utilization = higher score. Excess capacity is penalized to prevent wasting large lecture halls.
                val utilizationScore = (utilizationPct * 0.60).roundToInt() // Up to 60 pts
                val exactFitBonus = if (isExactFit) 15 else 0
                val buildingBonus = if (preferredBuilding != null && room.building.contains(preferredBuilding, ignoreCase = true)) 10 else 0
                val audioBonus = if (room.hasAudioSystem) 5 else 0
                val whiteboardBonus = if (room.hasWhiteboard) 5 else 0
                val accessibilityBonus = if (room.isAccessible) 5 else 0

                score = minOf(100, utilizationScore + exactFitBonus + buildingBonus + audioBonus + whiteboardBonus + accessibilityBonus)
            } else {
                score = 0
            }

            // 3. EXPLAINABILITY SYNTHESIS
            val explanation = if (isFeasible) {
                buildString {
                    append("Recommended: Room ${room.roomId} (${room.roomType}) has ${room.capacity} seats ")
                    append("which accommodates all $expectedAttendance attendees at ${utilizationPct}% space utilization. ")
                    if (isExactFit) append("Optimal capacity fit with minimal idle seats. ")
                    append("All mandatory facilities (${matchedFacilities.joinToString(", ").ifEmpty { "standard" }}) are present, ")
                    append("and no conflicting class schedules or events were detected for [$startTime - $endTime).")
                }
            } else {
                buildString {
                    append("Not feasible: ")
                    val reasons = mutableListOf<String>()
                    if (!capacityOk) {
                        reasons.add("capacity ${room.capacity} is less than required $expectedAttendance attendees")
                    }
                    if (missingFacilities.isNotEmpty()) {
                        reasons.add("lacks required ${missingFacilities.joinToString(", ")}")
                    }
                    if (conflicts.isNotEmpty()) {
                        reasons.add("conflicts with ${conflicts.size} scheduled session(s)")
                    }
                    append(reasons.joinToString("; "))
                    append(".")
                }
            }

            results.add(
                FeasibilityResult(
                    room = room,
                    isFeasible = isFeasible,
                    recommendationScore = score,
                    capacityDifference = capDifference,
                    capacityWaste = capWaste,
                    utilizationPercent = utilizationPct,
                    matchedFacilities = matchedFacilities,
                    missingFacilities = missingFacilities,
                    conflicts = conflicts,
                    explanation = explanation,
                    isExactFit = isExactFit
                )
            )
        }

        // Return sorted: feasible first by score descending, then non-feasible by capacity proximity
        return results.sortedWith(
            compareByDescending<FeasibilityResult> { it.isFeasible }
                .thenByDescending { it.recommendationScore }
                .thenBy { it.capacityWaste }
        )
    }

    /**
     * Generates alternative options when preferred rooms or slots are unavailable
     */
    fun generateAlternatives(
        evaluatedRooms: List<FeasibilityResult>,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        expectedAttendance: Int,
        classSchedules: List<ClassSchedule>,
        confirmedEvents: List<EventRecord>,
        bookedReservations: List<Pair<String, Pair<String, TimeSlot>>> = emptyList()
    ): List<AlternativeSuggestion> {
        val alternatives = mutableListOf<AlternativeSuggestion>()
        val targetSlot = TimeSlot.fromStrings(startTime, endTime)
        val durationM = targetSlot.endMinutes - targetSlot.startMinutes

        // 1. Look for near-capacity rooms in other buildings
        val feasibleRooms = evaluatedRooms.filter { it.isFeasible }
        if (feasibleRooms.isNotEmpty()) {
            val runnerUp = feasibleRooms.drop(1).firstOrNull()
            if (runnerUp != null) {
                alternatives.add(
                    AlternativeSuggestion(
                        title = "Alternative Facility: ${runnerUp.room.roomId}",
                        description = "${runnerUp.room.building} • ${runnerUp.room.capacity} seats",
                        suggestedRoomId = runnerUp.room.roomId,
                        suggestedTimeSlot = "$startTime - $endTime",
                        rationale = "Available at the requested time with ${runnerUp.utilizationPercent}% utilization."
                    )
                )
            }
        }

        // 2. Look for non-conflicting time slots for high-capacity rooms that failed due to time conflict
        val conflictedRooms = evaluatedRooms.filter { !it.isFeasible && it.capacityDifference >= 0 && it.conflicts.isNotEmpty() }
        for (cand in conflictedRooms.take(2)) {
            val freeSlots = schedulingEngine.findAlternativeTimeSlots(
                roomId = cand.room.roomId,
                dayOfWeek = dayOfWeek,
                requestedDurationMinutes = durationM,
                classSchedules = classSchedules,
                confirmedEvents = confirmedEvents,
                bookedReservations = bookedReservations
            )
            if (freeSlots.isNotEmpty()) {
                val nextSlot = freeSlots.first()
                val sStr = TimeSlot.minutesToTimeString(nextSlot.startMinutes)
                val eStr = TimeSlot.minutesToTimeString(nextSlot.endMinutes)
                alternatives.add(
                    AlternativeSuggestion(
                        title = "Time Shift: ${cand.room.roomId} at [$sStr - $eStr)",
                        description = "${cand.room.building} (${cand.room.capacity} seats)",
                        suggestedRoomId = cand.room.roomId,
                        suggestedTimeSlot = "$sStr - $eStr",
                        rationale = "Shift timing by ${nextSlot.startMinutes - targetSlot.startMinutes} mins to bypass ${cand.conflicts.firstOrNull()?.take(40) ?: "classes"}."
                    )
                )
            }
        }

        return alternatives
    }
}
