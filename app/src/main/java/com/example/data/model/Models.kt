package com.example.data.model

data class Room(
    val roomId: String,
    val building: String,
    val floor: Int,
    val roomType: String,
    val capacity: Int,
    val hasProjector: Boolean,
    val hasComputers: Boolean,
    val isAccessible: Boolean,
    val hasAudioSystem: Boolean,
    val hasWhiteboard: Boolean
)

data class ClassSchedule(
    val scheduleId: String,
    val courseCode: String,
    val courseName: String,
    val department: String,
    val roomId: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val enrolledStudents: Int
) {
    val timeSlot: TimeSlot by lazy { TimeSlot.fromStrings(startTime, endTime) }
}

data class RoomRequest(
    val requestId: String,
    val requesterRole: String,
    val organizationName: String,
    val title: String,
    val requestedDate: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val expectedAttendance: Int,
    val requiredRoomType: String,
    val requiresProjector: Boolean,
    val requiresComputers: Boolean,
    val requiresAccessible: Boolean,
    val priority: Int,
    val status: String,
    val assignedRoomId: String? = null
)

data class EventRecord(
    val eventId: String,
    val eventName: String,
    val eventType: String,
    val date: String,
    val dayOfWeek: String,
    val startHour: Int,
    val durationHours: Double,
    val roomId: String,
    val expectedAttendance: Int,
    val actualAttendance: Int,
    val hostOrganization: String
) {
    val timeSlot: TimeSlot by lazy {
        val s = startHour * 60
        val e = (startHour * 60 + durationHours * 60).toInt()
        TimeSlot(s, e)
    }
}

data class TimeSlot(
    val startMinutes: Int,
    val endMinutes: Int
) {
    companion object {
        fun fromStrings(start: String, end: String): TimeSlot {
            return TimeSlot(parseToMinutes(start), parseToMinutes(end))
        }

        fun parseToMinutes(timeStr: String): Int {
            val parts = timeStr.trim().split(":")
            if (parts.size >= 2) {
                val h = parts[0].toIntOrNull() ?: 0
                val m = parts[1].toIntOrNull() ?: 0
                return h * 60 + m
            }
            return 0
        }

        fun minutesToTimeString(minutes: Int): String {
            val h = (minutes / 60) % 24
            val m = minutes % 60
            return "%02d:%02d".format(h, m)
        }
    }

    /**
     * Strict half-open interval overlap check [start, end).
     * Two intervals [s1, e1) and [s2, e2) overlap iff max(s1, s2) < min(e1, e2).
     */
    fun overlapsWith(other: TimeSlot): Boolean {
        val overlapStart = maxOf(this.startMinutes, other.startMinutes)
        val overlapEnd = minOf(this.endMinutes, other.endMinutes)
        return overlapStart < overlapEnd
    }
}

data class FeasibilityResult(
    val room: Room,
    val isFeasible: Boolean,
    val recommendationScore: Int, // 0 - 100
    val capacityDifference: Int, // room.capacity - requestedAttendance
    val capacityWaste: Int,
    val utilizationPercent: Int,
    val matchedFacilities: List<String>,
    val missingFacilities: List<String>,
    val conflicts: List<String>,
    val explanation: String,
    val isExactFit: Boolean
)

data class AlternativeSuggestion(
    val title: String,
    val description: String,
    val suggestedRoomId: String?,
    val suggestedTimeSlot: String?,
    val rationale: String
)

data class AllocationAssignment(
    val requestId: String,
    val roomId: String,
    val organizationName: String,
    val title: String,
    val dayOfWeek: String,
    val timeSlot: String,
    val priority: Int,
    val expectedAttendance: Int,
    val roomCapacity: Int,
    val capacityUtilizationPct: Int,
    val algorithmDecision: String
)

data class OptimizationResult(
    val algorithmName: String,
    val totalRequestsEvaluated: Int,
    val accommodatedCount: Int,
    val unallocatedCount: Int,
    val totalCapacityWasteSeats: Int,
    val prioritySatisfactionPct: Double,
    val runtimeMs: Long,
    val assignments: List<AllocationAssignment>,
    val unallocatedRequests: List<RoomRequest>,
    val optimizationSummary: String
)

data class SimulationConfig(
    val demandSurgePercent: Int = 0,
    val offlineRoomIds: Set<String> = emptySet(),
    val offlineBuildings: Set<String> = emptySet(),
    val attendanceInflationPercent: Int = 0,
    val minPriorityAllowed: Int = 1
)

data class SimulationResult(
    val config: SimulationConfig,
    val baselineAccommodated: Int,
    val simulatedAccommodated: Int,
    val baselineWaste: Int,
    val simulatedWaste: Int,
    val displacedRequestCount: Int,
    val bottleneckBuildings: List<Pair<String, Int>>,
    val changedAssignments: List<Pair<String, String>>, // requestId to "RoomA -> RoomB"
    val narrative: String
)

data class MLModelMetrics(
    val modelType: String = "Multiple Linear Regression with Ridge (L2)",
    val formulaExplanation: String = "y_hat = w0 + w1*expected_att + w2*start_hour + w3*duration + w4*is_weekend + sum(w_type * 1_type)",
    val totalSamples: Int,
    val trainCount: Int,
    val valCount: Int,
    val testCount: Int,
    val trainMae: Double,
    val trainRmse: Double,
    val trainR2: Double,
    val valMae: Double,
    val valRmse: Double,
    val testMae: Double,
    val testRmse: Double,
    val testR2: Double,
    val baselineMae: Double,
    val baselineRmse: Double,
    val baselineR2: Double,
    val featureWeights: Map<String, Double>,
    val biasWeight: Double,
    val trainingLossHistory: List<Double>
)

data class AttendancePredictionResult(
    val expectedAttendance: Int,
    val predictedActualAttendance: Int,
    val estimatedDifference: Int,
    val turnoutRatioPct: Int,
    val confidenceLower: Int,
    val confidenceUpper: Int,
    val explanationFactors: List<String>
)

data class DatasetAuditSummary(
    val roomsCount: Int,
    val classScheduleCount: Int,
    val roomRequestsCount: Int,
    val eventsCount: Int,
    val uniqueBuildings: Int,
    val uniqueDepartments: Int,
    val foreignKeyIntegrityPassed: Boolean,
    val missingValuesDetected: Int,
    val duplicateRecordsDetected: Int,
    val notes: List<String>
)
