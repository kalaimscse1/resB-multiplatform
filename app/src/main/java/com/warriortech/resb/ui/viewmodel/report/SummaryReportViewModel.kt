package com.warriortech.resb.ui.viewmodel.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.BillRepository
import com.warriortech.resb.model.BillingSummary
import com.warriortech.resb.model.TblBillingResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummaryReportViewModel @Inject constructor(
    private val repository: BillRepository
) : ViewModel() {

    sealed class SummaryUiState<out T> {
        object Loading : SummaryUiState<Nothing>()
        data class Success<T>(val data: T) : SummaryUiState<T>()
        data class Error(val message: String) : SummaryUiState<Nothing>()
        object Empty : SummaryUiState<Nothing>()
    }

    private val _yearlySummary = MutableStateFlow<SummaryUiState<List<BillingSummary>>>(SummaryUiState.Empty)
    val yearlySummary: StateFlow<SummaryUiState<List<BillingSummary>>> = _yearlySummary

    private val _monthlySummary = MutableStateFlow<SummaryUiState<List<TblBillingResponse>>>(SummaryUiState.Empty)
    val monthlySummary: StateFlow<SummaryUiState<List<TblBillingResponse>>> = _monthlySummary

    fun fetchYearlySummary(year: String) {
        viewModelScope.launch {
            _yearlySummary.value = SummaryUiState.Loading
            repository.getYearlySummary(year).collect { result ->
                result.fold(
                    onSuccess = { data ->
                        _yearlySummary.value = if (data.isEmpty()) SummaryUiState.Empty else SummaryUiState.Success(data)
                    },
                    onFailure = { error ->
                        _yearlySummary.value = SummaryUiState.Error(error.message ?: "Unknown Error")
                    }
                )
            }
        }
    }

    fun fetchMonthlySummary(month: String, year: String) {
        viewModelScope.launch {
            _monthlySummary.value = SummaryUiState.Loading
            repository.getMonthlySummary(month, year).collect { result ->
                result.fold(
                    onSuccess = { data ->
                        _monthlySummary.value = if (data.isEmpty()) SummaryUiState.Empty else SummaryUiState.Success(data)
                    },
                    onFailure = { error ->
                        _monthlySummary.value = SummaryUiState.Error(error.message ?: "Unknown Error")
                    }
                )
            }
        }
    }
}
