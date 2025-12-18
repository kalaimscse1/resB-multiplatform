package com.warriortech.resb.data.repository

import com.warriortech.resb.model.TblLedgerDetailIdRequest
import com.warriortech.resb.model.TblLedgerDetailsIdResponse
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject

class LedgerDetailsRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
){
    suspend fun getLedgerDetails(fromDate: String,toDate: String):List<TblLedgerDetailsIdResponse>?{
        return try {
            apiService.getLedgerdetails(sessionManager.getCompanyCode() ?: "",fromDate,toDate).body()
        }catch (e:Exception){
            null
        }
    }

    suspend fun getDayBook(fromDate: String,toDate: String):List<TblLedgerDetailsIdResponse>?{
        return try {
            apiService.getDayBook(sessionManager.getCompanyCode() ?: "",fromDate,toDate).body()
        }catch (e:Exception){
            null
        }
    }


    suspend fun addLedgerDetails(ledgerDetails:TblLedgerDetailIdRequest): TblLedgerDetailsIdResponse?{
        return try {
            apiService.addLedgerDetails(ledgerDetails,sessionManager.getCompanyCode() ?: "").body()
        }catch (e:Exception){
            null
        }
    }

    suspend fun addAllLedgerDetails(ledgerDetails:List<TblLedgerDetailIdRequest>): Boolean?{
        return try {
            apiService.saveAllLedgerDetails(ledgerDetails,sessionManager.getCompanyCode() ?: "").body()
        }catch (e: Exception){
            null
        }
    }

    suspend fun updateAllLedgerDetails(ledgerDetails:List<TblLedgerDetailIdRequest>): Boolean?{
        return try {
            apiService.updateAllLedgerDetails(ledgerDetails,sessionManager.getCompanyCode() ?: "").body()
        }catch (e: Exception){
            null
        }
    }

    suspend fun getLedgerDetailsByLedgerId(entryNo:String):List<TblLedgerDetailsIdResponse>?{
        return try {
            apiService.getLedgerDetailsByEntryNo(entryNo,sessionManager.getCompanyCode() ?: "").body()
        }catch (e: Exception){
            null
        }
    }

    suspend fun updateLedgerDetails(ledgerDetailsId:Long,ledgerDetails:TblLedgerDetailIdRequest): Int? {
        return try {
            apiService.updateLedgerDetails(ledgerDetailsId,ledgerDetails,sessionManager.getCompanyCode()?:"").body()
        }catch (e: Exception){
            null
        }
    }

    suspend fun deleteLedgerDetails(ledgerDetailId: Long):Int?{
        return try {
            apiService.deleteLedgerDetails(ledgerDetailId,sessionManager.getCompanyCode()?:"").body()
        }catch (e:Exception){
            null
        }
    }

    suspend fun deleteByEntryNo(entryNo: String):Int?{
        return try {
            apiService.deleteByEntryNo(entryNo,sessionManager.getCompanyCode()?:"").body()
        }catch (e:Exception){
            null
        }
    }


    suspend fun getEntryNo():Map<String, String>{
        return try {
            apiService.getEntryNo(
                sessionManager.getUser()?.counter_id?:0,
                "ACCOUNTS",
                sessionManager.getCompanyCode()?:"").body()!!
        }catch (e: Exception){
            emptyMap()
        }
    }

    suspend fun getLedgerDetailsById(ledgerId:Long,fromDate: String,toDate: String) : List<TblLedgerDetailsIdResponse>{
        return try {
            apiService.getByPartyId(
                ledgerId,
                sessionManager.getCompanyCode()?:"",
                fromDate,
                toDate
            ).body()!!
        }catch (e: Exception){
            emptyList()
        }
    }

    suspend fun getOpeningBalance(fromDate: String,ledgerId: Long) : Map<String, Double>{
        return try {
            apiService.getOpeningBalance(
                fromDate,
                ledgerId,
                sessionManager.getCompanyCode()?:""
            ).body()!!
        }catch (e: Exception) {
            emptyMap()
        }
    }
}