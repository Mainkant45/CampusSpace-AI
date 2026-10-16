package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.analytics.DemandIntelligence
import com.example.analytics.DemandIntelligenceReport
import com.example.data.CampusSpaceRepository
import com.example.data.model.AlternativeSuggestion
import com.example.data.model.AttendancePredictionResult
import com.example.data.model.DatasetAuditSummary
import com.example.data.model.FeasibilityResult
import com.example.data.model.MLModelMetrics
import com.example.data.model.OptimizationResult
import com.example.data.model.Room
import com.example.data.model.RoomRequest
import com.example.data.model.SimulationConfig
import com.example.data.model.SimulationResult
import com.example.data.model.TimeSlot
import com.example.engine.DigitalTwinSimulator
import com.example.engine.GlobalAllocationOptimizer
import com.example.engine.RecommendationEngine
import com.example.engine.SchedulingEngine
import com.example.ml.AttendanceLinearRegression
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class CampusSpaceTab(val title: String) {
    DASHBOARD("Overview"),
    ROOM_FINDER("Room Finder"),
    OPTIMIZER("Global Optimizer"),
    DIGITAL_TWIN("Digital Twin"),
    ML_LAB("ML Lab"),
    DATA_AUDIT("Data & Audit")
}

data class CampusSpaceUiState(
    val selectedTab: CampusSpaceTab = CampusSpaceTab.DASHBOARD,
    val isLoading: Boolean = true,
    val auditSummary: DatasetAuditSummary? = null,
    val demandReport: DemandIntelligenceReport? = null,
    val mlMetrics: MLModelMetrics? = null,
    val isTrainingML: Boolean = false,
    val livePrediction: AttendancePredictionResult? = null,
    // Room search
    val searchDay: String = "Monday",
    val searchStartTime: String = "10:00",
    val searchEndTime: String = "11:30",
    val searchAttendees: Int = 35,
    val searchRoomType: String = "Any",
    val searchProjector: Boolean = true,
    val searchComputers: Boolean = false,
    val searchAccessible: Boolean = true,
    val searchBuilding: String = "Any",
    val searchResults: List<FeasibilityResult> = emptyList(),
    val searchAlternatives: List<AlternativeSuggestion> = emptyList(),
    val hasSearched: Boolean = false,
    // Optimization
    val isOptimizing: Boolean = false,
    val greedyResult: OptimizationResult? = null,
    val globalResult: OptimizationResult? = null,
    val selectedPriorityFilter: Int = 0, // 0 = all
    // Simulation
    val isSimulating: Boolean = false,
    val simulationSurge: Int = 25,
    val simulationOfflineBuildings: Set<String> = emptySet(),
    val simulationOfflineRooms: Set<String> = emptySet(),
    val simulationInflation: Int = 10,
    val simulationResult: SimulationResult? = null,
    // Toast / Feedback
    val feedbackMessage: String? = null
)

class CampusSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CampusSpaceRepository(application)
    private val schedulingEngine = SchedulingEngine()
    private val recommendationEngine = RecommendationEngine(schedulingEngine)
    private val allocationOptimizer = GlobalAllocationOptimizer(schedulingEngine)
    private val digitalTwinSimulator = DigitalTwinSimulator(allocationOptimizer)
    private val mlModel = AttendanceLinearRegression()
    private val demandIntelligence = DemandIntelligence()

    private val _uiState = MutableStateFlow(CampusSpaceUiState())
    val uiState: StateFlow<CampusSpaceUiState> = _uiState.asStateFlow()

    init {
        initializeApplication()
    }

    fun selectTab(tab: CampusSpaceTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun initializeApplication() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Stage 1: Load datasets and compute demand intelligence fast for instant Dashboard render
            val (audit, demand) = withContext(Dispatchers.Default) {
                val auditSummary = repository.loadDatasets()
                val demandReport = demandIntelligence.computeReport(
                    rooms = repository.rooms,
                    classSchedules = repository.classSchedules,
                    roomRequests = repository.roomRequests,
                    events = repository.events
                )
                auditSummary to demandReport
            }

            // Immediately show Dashboard UI in under 200ms
            _uiState.update {
                it.copy(
                    isLoading = false,
                    auditSummary = audit,
                    demandReport = demand
                )
            }

            // Stage 2: Background computation for secondary tabs (ML Model baseline, default search, optimizer)
            withContext(Dispatchers.Default) {
                val mlMetrics = mlModel.train(repository.events)

                val defaultSearch = recommendationEngine.evaluateRooms(
                    rooms = repository.rooms,
                    dayOfWeek = "Monday",
                    startTime = "10:00",
                    endTime = "11:30",
                    expectedAttendance = 35,
                    requiredRoomType = null,
                    requiresProjector = true,
                    requiresComputers = false,
                    requiresAccessible = true,
                    classSchedules = repository.classSchedules,
                    confirmedEvents = repository.events,
                    bookedReservations = repository.confirmedUserBookings
                )
                val defaultAlts = recommendationEngine.generateAlternatives(
                    evaluatedRooms = defaultSearch,
                    dayOfWeek = "Monday",
                    startTime = "10:00",
                    endTime = "11:30",
                    expectedAttendance = 35,
                    classSchedules = repository.classSchedules,
                    confirmedEvents = repository.events,
                    bookedReservations = repository.confirmedUserBookings
                )

                val samplePending = repository.roomRequests.filter { it.status == "Pending" }.take(25)
                val greedy = allocationOptimizer.optimizeGreedy(
                    requests = samplePending,
                    rooms = repository.rooms,
                    classSchedules = repository.classSchedules,
                    confirmedEvents = repository.events
                )
                val global = allocationOptimizer.optimizeGlobal(
                    requests = samplePending,
                    rooms = repository.rooms,
                    classSchedules = repository.classSchedules,
                    confirmedEvents = repository.events
                )

                _uiState.update {
                    it.copy(
                        mlMetrics = mlMetrics,
                        searchResults = defaultSearch,
                        searchAlternatives = defaultAlts,
                        hasSearched = true,
                        greedyResult = greedy,
                        globalResult = global
                    )
                }
            }
        }
    }

    fun searchRooms(
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        attendees: Int,
        roomType: String,
        requiresProjector: Boolean,
        requiresComputers: Boolean,
        requiresAccessible: Boolean,
        building: String
    ) {
        viewModelScope.launch {
            val (results, alts) = withContext(Dispatchers.Default) {
                val bldgPref = if (building == "Any") null else building
                val typePref = if (roomType == "Any") null else roomType

                val rList = recommendationEngine.evaluateRooms(
                    rooms = repository.rooms,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    expectedAttendance = attendees,
                    requiredRoomType = typePref,
                    requiresProjector = requiresProjector,
                    requiresComputers = requiresComputers,
                    requiresAccessible = requiresAccessible,
                    preferredBuilding = bldgPref,
                    classSchedules = repository.classSchedules,
                    confirmedEvents = repository.events,
                    bookedReservations = repository.confirmedUserBookings
                )

                val aList = recommendationEngine.generateAlternatives(
                    evaluatedRooms = rList,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    expectedAttendance = attendees,
                    classSchedules = repository.classSchedules,
                    confirmedEvents = repository.events,
                    bookedReservations = repository.confirmedUserBookings
                )
                Pair(rList, aList)
            }

            _uiState.update {
                it.copy(
                    searchDay = dayOfWeek,
                    searchStartTime = startTime,
                    searchEndTime = endTime,
                    searchAttendees = attendees,
                    searchRoomType = roomType,
                    searchProjector = requiresProjector,
                    searchComputers = requiresComputers,
                    searchAccessible = requiresAccessible,
                    searchBuilding = building,
                    searchResults = results,
                    searchAlternatives = alts,
                    hasSearched = true
                )
            }
        }
    }

    fun bookRoom(room: Room, dayOfWeek: String, startTime: String, endTime: String) {
        val targetSlot = TimeSlot.fromStrings(startTime, endTime)

        // Strict recheck immediately before confirmation
        val conflicts = schedulingEngine.findConflicts(
            roomId = room.roomId,
            dayOfWeek = dayOfWeek,
            targetSlot = targetSlot,
            classSchedules = repository.classSchedules,
            confirmedEvents = repository.events,
            bookedReservations = repository.confirmedUserBookings
        )

        if (conflicts.isNotEmpty()) {
            _uiState.update {
                it.copy(feedbackMessage = "Booking failed: Conflict detected (${conflicts.first()})")
            }
            return
        }

        // Commit reservation
        repository.addConfirmedBooking(room.roomId, dayOfWeek, targetSlot)

        _uiState.update {
            it.copy(
                feedbackMessage = "Successfully reserved ${room.roomId} on $dayOfWeek at [$startTime - $endTime)!"
            )
        }

        // Re-run search to refresh state
        searchRooms(
            dayOfWeek = _uiState.value.searchDay,
            startTime = _uiState.value.searchStartTime,
            endTime = _uiState.value.searchEndTime,
            attendees = _uiState.value.searchAttendees,
            roomType = _uiState.value.searchRoomType,
            requiresProjector = _uiState.value.searchProjector,
            requiresComputers = _uiState.value.searchComputers,
            requiresAccessible = _uiState.value.searchAccessible,
            building = _uiState.value.searchBuilding
        )
    }

    fun runOptimization(priorityFilter: Int = 0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOptimizing = true, selectedPriorityFilter = priorityFilter) }

            val (greedy, global) = withContext(Dispatchers.Default) {
                val allPending = repository.roomRequests.filter { it.status == "Pending" }
                val filtered = if (priorityFilter > 0) {
                    allPending.filter { it.priority == priorityFilter }
                } else {
                    allPending
                }.take(60)

                val gr = allocationOptimizer.optimizeGreedy(
                    requests = filtered,
                    rooms = repository.rooms,
                    classSchedules = repository.classSchedules,
                    confirmedEvents = repository.events
                )

                val gl = allocationOptimizer.optimizeGlobal(
                    requests = filtered,
                    rooms = repository.rooms,
                    classSchedules = repository.classSchedules,
                    confirmedEvents = repository.events
                )
                Pair(gr, gl)
            }

            _uiState.update {
                it.copy(
                    isOptimizing = false,
                    greedyResult = greedy,
                    globalResult = global,
                    feedbackMessage = "Batch Optimization complete: Global optimizer accommodated ${global.accommodatedCount} requests (${global.totalCapacityWasteSeats} wasted seats vs ${greedy.totalCapacityWasteSeats} in Greedy)."
                )
            }
        }
    }

    fun runSimulation(
        surgePct: Int,
        offlineBuildings: Set<String>,
        offlineRooms: Set<String>,
        inflationPct: Int
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSimulating = true,
                    simulationSurge = surgePct,
                    simulationOfflineBuildings = offlineBuildings,
                    simulationOfflineRooms = offlineRooms,
                    simulationInflation = inflationPct
                )
            }

            val simResult = withContext(Dispatchers.Default) {
                val basePending = repository.roomRequests.filter { it.status == "Pending" }.take(50)
                val config = SimulationConfig(
                    demandSurgePercent = surgePct,
                    offlineRoomIds = offlineRooms,
                    offlineBuildings = offlineBuildings,
                    attendanceInflationPercent = inflationPct
                )

                digitalTwinSimulator.runSimulation(
                    baseRequests = basePending,
                    rooms = repository.rooms,
                    classSchedules = repository.classSchedules,
                    confirmedEvents = repository.events,
                    config = config
                )
            }

            _uiState.update {
                it.copy(
                    isSimulating = false,
                    simulationResult = simResult,
                    feedbackMessage = "Simulation recomputed: ${simResult.simulatedAccommodated} requests accommodated under stress scenario."
                )
            }
        }
    }

    fun trainMLModel(seed: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTrainingML = true) }

            val metrics = withContext(Dispatchers.Default) {
                mlModel.train(repository.events, randomSeed = seed)
            }

            _uiState.update {
                it.copy(
                    isTrainingML = false,
                    mlMetrics = metrics,
                    feedbackMessage = "Model retrained from scratch! Test MAE: ${metrics.testMae} people (R²: ${metrics.testR2})"
                )
            }
        }
    }

    fun predictAttendance(
        expectedAttendance: Int,
        eventType: String,
        startHour: Int,
        duration: Double,
        dayOfWeek: String
    ) {
        val result = mlModel.predict(
            expectedAttendance = expectedAttendance,
            eventType = eventType,
            startHour = startHour,
            durationHours = duration,
            dayOfWeek = dayOfWeek
        )
        _uiState.update { it.copy(livePrediction = result) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    // Accessors for Data Audit raw viewer
    fun getRooms(): List<Room> = repository.rooms
    fun getClassSchedules(): List<com.example.data.model.ClassSchedule> = repository.classSchedules
    fun getRoomRequests(): List<RoomRequest> = repository.roomRequests
    fun getEvents(): List<com.example.data.model.EventRecord> = repository.events
    fun getReadmeText(): String = repository.readmeText
}
