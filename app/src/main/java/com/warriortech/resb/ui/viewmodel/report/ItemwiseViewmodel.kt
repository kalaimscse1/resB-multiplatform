package com.warriortech.resb.ui.viewmodel.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.ReportRepository
import com.warriortech.resb.screens.reports.ItemWiseReportReportUiState
import com.warriortech.resb.util.getCurrentDateModern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemWiseViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    private val _reportState =
        MutableStateFlow<ItemWiseReportReportUiState>(ItemWiseReportReportUiState.Idle)
    val reportState: StateFlow<ItemWiseReportReportUiState> = _reportState.asStateFlow()

    init {
        loadReports(getCurrentDateModern(), getCurrentDateModern())
    }

    fun loadReports(fromDate: String, toDate: String) {
        viewModelScope.launch {
            try {
                _reportState.value = ItemWiseReportReportUiState.Loading
                repository.getItemReport(fromDate, toDate).collect { res ->
                    res.fold(
                        onSuccess = { _reportState.value = ItemWiseReportReportUiState.Success(it) },
                        onFailure = {
                            _reportState.value =
                                ItemWiseReportReportUiState.Error(it.message ?: "Unknown Error")
                        }
                    )
                }
            }catch (e: Exception) {
                _reportState.value =
                    ItemWiseReportReportUiState.Error(e.message ?: "Failed to load report")
            }
        }
    }

}



