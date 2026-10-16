package com.example.engine

import com.example.data.model.ClassSchedule
import com.example.data.model.EventRecord
import com.example.data.model.TimeSlot

class SchedulingEngine {

    // Cached fast-lookup index to avoid linear scanning 5,000 schedules on every room query
    private var cachedClassesRef: List<ClassSchedule>? = null
    private var cachedEventsRef: List<EventRecord>? = null
    private var classesIndex: Map<Pair<String, String>, List<ClassSchedule>> = emptyMap()
    private var eventsIndex: Map<Pair<String, String>, List<EventRecord>> = emptyMap()

    private fun ensureIndex(classSchedules: List<ClassSchedule>, confirmedEvents: List<EventRecord>) {
        if (cachedClassesRef !== classSchedules) {
            classesIndex = classSchedules.groupBy { it.roomId.lowercase() to it.dayOfWeek.lowercase() }
            cachedClassesRef = classSchedules
        }
        if (cachedEventsRef !== confirmedEvents) {
            eventsIndex = confirmedEvents.groupBy { it.roomId.lowercase() to it.dayOfWeek.lowercase() }
            cachedEventsRef = confirmedEvents
        }
    }

    /**
     * Checks if a target room is occupied at the requested time slot on the given day.
     * Evaluates against both fixed academic class schedules and confirmed events.
     * Strict half-open interval rule: [s1, e1) and [s2, e2) conflict iff max(s1, s2) < min(e1, e2).
     */
    fun findConflicts(
        roomId: String,
        dayOfWeek: String,
        targetSlot: TimeSlot,
        classSchedules: List<ClassSchedule>,
        confirmedEvents: List<EventRecord>,
        bookedReservations: List<Pair<String, Pair<String, TimeSlot>>> = emptyList() // (roomId, (day, slot))
    ): List<String> {
        ensureIndex(classSchedules, confirmedEvents)
        val key = roomId.lowercase() to dayOfWeek.lowercase()
        val conflicts = mutableListOf<String>()

        // 1. Fast indexed check against Class Schedule (O(1) bucket with ~2-4 classes)
        val relevantClasses = classesIndex[key]
        if (relevantClasses != null) {
            for (sched in relevantClasses) {
                if (targetSlot.overlapsWith(sched.timeSlot)) {
                    conflicts.add(
                        "Class conflict: ${sched.courseCode} (${sched.courseName}) " +
                                "scheduled [${sched.startTime} - ${sched.endTime}) in ${sched.department}"
                    )
                }
            }
        }

        // 2. Fast indexed check against Confirmed Events (O(1) bucket with ~0-2 events)
        val relevantEvents = eventsIndex[key]
        if (relevantEvents != null) {
            for (event in relevantEvents) {
                if (targetSlot.overlapsWith(event.timeSlot)) {
                    val sTime = TimeSlot.minutesToTimeString(event.timeSlot.startMinutes)
                    val eTime = TimeSlot.minutesToTimeString(event.timeSlot.endMinutes)
                    conflicts.add(
                        "Event conflict: '${event.eventName}' hosted by ${event.hostOrganization} " +
                                "confirmed [${sTime} - ${eTime})"
                    )
                }
            }
        }

        // 3. Check against User-Booked / Dynamic Reservations
        for (booking in bookedReservations) {
            val (bRoom, daySlot) = booking
            val (bDay, bSlot) = daySlot
            if (bRoom.equals(roomId, ignoreCase = true) && bDay.equals(dayOfWeek, ignoreCase = true)) {
                if (targetSlot.overlapsWith(bSlot)) {
                    val sTime = TimeSlot.minutesToTimeString(bSlot.startMinutes)
                    val eTime = TimeSlot.minutesToTimeString(bSlot.endMinutes)
                    conflicts.add("Active booking conflict: Confirmed reservation at [$sTime - $eTime)")
                }
            }
        }

        return conflicts
    }

    /**
     * Checks if a room is completely available for the given slot.
     */
    fun isRoomAvailable(
        roomId: String,
        dayOfWeek: String,
        targetSlot: TimeSlot,
        classSchedules: List<ClassSchedule>,
        confirmedEvents: List<EventRecord>,
        bookedReservations: List<Pair<String, Pair<String, TimeSlot>>> = emptyList()
    ): Boolean {
        return findConflicts(roomId, dayOfWeek, targetSlot, classSchedules, confirmedEvents, bookedReservations).isEmpty()
    }

    /**
     * Finds next available non-conflicting time slots for a specific room on a given day.
     */
    fun findAlternativeTimeSlots(
        roomId: String,
        dayOfWeek: String,
        requestedDurationMinutes: Int,
        classSchedules: List<ClassSchedule>,
        confirmedEvents: List<EventRecord>,
        bookedReservations: List<Pair<String, Pair<String, TimeSlot>>> = emptyList()
    ): List<TimeSlot> {
        val alternatives = mutableListOf<TimeSlot>()
        val startOfDay = 8 * 60 // 08:00
        val endOfDay = 21 * 60  // 21:00

        var currentStart = startOfDay
        while (currentStart + requestedDurationMinutes <= endOfDay && alternatives.size < 4) {
            val candidate = TimeSlot(currentStart, currentStart + requestedDurationMinutes)
            if (isRoomAvailable(roomId, dayOfWeek, candidate, classSchedules, confirmedEvents, bookedReservations)) {
                alternatives.add(candidate)
                currentStart += requestedDurationMinutes + 30 // step forward
            } else {
                currentStart += 30
            }
        }
        return alternatives
    }
}
