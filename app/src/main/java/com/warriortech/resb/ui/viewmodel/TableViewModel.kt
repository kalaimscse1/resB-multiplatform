package com.warriortech.resb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.TableRepository
import com.warriortech.resb.model.Area
import com.warriortech.resb.model.TableStatusResponse
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.ConnectionState
import com.warriortech.resb.util.CurrencySettings
import com.warriortech.resb.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the table management screen
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TableViewModel @Inject constructor(
    private val tableRepository: TableRepository,
    private val networkMonitor: NetworkMonitor,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Network connection state
    private val _connectionState = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConnectionState.Available
    )
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // Tables state flow
    private val _tablesState = MutableStateFlow<TablesState>(TablesState.Loading)
    val tablesState: StateFlow<TablesState> = _tablesState
    private val _area = MutableStateFlow<List<Area>>(emptyList())
    val areas: StateFlow<List<Area>> = _area


    // Selected section
    private val _selectedSection = MutableStateFlow<Long?>(null)

    // Table selection mode
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedTables = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTables: StateFlow<Set<Long>> = _selectedTables.asStateFlow()

    private val _selectionAction = MutableStateFlow<SelectionAction?>(null)
    val selectionAction: StateFlow<SelectionAction?> = _selectionAction.asStateFlow()

    sealed class SelectionAction {
        object ChangeTable : SelectionAction()
        object MergeTable : SelectionAction()
    }

    fun enableSelectionMode(action: SelectionAction, initialTableId: Long) {
        _isSelectionMode.value = true
        _selectionAction.value = action
        _selectedTables.value = setOf(initialTableId)
    }

    fun disableSelectionMode() {
        _isSelectionMode.value = false
        _selectionAction.value = null
        _selectedTables.value = emptySet()
    }

    fun toggleTableSelection(tableId: Long) {
        if (!_isSelectionMode.value) return
        
        val currentSelection = _selectedTables.value
        if (currentSelection.contains(tableId)) {
            if (currentSelection.size > 1) { // Keep at least one table selected (the source)
                _selectedTables.value = currentSelection - tableId
            }
        } else {
            if (_selectionAction.value == SelectionAction.ChangeTable) {
                // Change table only allows one source and one destination
                _selectedTables.value = setOf(currentSelection.first(), tableId)
            } else {
                _selectedTables.value = currentSelection + tableId
            }
        }
    }

    fun confirmSelection() {
        viewModelScope.launch {
            val action = _selectionAction.value
            val tables = _selectedTables.value.toList()
            
            if (action != null && tables.size >= 2) {
                try {
                    val success = when (action) {
                        SelectionAction.ChangeTable -> {

                            tableRepository.changeTable(tables[0], tables[1])
                        }
                        SelectionAction.MergeTable -> {
                            tableRepository.mergeTables(tables[0], tables[1])
                        }
                    }
                    if (success) {
                        loadTables()
                    } else {
                        _tablesState.value = TablesState.Error("Action failed on server")
                    }
                } catch (e: Exception) {
                    _tablesState.value = TablesState.Error(e.message ?: "Action failed")
                }
            }
            disableSelectionMode()
        }
    }

    init {
        CurrencySettings.update(
            symbol = sessionManager.getRestaurantProfile()?.currency ?: "",
            decimals = sessionManager.getRestaurantProfile()?.decimal_point?.toInt() ?: 2
        )
    }

    fun loadTables() {
        // Load tables when section changes or just all tables if no section selected
        viewModelScope.launch {
            _area.value = tableRepository.getAllAreas().filter { it.area_name != "--" }
            _selectedSection
                .flatMapLatest { section ->
                    if (section != null) {
                        tableRepository.getTablesBySection(section)

                    } else {
                        tableRepository.getActiveTables()
                    }
                }
                .catch { e ->
                    _tablesState.value = TablesState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { tables ->
                    _tablesState.value = TablesState.Success(
                        tables
                    )
                }
        }
    }

    /**
     * Set the selected section filter
     */
    fun setSection(section: Long?) {
        _selectedSection.value = section
    }

    /**
     * Load all tables without section filter
     */
    fun loadAllTables() {
        _selectedSection.value = null
    }

    /**
     * Update table status (works offline)
     */
    fun updateTableStatus(tableId: Long, status: String) {
        viewModelScope.launch {
            try {
                val updatedTable = tableRepository.updateTableStatus(tableId, status)

                // If we're offline, show appropriate message
                if (_connectionState.value == ConnectionState.Unavailable) {
                    // Table status updated locally, will sync when online
                } else {
                    // Table status updated on server
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * State holder for tables screen
     */
    sealed class TablesState {
        object Loading : TablesState()
        data class Success(val tables: List<TableStatusResponse>) : TablesState()
        data class Error(val message: String) : TablesState()
    }
}