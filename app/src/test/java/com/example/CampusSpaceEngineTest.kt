package com.example

import com.example.data.model.*
import com.example.engine.GlobalAllocationOptimizer
import com.example.engine.RecommendationEngine
import com.example.engine.SchedulingEngine
import com.example.ml.AttendanceLinearRegression
import org.junit.Assert.*
import org.junit.Test

class CampusSpaceEngineTest {

    private val schedulingEngine = SchedulingEngine()
    private val recommendationEngine = RecommendationEngine(schedulingEngine)
    private val optimizer = GlobalAllocationOptimizer(schedulingEngine)

    @Test
    fun testHalfOpenIntervalOverlapSemantics() {
        // [10:00, 11:30) and [11:30, 13:00) should NOT conflict
        val slot1 = TimeSlot.fromStrings("10:00", "11:30")
        val slot2 = TimeSlot.fromStrings("11:30", "13:00")
        assertFalse("Adjacent half-open intervals must not overlap", slot1.overlapsWith(slot2))
        assertFalse("Adjacent half-open intervals must not overlap (reverse)", slot2.overlapsWith(slot1))

        // [10:00, 11:30) and [11:00, 12:00) DO overlap
        val slot3 = TimeSlot.fromStrings("11:00", "12:00")
        assertTrue("Overlapping intervals must be detected", slot1.overlapsWith(slot3))
        assertTrue("Overlapping intervals must be detected (reverse)", slot3.overlapsWith(slot1))

        // Subset interval [10:15, 11:00) inside [10:00, 11:30)
        val slot4 = TimeSlot.fromStrings("10:15", "11:00")
        assertTrue("Contained interval must overlap", slot1.overlapsWith(slot4))
    }

    @Test
    fun testHardConstraintEnforcementInRecommendationEngine() {
        val testRooms = listOf(
            Room("R-101", "Engineering", 1, "Classroom", 30, hasProjector = true, hasComputers = false, isAccessible = true, hasAudioSystem = false, hasWhiteboard = true),
            Room("R-102", "Engineering", 1, "Computer Lab", 40, hasProjector = true, hasComputers = true, isAccessible = true, hasAudioSystem = false, hasWhiteboard = true),
            Room("R-103", "Engineering", 1, "Seminar Room", 20, hasProjector = true, hasComputers = false, isAccessible = false, hasAudioSystem = false, hasWhiteboard = true)
        )

        // Request needing 35 attendees and computers
        val results = recommendationEngine.evaluateRooms(
            rooms = testRooms,
            dayOfWeek = "Monday",
            startTime = "10:00",
            endTime = "11:30",
            expectedAttendance = 35,
            requiredRoomType = null,
            requiresProjector = true,
            requiresComputers = true,
            requiresAccessible = true,
            classSchedules = emptyList(),
            confirmedEvents = emptyList()
        )

        // R-101 is rejected (capacity 30 < 35, missing computers)
        val r101 = results.first { it.room.roomId == "R-101" }
        assertFalse(r101.isFeasible)

        // R-102 is feasible (capacity 40 >= 35, has projector, computers, accessible)
        val r102 = results.first { it.room.roomId == "R-102" }
        assertTrue(r102.isFeasible)
        assertTrue(r102.recommendationScore > 0)

        // R-103 is rejected (capacity 20 < 35, lacks computers and accessibility)
        val r103 = results.first { it.room.roomId == "R-103" }
        assertFalse(r103.isFeasible)
    }

    @Test
    fun testMLModelTrainedFromScratchPerformance() {
        val ml = AttendanceLinearRegression()

        // Generate synthetic training events
        val testEvents = mutableListOf<EventRecord>()
        for (i in 1..200) {
            val exp = 20 + (i % 60)
            val act = (exp * 0.85 + (i % 5)).toInt()
            testEvents.add(
                EventRecord(
                    eventId = "EVT-$i",
                    eventName = "Workshop $i",
                    eventType = if (i % 2 == 0) "Academic Workshop" else "Club Meeting",
                    date = "2026-10-15",
                    dayOfWeek = "Wednesday",
                    startHour = 14,
                    durationHours = 2.0,
                    roomId = "R-101",
                    expectedAttendance = exp,
                    actualAttendance = act,
                    hostOrganization = "ACM"
                )
            )
        }

        val metrics = ml.train(testEvents, randomSeed = 42L)
        assertNotNull(metrics)
        assertTrue("Test set size should be > 0", metrics.testCount > 0)
        assertTrue("Model MAE should outperform baseline MAE", metrics.testMae <= metrics.baselineMae)
        assertTrue("Model R2 should be positive", metrics.testR2 >= 0.0)

        // Test inference
        val pred = ml.predict(
            expectedAttendance = 50,
            eventType = "Academic Workshop",
            startHour = 14,
            durationHours = 2.0,
            dayOfWeek = "Wednesday"
        )
        assertTrue("Predicted attendance should be reasonable", pred.predictedActualAttendance in 30..55)
        assertTrue("Confidence interval must bracket prediction", pred.confidenceLower <= pred.predictedActualAttendance && pred.confidenceUpper >= pred.predictedActualAttendance)
    }

    @Test
    fun testGlobalAllocationOptimizerVsGreedy() {
        val rooms = listOf(
            Room("BIG-1", "Main", 1, "Lecture Hall", 200, hasProjector = true, hasComputers = false, isAccessible = true, hasAudioSystem = true, hasWhiteboard = true),
            Room("MED-1", "Main", 1, "Classroom", 40, hasProjector = true, hasComputers = false, isAccessible = true, hasAudioSystem = false, hasWhiteboard = true)
        )

        // Two competing requests at the same time:
        // Req1: Small meeting (30 attendees, priority 2)
        // Req2: Large symposium (150 attendees, priority 5)
        val req1 = RoomRequest("REQ-1", "Student", "Club", "Small Study", "2026-10-15", "Monday", "10:00", "11:30", 30, "Classroom", requiresProjector = true, requiresComputers = false, requiresAccessible = true, priority = 2, status = "Pending")
        val req2 = RoomRequest("REQ-2", "Faculty", "Dept", "Major Symposium", "2026-10-15", "Monday", "10:00", "11:30", 150, "Lecture Hall", requiresProjector = true, requiresComputers = false, requiresAccessible = true, priority = 5, status = "Pending")

        // In naive greedy if req1 is evaluated first, it could grab BIG-1, leaving req2 stranded
        val globalRes = optimizer.optimizeGlobal(listOf(req1, req2), rooms, emptyList(), emptyList())

        // Global optimizer must accommodate BOTH requests by giving MED-1 to Req1 and BIG-1 to Req2!
        assertEquals("Global optimizer should accommodate both requests", 2, globalRes.accommodatedCount)
        val assignedRooms = globalRes.assignments.associate { it.requestId to it.roomId }
        assertEquals("MED-1", assignedRooms["REQ-1"])
        assertEquals("BIG-1", assignedRooms["REQ-2"])
    }
}
