package com.warriortech.resb.ui.viewmodel.report.gst

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.GstRepository
import com.warriortech.resb.model.*
import com.warriortech.resb.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    sealed class GstB2CState {
        object Loading : GstB2CState()
        data class Success(val data: List<ReportGstB2CResponse>) : GstB2CState()
        data class Error(val message: String) : GstB2CState()
        object Empty : GstB2CState()
    }

    sealed class GstB2BState {
        object Loading : GstB2BState()
        data class Success(val data: List<GstB2BResponse>) : GstB2BState()
        data class Error(val message: String) : GstB2BState()
        object Empty : GstB2BState()
    }

    private val _hsnReports = MutableStateFlow<HsnUiState>(HsnUiState.Loading)
    val hsnReports: StateFlow<HsnUiState> = _hsnReports

    private val _gstrDocs = MutableStateFlow<GSTRUiState>(GSTRUiState.Loading)
    val gstrDocs: StateFlow<GSTRUiState> = _gstrDocs

    private val _gstReports = MutableStateFlow<GstReportUiState>(GstReportUiState.Loading)
    val gstReports: StateFlow<GstReportUiState> = _gstReports

    private val _gstB2CReports = MutableStateFlow<GstB2CState>(GstB2CState.Loading)
    val gstB2CReports: StateFlow<GstB2CState> = _gstB2CReports

    private val _gstB2BReports = MutableStateFlow<GstB2BState>(GstB2BState.Loading)
    val gstB2BReports: StateFlow<GstB2BState> = _gstB2BReports

    fun fetchHsnReports(fromDate: String, toDate: String) {
        viewModelScope.launch {
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
    }


    fun fetchGSTRDocs(fromDate: String, toDate: String) {
        viewModelScope.launch {
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
    }

    fun fetchGstReport(fromDate: String, toDate: String) {
        viewModelScope.launch {
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

    fun fetchGstB2CReport(fromDate: String, toDate: String) {
        viewModelScope.launch {
            try {
                _gstB2CReports.value = GstB2CState.Loading
                repository.getGstB2CReport(fromDate, toDate).collect { result ->
                    result.fold(
                        onSuccess = { reports ->
                            _gstB2CReports.value = if (reports.isEmpty()) GstB2CState.Empty else GstB2CState.Success(reports)
                        },
                        onFailure = { error ->
                            _gstB2CReports.value = GstB2CState.Error(error.message ?: "Unknown Error")
                        }
                    )
                }
            } catch (e: Exception) {
                _gstB2CReports.value = GstB2CState.Error(e.message ?: "Exception occurred")
            }
        }
    }

    fun fetchGstB2BReport(fromDate: String, toDate: String) {
        viewModelScope.launch {
            try {
                _gstB2BReports.value = GstB2BState.Loading
                repository.getGstB2BReport(fromDate, toDate).collect { result ->
                    result.fold(
                        onSuccess = { reports ->
                            _gstB2BReports.value = if (reports.isEmpty()) GstB2BState.Empty else GstB2BState.Success(reports)
                        },
                        onFailure = { error ->
                            _gstB2BReports.value = GstB2BState.Error(error.message ?: "Unknown Error")
                        }
                    )
                }
            } catch (e: Exception) {
                _gstB2BReports.value = GstB2BState.Error(e.message ?: "Exception occurred")
            }
        }
    }

}