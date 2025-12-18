package com.warriortech.resb.ui.viewmodel.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.GroupRepository
import com.warriortech.resb.model.TblGroupDetails
import com.warriortech.resb.model.TblGroupNature
import com.warriortech.resb.model.TblGroupRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupDetailsViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    sealed class GroupUiState {
        object Loading : GroupUiState()
        data class Success(val groups: List<TblGroupDetails>) : GroupUiState()
        data class Error(val message: String) : GroupUiState()
    }

    private val _msg = MutableStateFlow<String>("")
    val msg: StateFlow<String> = _msg.asStateFlow()


    private val _groupState = MutableStateFlow<GroupUiState>(GroupUiState.Loading)
    val groupState: StateFlow<GroupUiState> = _groupState.asStateFlow()
    private val _groups = MutableStateFlow<List<TblGroupDetails>>(emptyList())
    val groups = _groups.asStateFlow()

    private val _groupNatures = MutableStateFlow<List<TblGroupNature>>(emptyList())
    val groupNatures = _groupNatures.asStateFlow()

    private val _orderBy = MutableStateFlow<String>("")
    val orderBy: StateFlow<String> = _orderBy.asStateFlow()
    fun loadGroups() {
        viewModelScope.launch {
            groupRepository.getGroups().let {
                _groups.value = it ?: emptyList()
                _groupState.value = GroupUiState.Success(it ?: emptyList())
            }
        }
    }

    fun getOrderBy() {
        viewModelScope.launch {
            try {
                val response = groupRepository.getOrderBy()
                _orderBy.value = response["order_by"].toString()
            } catch (e: Exception) {
                _groupState.value = GroupUiState.Error(e.message ?: "Failed to getOrderBy")
            }
        }
    }

    fun loadGroupNature() {
        viewModelScope.launch {
            groupRepository.getGroupNatures().let {
                _groupNatures.value = it ?: emptyList()
            }
        }
    }

    fun addGroup(group: TblGroupRequest) {
        viewModelScope.launch {
            val res = groupRepository.checkExists(group.group_name)
            if (res.data==true){
                val res = groupRepository.createGroup(group)
                if (res != null)
                    _msg.value = "Group added successfully"
            }
            else{
                val msg = res.message
                _msg.value = msg
            }
        }
    }

    fun updateGroup(group_id: Int, group: TblGroupRequest) {
        viewModelScope.launch {
            val res = groupRepository.updateGroup(group_id, group)
            if (res != null)
                _msg.value = "Group Updated successfully"
        }
    }
    fun clearMsg(){
        _msg.value = ""
    }
    fun deleteGroup(group_id: Int) {
        viewModelScope.launch {
            val res = groupRepository.deleteGroup(group_id)
            if (res != null)
                _msg.value = "Group deleted successfully"
        }
    }

    fun checkExists(group_name: String) {
        viewModelScope.launch {
            try {
                val res = groupRepository.checkExists(group_name)
                if (res.data == false) {
                    val msg = res.message
                    _msg.value = msg
                }
            } catch (e: Exception) {

            }
        }
    }

}
