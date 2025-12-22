package com.warriortech.resb.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.work.WorkManager
import com.warriortech.resb.ai.AIRepository
import com.warriortech.resb.data.local.MIGRATION_2_3
import com.warriortech.resb.data.local.RestaurantDatabase
import com.warriortech.resb.data.local.dao.MenuItemDao
import com.warriortech.resb.data.local.dao.TableDao
import com.warriortech.resb.data.local.dao.TblOrderDetailsDao
import com.warriortech.resb.data.local.dao.TblOrderMasterDao
import com.warriortech.resb.data.local.dao.TblVoucherDao
import com.warriortech.resb.data.repository.AreaRepository
import com.warriortech.resb.data.repository.DashboardRepository
import com.warriortech.resb.data.repository.MenuItemRepository
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.data.repository.TableRepository
import com.warriortech.resb.data.repository.CounterRepository
import com.warriortech.resb.data.repository.GeneralSettingsRepository
import com.warriortech.resb.data.repository.MenuRepository
import com.warriortech.resb.data.repository.ModifierRepository
import com.warriortech.resb.data.repository.PrinterRepository
import com.warriortech.resb.data.repository.RestaurantProfileRepository
import com.warriortech.resb.data.repository.RoleRepository
import com.warriortech.resb.data.repository.StaffRepository
import com.warriortech.resb.data.repository.TaxRepository
import com.warriortech.resb.data.repository.TaxSplitRepository
import com.warriortech.resb.data.repository.TemplateRepository
import com.warriortech.resb.data.repository.UnitRepository
import com.warriortech.resb.data.repository.VoucherTypeRepository
import com.warriortech.resb.data.repository.VoucherRepository
import com.warriortech.resb.data.sync.SyncManager
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.notification.NotificationHelper
import com.warriortech.resb.service.PrintService
import com.warriortech.resb.util.NetworkMonitor
import com.warriortech.resb.util.PrinterHelper
import com.warriortech.resb.util.SubscriptionScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRestaurantDatabase(@ApplicationContext context: Context): RestaurantDatabase {
        return Room.databaseBuilder(
            context,
            RestaurantDatabase::class.java,
            "KTS-RESB"
        )
//            .addMigrations(MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
    }


    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideTableDao(database: RestaurantDatabase): TableDao {
        return database.tableDao()
    }

    //
    @Provides
    @Singleton
    fun provideMenuItemDao(database: RestaurantDatabase): MenuItemDao {
        return database.menuItemDao()
    }

    @Provides
    fun provideOrderMasterDao(db: RestaurantDatabase): TblOrderMasterDao {
        return db.tblOrderMasterDao()
    }

    @Provides
    fun provideOrderDetailsDao(db: RestaurantDatabase): TblOrderDetailsDao {
        return db.tblOrderDetailsDao()
    }

    @Provides
    fun provideVoucherDao(db: RestaurantDatabase): TblVoucherDao {
        return db.tblVoucherDao()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("YourPrefsName", Context.MODE_PRIVATE)
    }

//    @Provides
//    @Singleton
//    fun provideOrderDao(database: RestaurantDatabase): OrderDao {
//        return database.orderDao()
//    }

//    @Provides
//    @Singleton
//    fun provideOrderItemDao(database: RestaurantDatabase): OrderItemDao {
//        return database.orderItemDao()
//    }

    @Provides
    @Singleton
    fun provideSubscriptionScheduler(@ApplicationContext context: Context): SubscriptionScheduler {
        return SubscriptionScheduler(context)
    }

    @Provides
    @Singleton
    fun provideNotifocationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitor(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(okHttpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl("http://72.61.172.248:5055/api/")
//            .baseUrl("http://154.210.206.184:5050/api/") // Replace with your actual API base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAIRepository(@ApplicationContext context: Context): AIRepository {
        return AIRepository(context)
    }

    @Provides
    @Singleton
    fun provideTableRepository(
        tableDao: TableDao,
        apiService: ApiService,
        networkMonitor: NetworkMonitor,
        sessionManager: SessionManager
    ): TableRepository {
        return TableRepository(
            tableDao, apiService, networkMonitor,
            sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideMenuItemRepository(
        menuItemDao: MenuItemDao,
        apiService: ApiService,
        networkMonitor: NetworkMonitor,
        sessionManager: SessionManager
    ): MenuItemRepository {
        return MenuItemRepository(
            menuItemDao, apiService,
            networkMonitor,
            sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        apiService: ApiService,
        orderDao: TblOrderMasterDao,
        orderDetailsDao: TblOrderDetailsDao,
        voucherDao: TblVoucherDao,
        tableDao: TableDao,
        sessionManager: SessionManager,
        printerHelper: PrinterHelper
    ): OrderRepository {
        return OrderRepository(
            apiService,
            orderDao,
            orderDetailsDao,
            voucherDao,
            tableDao,
            sessionManager,
            printerHelper
        )
    }


    @Provides
    @Singleton // Or another appropriate scope if needed
    fun providePrinterHelper(@ApplicationContext context: Context /*, other dependencies */): PrinterHelper {
        return PrinterHelper(context /*, other dependencies */) // Assuming PrinterHelper needs Context
    }

    @Provides
    @Singleton
    fun dashboardRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): DashboardRepository {
        return DashboardRepository(
            apiService,
            sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        networkMonitor: NetworkMonitor,
        workManager: WorkManager,
        sessionManager: SessionManager,
        apiService: ApiService
    ): SyncManager {
        return SyncManager(
            context, networkMonitor, apiService, sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideCounterRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): CounterRepository {
        return CounterRepository(
            apiService = apiService,
            sessionManager = sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideAreaRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): AreaRepository {
        return AreaRepository(
            apiService = apiService,
            sessionManager = sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideStaffRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): StaffRepository {
        return StaffRepository(
            apiService = apiService,
            sessionManager = sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideRoleRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): RoleRepository {
        return RoleRepository(
            apiService = apiService,
            sessionManager = sessionManager
        )
    }

    @Provides
    @Singleton
    fun providePrinterRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): PrinterRepository {
        return PrinterRepository(
            apiService = apiService,
            sessionManager = sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideTemplateRepository(): TemplateRepository {
        return TemplateRepository()
    }

    @Provides
    @Singleton
    fun providePrintService(
        @ApplicationContext context: Context,
        templateRepository: TemplateRepository
    ): PrintService {
        return PrintService(context, templateRepository)
    }

    @Provides
    @Singleton
    fun provideTaxRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): TaxRepository {
        return TaxRepository(
            apiService = apiService,
            sessionManager = sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideTaxSplitRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): TaxSplitRepository {
        return TaxSplitRepository(
            apiService = apiService,
            sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideGeneralSettingsRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): GeneralSettingsRepository {
        return GeneralSettingsRepository(
            apiService = apiService,
            sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideRestaurantProfileRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): RestaurantProfileRepository {
        return RestaurantProfileRepository(
            apiService = apiService,
            sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideVoucherRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): VoucherRepository {
        return VoucherRepository(
            apiService = apiService,
            sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideMenuRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): MenuRepository = MenuRepository(apiService, sessionManager)

    @Provides
    @Singleton
    fun provideModifierRepository(
        apiService: ApiService,
        networkMonitor: NetworkMonitor,
        sessionManager: SessionManager
    ): ModifierRepository = ModifierRepository(apiService, networkMonitor, sessionManager)

    @Provides
    @Singleton
    fun provideUnitRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): UnitRepository = UnitRepository(apiService, sessionManager)

    @Provides
    @Singleton
    fun provideVoucherTypeRepository(
        apiService: ApiService,
        sessionManager: SessionManager
    ): VoucherTypeRepository = VoucherTypeRepository(apiService, sessionManager)
}