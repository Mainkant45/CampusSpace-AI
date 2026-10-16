package com.example.analytics

import com.example.data.model.ClassSchedule
import com.example.data.model.EventRecord
import com.example.data.model.Room
import com.example.data.model.RoomRequest
import com.example.data.model.TimeSlot
import kotlin.math.roundToInt

data class BuildingUtilization(
    val buildingName: String,
    val roomCount: Int,
    val totalCapacity: Int,
    val totalScheduledMinutesPerWeek: Int,
    val utilizationPercent: Int
)

data class TimeSlotDemand(
    val slotLabel: String,
    val requestCount: Int,
    val percentage: Int
)

data class DayDemand(
    val dayOfWeek: String,
    val requestCount: Int,
    val percentage: Int
)

data class RoleDemand(
    val roleName: String,
    val count: Int,
    val percentage: Int
)

data class RoomUtilizationMetric(
    val roomId: String,
    val building: String,
    val roomType: String,
    val capacity: Int,
    val scheduledHoursPerWeek: Double,
    val utilizationPercent: Int,
    val status: String // "High Demand", "Normal", "Underutilized"
)

data class DemandIntelligenceReport(
    val totalRooms: Int,
    val totalClasses: Int,
    val totalRequests: Int,
    val totalEvents: Int,
    val averageScheduledUtilizationPct: Int,
    val peakDemandSlot: String,
    val peakDemandDay: String,
    val buildingUtilizations: List<BuildingUtilization>,
    val timeSlotDemands: List<TimeSlotDemand>,
    val dayDemands: List<DayDemand>,
    val roleDemands: List<RoleDemand>,
    val topUnderutilizedRooms: List<RoomUtilizationMetric>,
    val topHighDemandRooms: List<RoomUtilizationMetric>,
    val educationalCaveat: String
)

class DemandIntelligence {

    fun computeReport(
        rooms: List<Room>,
        classSchedules: List<ClassSchedule>,
        roomRequests: List<RoomRequest>,
        events: List<EventRecord>
    ): DemandIntelligenceReport {
        // Operational baseline: 13 hours/day (08:00 to 21:00) * 6 days = 78 hours/week = 4,680 minutes/week per room
        val totalWeeklyAvailableMinutesPerRoom = 78 * 60

        // 1. Compute per-room scheduled minutes from class_schedule.csv
        val roomScheduledMinutes = mutableMapOf<String, Int>()
        for (sched in classSchedules) {
            val slot = TimeSlot.fromStrings(sched.startTime, sched.endTime)
            val durationM = maxOf(0, slot.endMinutes - slot.startMinutes)
            roomScheduledMinutes[sched.roomId] = (roomScheduledMinutes[sched.roomId] ?: 0) + durationM
        }

        // Room Utilization list
        val roomMetrics = rooms.map { room ->
            val minutes = roomScheduledMinutes[room.roomId] ?: 0
            val hours = (minutes / 60.0 * 10).roundToInt() / 10.0
            val utilPct = ((minutes.toDouble() / totalWeeklyAvailableMinutesPerRoom) * 100).roundToInt().coerceIn(0, 100)
            val status = when {
                utilPct >= 65 -> "High Demand"
                utilPct < 25 -> "Underutilized"
                else -> "Normal"
            }
            RoomUtilizationMetric(
                roomId = room.roomId,
                building = room.building,
                roomType = room.roomType,
                capacity = room.capacity,
                scheduledHoursPerWeek = hours,
                utilizationPercent = utilPct,
                status = status
            )
        }

        // Building Utilizations
        val buildingGroups = rooms.groupBy { it.building }
        val buildingUtils = buildingGroups.map { (bName, bRooms) ->
            val roomCount = bRooms.size
            val totalCap = bRooms.sumOf { it.capacity }
            val bMinutes = bRooms.sumOf { roomScheduledMinutes[it.roomId] ?: 0 }
            val totalAvail = roomCount * totalWeeklyAvailableMinutesPerRoom
            val pct = if (totalAvail > 0) ((bMinutes.toDouble() / totalAvail) * 100).roundToInt().coerceIn(0, 100) else 0

            BuildingUtilization(
                buildingName = bName,
                roomCount = roomCount,
                totalCapacity = totalCap,
                totalScheduledMinutesPerWeek = bMinutes,
                utilizationPercent = pct
            )
        }.sortedByDescending { it.utilizationPercent }

        // 2. Request Demand by Day of Week
        val dayCounts = roomRequests.groupingBy { it.dayOfWeek }.eachCount()
        val totalReq = maxOf(1, roomRequests.size)
        val dayDemands = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday").map { d ->
            val c = dayCounts[d] ?: 0
            DayDemand(
                dayOfWeek = d,
                requestCount = c,
                percentage = ((c.toDouble() / totalReq) * 100).roundToInt()
            )
        }

        // 3. Request Demand by Time Period
        var morning = 0   // 08:00 - 11:30
        var midday = 0    // 11:30 - 14:30
        var afternoon = 0 // 14:30 - 17:30
        var evening = 0   // 17:30 - 21:00

        for (req in roomRequests) {
            val startM = TimeSlot.parseToMinutes(req.startTime)
            when {
                startM < 11 * 60 + 30 -> morning++
                startM < 14 * 60 + 30 -> midday++
                startM < 17 * 60 + 30 -> afternoon++
                else -> evening++
            }
        }

        val timeSlotDemands = listOf(
            TimeSlotDemand("Morning (08:00-11:30)", morning, ((morning.toDouble() / totalReq) * 100).roundToInt()),
            TimeSlotDemand("Midday (11:30-14:30)", midday, ((midday.toDouble() / totalReq) * 100).roundToInt()),
            TimeSlotDemand("Afternoon (14:30-17:30)", afternoon, ((afternoon.toDouble() / totalReq) * 100).roundToInt()),
            TimeSlotDemand("Evening (17:30-21:00)", evening, ((evening.toDouble() / totalReq) * 100).roundToInt())
        )

        // 4. Role Demands
        val roleCounts = roomRequests.groupingBy { it.requesterRole }.eachCount()
        val roleDemands = roleCounts.map { (role, count) ->
            RoleDemand(role, count, ((count.toDouble() / totalReq) * 100).roundToInt())
        }.sortedByDescending { it.count }

        val topUnderutilized = roomMetrics.filter { it.status == "Underutilized" }.sortedBy { it.utilizationPercent }.take(6)
        val topHighDemand = roomMetrics.filter { it.status == "High Demand" }.sortedByDescending { it.utilizationPercent }.take(6)

        val avgUtil = if (buildingUtils.isNotEmpty()) {
            buildingUtils.map { it.utilizationPercent }.average().roundToInt()
        } else 0

        val peakDay = dayDemands.maxByOrNull { it.requestCount }?.dayOfWeek ?: "Wednesday"
        val peakSlot = timeSlotDemands.maxByOrNull { it.requestCount }?.slotLabel ?: "Midday (11:30-14:30)"

        return DemandIntelligenceReport(
            totalRooms = rooms.size,
            totalClasses = classSchedules.size,
            totalRequests = roomRequests.size,
            totalEvents = events.size,
            averageScheduledUtilizationPct = avgUtil,
            peakDemandSlot = peakSlot,
            peakDemandDay = peakDay,
            buildingUtilizations = buildingUtils,
            timeSlotDemands = timeSlotDemands,
            dayDemands = dayDemands,
            roleDemands = roleDemands,
            topUnderutilizedRooms = topUnderutilized,
            topHighDemandRooms = topHighDemand,
            educationalCaveat = "Metrics strictly distinguish Scheduled Timetable Utilization from Incoming Request Demand and Verified Historical Event Attendance. No live sensor occupancy is assumed or fabricated."
        )
    }
}
