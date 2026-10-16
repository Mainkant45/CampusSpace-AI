package com.example.engine

import com.example.data.model.ClassSchedule
import com.example.data.model.EventRecord
import com.example.data.model.Room
import com.example.data.model.RoomRequest
import com.example.data.model.SimulationConfig
import com.example.data.model.SimulationResult
import java.util.Random

class DigitalTwinSimulator(
    private val optimizer: GlobalAllocationOptimizer = GlobalAllocationOptimizer()
) {

    /**
     * Runs hypothetical what-if scenario simulations in an isolated sandbox.
     * Compares the simulated outcome against the standard baseline.
     */
    fun runSimulation(
        baseRequests: List<RoomRequest>,
        rooms: List<Room>,
        classSchedules: List<ClassSchedule>,
        confirmedEvents: List<EventRecord>,
        config: SimulationConfig
    ): SimulationResult {
        // 1. Run Baseline (No outages, standard demand)
        val baselineResult = optimizer.optimizeGlobal(
            requests = baseRequests,
            rooms = rooms,
            classSchedules = classSchedules,
            confirmedEvents = confirmedEvents
        )

        // 2. Synthesize Simulated Scenario Inputs (Isolated Sandbox)
        val rng = Random(1234L)
        val simulatedRequests = mutableListOf<RoomRequest>()

        // Apply attendance inflation to base requests
        for (req in baseRequests) {
            val inflatedAtt = (req.expectedAttendance * (1.0 + (config.attendanceInflationPercent / 100.0))).toInt()
            simulatedRequests.add(req.copy(expectedAttendance = maxOf(5, inflatedAtt)))
        }

        // Apply demand surge (generate hypothetical additional requests if surge > 0)
        if (config.demandSurgePercent > 0) {
            val additionalCount = ((baseRequests.size * (config.demandSurgePercent / 100.0))).toInt()
            for (i in 1..additionalCount) {
                val template = baseRequests[rng.nextInt(baseRequests.size)]
                val surgeReq = template.copy(
                    requestId = "HYP-SURGE-${template.requestId}-$i",
                    title = "[Simulation] ${template.title} (Surge Batch)",
                    priority = maxOf(1, template.priority)
                )
                simulatedRequests.add(surgeReq)
            }
        }

        // 3. Run Optimization under Stress Conditions
        val simulatedOptResult = optimizer.optimizeGlobal(
            requests = simulatedRequests,
            rooms = rooms,
            classSchedules = classSchedules,
            confirmedEvents = confirmedEvents,
            offlineRoomIds = config.offlineRoomIds,
            offlineBuildings = config.offlineBuildings
        )

        // 4. Calculate Differential Impact
        val baselineAccommodated = baselineResult.accommodatedCount
        val simulatedAccommodated = simulatedOptResult.accommodatedCount
        val displacedCount = maxOf(0, baselineAccommodated - simulatedAccommodated)

        // Analyze which buildings absorbed highest demand or faced bottlenecks
        val buildingCounts = mutableMapOf<String, Int>()
        for (assignment in simulatedOptResult.assignments) {
            val room = rooms.find { it.roomId == assignment.roomId }
            val bName = room?.building ?: "Unknown"
            buildingCounts[bName] = (buildingCounts[bName] ?: 0) + 1
        }
        val bottleneckBuildings = buildingCounts.toList().sortedByDescending { it.second }.take(4)

        // Track shifted assignments for base requests
        val baselineMap = baselineResult.assignments.associate { it.requestId to it.roomId }
        val simulatedMap = simulatedOptResult.assignments.associate { it.requestId to it.roomId }
        val changedAssignments = mutableListOf<Pair<String, String>>()

        for ((reqId, baseRoom) in baselineMap) {
            val simRoom = simulatedMap[reqId]
            if (simRoom != null && simRoom != baseRoom) {
                changedAssignments.add(reqId to "$baseRoom → $simRoom")
            } else if (simRoom == null) {
                changedAssignments.add(reqId to "$baseRoom → Waitlisted/Unallocated")
            }
        }

        val narrative = buildString {
            append("Digital Twin Simulation Analysis: ")
            if (config.offlineBuildings.isNotEmpty()) {
                append("Taking [${config.offlineBuildings.joinToString(", ")}] offline ")
            }
            if (config.offlineRoomIds.isNotEmpty()) {
                append("and disabling rooms [${config.offlineRoomIds.joinToString(", ")}] ")
            }
            if (config.demandSurgePercent > 0) {
                append("with a ${config.demandSurgePercent}% request demand surge ")
            }
            append("resulted in accommodating ${simulatedAccommodated} out of ${simulatedRequests.size} requests. ")
            if (displacedCount > 0) {
                append("A net displacement of $displacedCount baseline bookings occurred, ")
                append("with ${bottleneckBuildings.firstOrNull()?.first ?: "adjacent facilities"} absorbing maximum overflow.")
            } else {
                append("Campus space network absorbed stress gracefully without displacing existing baseline allocations.")
            }
        }

        return SimulationResult(
            config = config,
            baselineAccommodated = baselineAccommodated,
            simulatedAccommodated = simulatedAccommodated,
            baselineWaste = baselineResult.totalCapacityWasteSeats,
            simulatedWaste = simulatedOptResult.totalCapacityWasteSeats,
            displacedRequestCount = displacedCount,
            bottleneckBuildings = bottleneckBuildings,
            changedAssignments = changedAssignments.take(8),
            narrative = narrative
        )
    }
}
