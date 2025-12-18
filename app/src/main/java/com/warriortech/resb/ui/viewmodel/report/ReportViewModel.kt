package com.warriortech.resb.ui.viewmodel.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.ReportRepository
import com.warriortech.resb.screens.reports.ReportUiState
import com.warriortech.resb.util.getCurrentDateModern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    private val _reportState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val reportState: StateFlow<ReportUiState> = _reportState.asStateFlow()

    init {
        loadReports(getCurrentDateModern(), getCurrentDateModern())
    }

    fun loadReports(fromDate: String, toDate: String) {
        viewModelScope.launch {
            _reportState.value = ReportUiState.Loading
            repository.getSalesReport(fromDate, toDate).collect { res ->
                res.fold(
                    onSuccess = { _reportState.value = ReportUiState.Success(it) },
                    onFailure = {
                        _reportState.value = ReportUiState.Error(it.message ?: "Unknown Error")
                    }
                )
            }
        }
    }
}