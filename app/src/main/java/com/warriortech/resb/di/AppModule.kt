package com.warriortech.resb.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.work.WorkManager
import com.warriortech.resb.ai.AIRepository
import com.warriortech.resb.data.local.MIGRATION_2_3
import com.warriortech.resb.data.local.RestaurantDatabase
import com.warriortech.resb.data.local.dao.MenuItemDao
import com.warriortech.resb.data.local.dao.PrintTemplateDao
import com.warriortech.resb.data.local.dao.TableDao
import com.warriortech.resb.data.local.dao.TblAreaDao
import com.warriortech.resb.data.local.dao.TblOrderDetailsDao
import com.warriortech.resb.data.local.dao.TblOrderMasterDao
import com.warriortech.resb.data.local.dao.TblVoucherDao
import com.warriortech.resb.data.local.entity.TblArea
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
import com.warriortech.resb.data.repository.UnitRepository
import com.warriortech.resb.data.repository.VoucherTypeRepository
import com.warriortech.resb.data.repository.VoucherRepository
import com.warriortech.resb.data.sync.SyncManager
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.network.WhatsAppApi
import com.warriortech.resb.notification.NotificationHelper
import com.warriortech.resb.util.NetworkMonitor
import com.warriortech.resb.util.PrinterHelper
import com.warriortech.resb.util.SubscriptionScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
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

    @Provides
    @Singleton
    fun provideAreaDao(database: RestaurantDatabase): TblAreaDao {
        return database.tblAreaDao()
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
    fun providePrintTemplateDao(database: RestaurantDatabase): PrintTemplateDao {
        return database.printTemplateDao()
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
    fun provideOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val baseUrlInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url
            
            // Do not intercept WhatsApp API requests
            if (originalUrl.host == "eco.hashwa.in") {
                return@Interceptor chain.proceed(originalRequest)
            }

            val newBaseUrlString = sessionManager.getBaseUrl()
            val newBaseUrl = newBaseUrlString.toHttpUrlOrNull() ?: return@Interceptor chain.proceed(originalRequest)

            val newUrl = originalUrl.newBuilder()
                .scheme(newBaseUrl.scheme)
                .host(newBaseUrl.host)
                .port(newBaseUrl.port)
                .build()

            chain.proceed(originalRequest.newBuilder().url(newUrl).build())
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(baseUrlInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(okHttpClient: OkHttpClient, sessionManager: SessionManager): ApiService {
        return Retrofit.Builder()
            .baseUrl(sessionManager.getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWhatsAppApiService(okHttpClient: OkHttpClient): WhatsAppApi {
        return Retrofit.Builder()
            .baseUrl("https://eco.hashwa.in/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(WhatsAppApi::class.java)
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
        sessionManager: SessionManager,
        printerHelper: PrinterHelper
    ): OrderRepository {
        return OrderRepository(
            apiService,
            sessionManager,
            printerHelper
        )
    }


    @Provides
    @Singleton
    fun providePrinterHelper(
        @ApplicationContext context: Context,
        printTemplateDao: PrintTemplateDao,
        sessionManager: SessionManager
    ): PrinterHelper {
        return PrinterHelper(context, printTemplateDao,sessionManager)
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
        sessionManager: SessionManager,
        areaDao: TblAreaDao,
        networkMonitor: NetworkMonitor
    ): AreaRepository {
        return AreaRepository(
            apiService = apiService,
            areaDao,
            sessionManager = sessionManager,
            networkMonitor
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
