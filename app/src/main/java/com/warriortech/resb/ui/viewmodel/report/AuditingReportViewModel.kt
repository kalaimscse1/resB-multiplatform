package com.warriortech.resb.ui.viewmodel.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.AuditingReportRepository
import com.warriortech.resb.model.TblAuditingResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuditingReportViewModel @Inject constructor(
    private val repository: AuditingReportRepository
) : ViewModel() {
    sealed class AuditingState {
        object Loading : AuditingState()
        data class Success(val report: List<TblAuditingResponse>) : AuditingState()
        data class Failure(val message: String) : AuditingState()
    }

    private val _auditingState = MutableStateFlow<AuditingState>(AuditingState.Loading)
    val auditingState: StateFlow<AuditingState> = _auditingState.asStateFlow()

    init {
        loadAuditingReport()
    }

    fun loadAuditingReport() {
        viewModelScope.launch {
            _auditingState.value = AuditingState.Loading
            try {
                val report = repository.getAuditingReport()
                _auditingState.value = AuditingState.Success(report)
            } catch (e: Exception) {
                _auditingState.value =
                    AuditingState.Failure(e.message ?: "Error loading auditing report")
            }
        }
    }
}