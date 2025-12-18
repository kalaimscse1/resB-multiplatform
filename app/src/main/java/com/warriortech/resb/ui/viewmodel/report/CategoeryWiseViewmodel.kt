package com.warriortech.resb.ui.viewmodel.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.ReportRepository
import com.warriortech.resb.screens.reports.CategoryWiseReportReportUiState
import com.warriortech.resb.util.getCurrentDateModern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryWiseViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    private val _reportState =
        MutableStateFlow<CategoryWiseReportReportUiState>(CategoryWiseReportReportUiState.Idle)
    val reportState: StateFlow<CategoryWiseReportReportUiState> = _reportState.asStateFlow()

    init {
        loadReports(getCurrentDateModern(), getCurrentDateModern())
    }

    fun loadReports(fromDate: String, toDate: String) {
        viewModelScope.launch {
            _reportState.value = CategoryWiseReportReportUiState.Loading
            repository.getCategoryReport(fromDate, toDate).collect { res ->
                res.fold(
                    onSuccess = {
                        _reportState.value = CategoryWiseReportReportUiState.Success(it)
                    },
                    onFailure = {
                        _reportState.value =
                            CategoryWiseReportReportUiState.Error(it.message ?: "Unknown Error")
                    }
                )
            }
        }
    }

}