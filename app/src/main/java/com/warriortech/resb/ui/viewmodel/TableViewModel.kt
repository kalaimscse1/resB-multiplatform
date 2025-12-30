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