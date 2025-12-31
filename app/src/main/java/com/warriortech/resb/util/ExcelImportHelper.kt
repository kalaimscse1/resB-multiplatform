package com.warriortech.resb.util

import android.content.Context
import android.net.Uri
import com.warriortech.resb.data.local.entity.SyncStatus
import com.warriortech.resb.data.local.entity.TblMenuItem
import timber.log.Timber
import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory

class ExcelImportHelper(private val context: Context) {

    fun importMenuItemsFromExcel(uri: Uri): List<TblMenuItem>? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.let {
                val workbook = WorkbookFactory.create(it)
                val sheet = workbook.getSheetAt(0)
                val menuItems = mutableListOf<TblMenuItem>()

                for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(rowIndex) ?: continue
                    
                    try {
                        val menuItem = TblMenuItem(
                            menu_item_id = (row.getCell(0)?.numericCellValue?.toInt() ?: 0),
                            menu_item_code = row.getCell(1)?.stringCellValue ?: "",
                            menu_item_name = row.getCell(2)?.stringCellValue ?: "",
                            menu_item_name_tamil = row.getCell(3)?.stringCellValue ?: "",
                            menu_id = (row.getCell(4)?.numericCellValue?.toInt() ?: 0),
                            rate = row.getCell(5)?.numericCellValue ?: 0.0,
                            image = row.getCell(6)?.stringCellValue ?: "",
                            ac_rate = row.getCell(7)?.numericCellValue ?: 0.0,
                            parcel_rate = row.getCell(8)?.numericCellValue ?: 0.0,
                            is_available = row.getCell(9)?.stringCellValue ?: "Y",
                            item_cat_id = (row.getCell(10)?.numericCellValue?.toInt() ?: 0),
                            parcel_charge = row.getCell(11)?.numericCellValue ?: 0.0,
                            tax_id = (row.getCell(12)?.numericCellValue?.toInt() ?: 0),
                            kitchen_cat_id = (row.getCell(13)?.numericCellValue?.toInt() ?: 0),
                            stock_maintain = row.getCell(14)?.stringCellValue ?: "N",
                            rate_lock = row.getCell(15)?.stringCellValue ?: "N",
                            unit_id = (row.getCell(16)?.numericCellValue?.toInt() ?: 0),
                            min_stock = (row.getCell(17)?.numericCellValue?.toInt() ?: 0),
                            hsn_code = row.getCell(18)?.stringCellValue ?: "",
                            order_by = (row.getCell(19)?.numericCellValue?.toInt() ?: 0),
                            is_inventory = (row.getCell(20)?.numericCellValue?.toInt() ?: 0),
                            is_raw = row.getCell(21)?.stringCellValue ?: "N",
                            cess_specific = row.getCell(22)?.numericCellValue ?: 0.0,
                            is_favourite = (row.getCell(23)?.booleanCellValue ?: false),
                            is_active = (row.getCell(24)?.numericCellValue?.toInt() ?: 1) == 1,
                            preparation_time = (row.getCell(25)?.numericCellValue?.toInt() ?: 0),
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                        menuItems.add(menuItem)
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing menu item at row $rowIndex")
                    }
                }
                workbook.close()
                it.close()
                menuItems
            }
        } catch (e: Exception) {
            Timber.e(e, "Error importing menu items from Excel")
            null
        }
    }
}
