package com.warriortech.resb.ui.viewmodel.report.gst

import androidx.lifecycle.ViewModel
import com.warriortech.resb.data.repository.GstRepository
import com.warriortech.resb.model.GSTRDOCS
import com.warriortech.resb.model.HsnReport
import com.warriortech.resb.model.ReportGSTResponse
import com.warriortech.resb.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HsnReportViewModel @Inject constructor(
    private val repository: GstRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    sealed class HsnUiState {
        object Loading : HsnUiState()
        data class Success(val data: List<HsnReport>) : HsnUiState()
        data class Error(val message: String) : HsnUiState()
        object Empty : HsnUiState()
    }

    sealed class GSTRUiState {
        object Loading : GSTRUiState()
        data class Success(val data: List<GSTRDOCS>) : GSTRUiState()
        data class Error(val message: String) : GSTRUiState()
        object Empty : GSTRUiState()
    }

    sealed class GstReportUiState {
        object Loading : GstReportUiState()
        data class Success(val data: List<ReportGSTResponse>) : GstReportUiState()
        data class Error(val message: String) : GstReportUiState()
        object Empty : GstReportUiState()
    }

    private val _hsnReports = MutableStateFlow<HsnUiState>(HsnUiState.Loading)
    val hsnReports: StateFlow<HsnUiState> = _hsnReports

    private val _gstrDocs = MutableStateFlow<GSTRUiState>(GSTRUiState.Loading)
    val gstrDocs: StateFlow<GSTRUiState> = _gstrDocs

    private val _gstReports = MutableStateFlow<GstReportUiState>(GstReportUiState.Loading)
    val gstReports: StateFlow<GstReportUiState> = _gstReports

    suspend fun fetchHsnReports(fromDate: String, toDate: String) {
        try {
            repository.getHsnReport(fromDate, toDate).collect { result ->
                result.fold(
                    onSuccess = { reports ->
                        _hsnReports.value = if (reports.isEmpty()) {
                            HsnUiState.Empty
                        } else {
                            HsnUiState.Success(reports)
                        }
                    },
                    onFailure = { error ->
                        _hsnReports.value = HsnUiState.Error(error.message ?: "Unknown Error")
                    }
                )
            }
        } catch (e: Exception) {
            _hsnReports.value = HsnUiState.Error(e.message ?: "Exception occurred")
        }
    }


    suspend fun fetchGSTRDocs(fromDate: String, toDate: String) {
        try {
            repository.getGstDocs(fromDate, toDate).collect { result ->
                result.fold(
                    onSuccess = { docs ->
                        _gstrDocs.value = if (docs.isEmpty()) {
                            GSTRUiState.Empty
                        } else {
                            GSTRUiState.Success(docs)
                        }
                    },
                    onFailure = { error ->
                        _gstrDocs.value = GSTRUiState.Error(error.message ?: "Unknown Error")
                    }
                )
            }
        } catch (e: Exception) {
            _gstrDocs.value = GSTRUiState.Error(e.message ?: "Exception occurred")
        }
    }

    suspend fun fetchGstReport(fromDate: String, toDate: String) {
        try {
            repository.getGstReport(fromDate, toDate).collect { result ->
                result.fold(
                    onSuccess = { reports ->
                        _gstReports.value = if (reports.isEmpty()) {
                            GstReportUiState.Empty
                        } else {
                            GstReportUiState.Success(reports)
                        }
                    },
                    onFailure = { error ->
                        _gstReports.value = GstReportUiState.Error(error.message ?: "Unknown Error")
                    }
                )
            }
        } catch (e: Exception) {
            _gstReports.value = GstReportUiState.Error(e.message ?: "Exception occurred")
        }
    }

}