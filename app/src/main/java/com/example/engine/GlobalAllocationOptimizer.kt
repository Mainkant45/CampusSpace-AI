package com.example.engine

import com.example.data.model.AllocationAssignment
import com.example.data.model.ClassSchedule
import com.example.data.model.EventRecord
import com.example.data.model.OptimizationResult
import com.example.data.model.Room
import com.example.data.model.RoomRequest
import com.example.data.model.TimeSlot
import kotlin.math.roundToInt

class GlobalAllocationOptimizer(
    private val schedulingEngine: SchedulingEngine = SchedulingEngine()
) {

    /**
     * Executes the Greedy Baseline Allocation algorithm:
     * Iterates through requests in order of submission and assigns the first valid room.
     */
    fun optimizeGreedy(
        requests: List<RoomRequest>,
        rooms: List<Room>,
        classSchedules: List<ClassSchedule>,
        confirmedEvents: List<EventRecord>,
        offlineRoomIds: Set<String> = emptySet(),
        offlineBuildings: Set<String> = emptySet()
    ): OptimizationResult {
        val startTime = System.currentTimeMillis()
        val availableRooms = rooms.filter { !offlineRoomIds.contains(it.roomId) && !offlineBuildings.contains(it.building) }

        val assignedBookings = mutableListOf<Pair<String, Pair<String, TimeSlot>>>()
        val assignments = mutableListOf<AllocationAssignment>()
        val unallocated = mutableListOf<RoomRequest>()
        var totalCapacityWaste = 0

        for (req in requests) {
            val reqSlot = TimeSlot.fromStrings(req.startTime, req.endTime)
            var chosenRoom: Room? = null

            for (room in availableRooms) {
                // Hard constraints
                if (room.capacity < req.expectedAttendance) continue
                if (req.requiresProjector && !room.hasProjector) continue
                if (req.requiresComputers && !room.hasComputers) continue
                if (req.requiresAccessible && !room.isAccessible) continue
                if (req.requiredRoomType.isNotEmpty() && !req.requiredRoomType.equals("Any", ignoreCase = true) &&
                    !room.roomType.equals(req.requiredRoomType, ignoreCase = true) &&
                    !(req.requiredRoomType.equals("Classroom", ignoreCase = true) && room.roomType.equals("Lecture Hall", ignoreCase = true))
                ) continue

                // Check conflict against schedule + already assigned batch bookings
                val isFree = schedulingEngine.isRoomAvailable(
                    roomId = room.roomId,
                    dayOfWeek = req.dayOfWeek,
                    targetSlot = reqSlot,
                    classSchedules = classSchedules,
                    confirmedEvents = confirmedEvents,
                    bookedReservations = assignedBookings
                )

                if (isFree) {
                    chosenRoom = room
                    break // Greedy: takes the first match
                }
            }

            if (chosenRoom != null) {
                assignedBookings.add(chosenRoom.roomId to (req.dayOfWeek to reqSlot))
                val waste = chosenRoom.capacity - req.expectedAttendance
                totalCapacityWaste += maxOf(0, waste)
                val utilPct = ((req.expectedAttendance.toDouble() / chosenRoom.capacity) * 100).roundToInt()

                assignments.add(
                    AllocationAssignment(
                        requestId = req.requestId,
                        roomId = chosenRoom.roomId,
                        organizationName = req.organizationName,
                        title = req.title,
                        dayOfWeek = req.dayOfWeek,
                        timeSlot = "${req.startTime} - ${req.endTime}",
                        priority = req.priority,
                        expectedAttendance = req.expectedAttendance,
                        roomCapacity = chosenRoom.capacity,
                        capacityUtilizationPct = utilPct,
                        algorithmDecision = "Greedy First-Fit Match"
                    )
                )
            } else {
                unallocated.add(req)
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        val accommodatedPrioSum = assignments.sumOf { it.priority }
        val totalPrioSum = requests.sumOf { it.priority }.coerceAtLeast(1)
        val prioSatisfactionPct = (accommodatedPrioSum.toDouble() / totalPrioSum) * 100.0

        return OptimizationResult(
            algorithmName = "Greedy First-Fit Baseline",
            totalRequestsEvaluated = requests.size,
            accommodatedCount = assignments.size,
            unallocatedCount = unallocated.size,
            totalCapacityWasteSeats = totalCapacityWaste,
            prioritySatisfactionPct = (prioSatisfactionPct * 10).roundToInt() / 10.0,
            runtimeMs = elapsed,
            assignments = assignments,
            unallocatedRequests = unallocated,
            optimizationSummary = "Sequential first-fit assignment without global constraint awareness. Susceptible to premature allocation of oversized lecture halls."
        )
    }

    /**
     * Executes the Global Constraint-Aware Allocation Optimizer:
     * 1. Multi-variable sorting: High priority requests (5 down to 1), then tightest feasible candidate set (Most Constrained Variable).
     * 2. Best-Fit Minimal Waste selection: Selects the room that minimizes idle seating capacity while strictly preserving all hard constraints.
     * 3. Global Interval Partitioning: Zero temporal collisions between batch requests.
     */
    fun optimizeGlobal(
        requests: List<RoomRequest>,
        rooms: List<Room>,
        classSchedules: List<ClassSchedule>,
        confirmedEvents: List<EventRecord>,
        offlineRoomIds: Set<String> = emptySet(),
        offlineBuildings: Set<String> = emptySet()
    ): OptimizationResult {
        val startTime = System.currentTimeMillis()
        val availableRooms = rooms.filter { !offlineRoomIds.contains(it.roomId) && !offlineBuildings.contains(it.building) }

        // Find candidate feasible rooms for each request to determine constraint tightness
        val candidateMap = mutableMapOf<String, List<Room>>()
        for (req in requests) {
            val valid = availableRooms.filter { room ->
                room.capacity >= req.expectedAttendance &&
                        (!req.requiresProjector || room.hasProjector) &&
                        (!req.requiresComputers || room.hasComputers) &&
                        (!req.requiresAccessible || room.isAccessible) &&
                        (req.requiredRoomType.isEmpty() || req.requiredRoomType.equals("Any", ignoreCase = true) ||
                                room.roomType.equals(req.requiredRoomType, ignoreCase = true) ||
                                (req.requiredRoomType.equals("Classroom", ignoreCase = true) && room.roomType.equals("Lecture Hall", ignoreCase = true)))
            }
            candidateMap[req.requestId] = valid
        }

        // Sort requests: Highest Priority first (5..1), then fewest feasible candidates (MRV)
        val sortedRequests = requests.sortedWith(
            compareByDescending<RoomRequest> { it.priority }
                .thenBy { candidateMap[it.requestId]?.size ?: 0 }
                .thenByDescending { it.expectedAttendance }
        )

        val assignedBookings = mutableListOf<Pair<String, Pair<String, TimeSlot>>>()
        val assignments = mutableListOf<AllocationAssignment>()
        val unallocated = mutableListOf<RoomRequest>()
        var totalCapacityWaste = 0

        for (req in sortedRequests) {
            val reqSlot = TimeSlot.fromStrings(req.startTime, req.endTime)
            val candidates = candidateMap[req.requestId] ?: emptyList()

            // Filter out rooms with scheduling conflicts
            val availableCandidates = candidates.filter { room ->
                schedulingEngine.isRoomAvailable(
                    roomId = room.roomId,
                    dayOfWeek = req.dayOfWeek,
                    targetSlot = reqSlot,
                    classSchedules = classSchedules,
                    confirmedEvents = confirmedEvents,
                    bookedReservations = assignedBookings
                )
            }

            // OPTIMIZATION OBJECTIVE:
            // Pick room with minimum capacity waste (capacity - expectedAttendance)
            // Tie-break with facility match affinity
            val optimalRoom = availableCandidates.minByOrNull { room ->
                val waste = room.capacity - req.expectedAttendance
                val facilityBonus = (if (room.hasAudioSystem) -5 else 0) + (if (room.isAccessible) -5 else 0)
                waste + facilityBonus
            }

            if (optimalRoom != null) {
                assignedBookings.add(optimalRoom.roomId to (req.dayOfWeek to reqSlot))
                val waste = optimalRoom.capacity - req.expectedAttendance
                totalCapacityWaste += maxOf(0, waste)
                val utilPct = ((req.expectedAttendance.toDouble() / optimalRoom.capacity) * 100).roundToInt()

                assignments.add(
                    AllocationAssignment(
                        requestId = req.requestId,
                        roomId = optimalRoom.roomId,
                        organizationName = req.organizationName,
                        title = req.title,
                        dayOfWeek = req.dayOfWeek,
                        timeSlot = "${req.startTime} - ${req.endTime}",
                        priority = req.priority,
                        expectedAttendance = req.expectedAttendance,
                        roomCapacity = optimalRoom.capacity,
                        capacityUtilizationPct = utilPct,
                        algorithmDecision = "Optimal Global Fit (Waste: $waste seats)"
                    )
                )
            } else {
                unallocated.add(req)
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        val accommodatedPrioSum = assignments.sumOf { it.priority }
        val totalPrioSum = requests.sumOf { it.priority }.coerceAtLeast(1)
        val prioSatisfactionPct = (accommodatedPrioSum.toDouble() / totalPrioSum) * 100.0

        return OptimizationResult(
            algorithmName = "Global Constraint & Waste Optimizer",
            totalRequestsEvaluated = requests.size,
            accommodatedCount = assignments.size,
            unallocatedCount = unallocated.size,
            totalCapacityWasteSeats = totalCapacityWaste,
            prioritySatisfactionPct = (prioSatisfactionPct * 10).roundToInt() / 10.0,
            runtimeMs = elapsed,
            assignments = assignments,
            unallocatedRequests = unallocated,
            optimizationSummary = "Global multi-constraint optimization. Evaluates priority vectors, MRV domain tightness, and minimizes idle capacity waste across the campus network."
        )
    }
}
