# resAPP - Restaurant Billing System

## Overview
A comprehensive Android restaurant billing system built with Kotlin, Jetpack Compose, Hilt dependency injection, and WorkManager. The system provides counter billing, table management, inventory tracking, payment processing, background data synchronization, and user support documentation.

## User Preferences
Preferred communication style: Simple, everyday language.

## Recent Changes (Latest: December 29, 2025)
- **BARCODE SCANNER FEATURE**: Added barcode scanning capability to Quick Bill and Menu screens
  - Integrated MLKit barcode scanner for multiple barcode formats (QR Code, Code 128, EAN-13, UPC, etc.)
  - Added barcode scanner button to QuickBillScreen top app bar for quick item addition to bills
  - Added barcode scanner button to MenuScreen top app bar for quick item addition to orders
  - Implemented barcode search in BillingViewModel to find items by menu_item_code
  - Implemented barcode search in MenuViewModel to find items by menu_item_code
  - Displays error messages when barcode is not found or scanning fails
  - Seamlessly adds items to the bill/order when barcode is successfully scanned

## Previous Changes (December 16, 2025)
- **UI/UX FIXES**: Fixed 6 reported issues in the billing system
  - **Bill Discount/Charges**: Made discount and other charges fields editable in BillingScreen with proper TextField inputs and ViewModel integration
  - **Enter Key Behavior**: Added singleLine=true to dialog text fields in MenuCategorySettingsScreen, TableSettingsScreen, and AreaSettingsScreen to prevent unwanted newlines
  - **Keyboard Visibility**: Added imePadding() modifier to ReusableBottomSheet for proper keyboard visibility in bottom sheet dialogs
  - **Payment Proceed Button**: Fixed proceed button to only enable when user manually enters payment amount (removed auto-fill LaunchedEffect from PaymentMethodCard)
  - **Import Feature**: Added import icon to MenuItemSettingsScreen with placeholder function for future Excel/CSV import functionality
- **IMPORT COMPLETED**: Successfully migrated project to Replit environment
  - Installed Java GraalVM toolchain for Android development
  - Configured Android SDK with Platform 35, Build Tools 35, and Platform Tools 36
  - Updated Gradle memory settings for better performance (4GB heap)
  - Validated project configuration with successful dry-run build
  - Streamlined workflows to single Android Build validation workflow

## Previous Changes (September 25, 2025)
- **UI FIX**: Comprehensive navigation button display fix for Redmi/MIUI devices
  - Implemented edge-to-edge display with WindowCompat.setDecorFitsSystemWindows
  - Added proper system bar and navigation bar handling in ResbTheme
  - Created RedmiSafeScaffold and system bar utility components
  - Fixed navigation button positioning issues on Redmi devices with custom navigation bars
  - Enhanced theme with transparent system bars and proper contrast enforcement
- **NEW FEATURE**: Implemented complete paid bills management system
  - Created PaidBillRepository for API operations (CRUD and search)
  - Developed PaidBillsViewModel with proper state management
  - Built PaidBillsListScreen with search, view, edit, and delete functionality
  - Added navigation integration and route configuration
- Successfully imported GitHub project into Replit environment
- **SECURITY**: Removed committed keystore file (key/signedkey.jks) and updated .gitignore to prevent future key commits
- Set up complete Android development environment with Java GraalVM 19.0.2
- Configured Gradle 8.11.1 with Kotlin 2.0.20 support
- Installed full Android SDK with required build tools and platform components
- Created Android development workflow and environment setup script
- Tested Android build process (completes most compilation tasks successfully)
- Established project structure for Android development in Replit

## Development Environment Setup
- **Java Runtime**: OpenJDK 19.0.2 with GraalVM CE 22.3.1
- **Build System**: Gradle 8.11.1 with Kotlin 2.0.20
- **Android Tools**: Basic android-tools package installed
- **Environment Script**: `android_setup.sh` for quick environment configuration

### Current Capabilities
- Java/Kotlin compilation environment ready (OpenJDK 19.0.2 with GraalVM CE)
- Gradle wrapper properly configured (Gradle 8.11.1)
- Full Android SDK installed with required components:
  - Android SDK Build-Tools 35
  - Android SDK Command-line Tools (latest)
  - Android SDK Platform-Tools
  - Android SDK Platform 35 (compileSdk target)
- Project dependencies and structure validated
- Android development workflow configured and tested

### Limitations
- Build process may encounter memory/resource limitations in Replit environment
- Android emulator not available in current environment
- Large builds may cause Gradle daemon to disappear due to resource constraints
- Requires manual environment setup for each session

### Building the Project
The Android project can be built using:
```bash
./android_setup.sh  # Set up Android SDK environment
./gradlew assembleDebug  # Build debug APK
```

**Note**: The build process successfully completes most Android compilation tasks but may encounter resource limitations during final assembly in the Replit environment.

## System Architecture
Android application structure:
- **Frontend**: Jetpack Compose with Material Design
- **Backend Logic**: Kotlin with Hilt dependency injection
- **Background Tasks**: WorkManager for data synchronization
- **Reports**: PDF generation using iTextPDF library
- **Database**: Local SQLite with API integration capabilities

Key components:
- Counter billing and quick billing screens
- Table management system
- Inventory tracking
- Payment processing (Cash, Card, UPI, Due)
- **Paid bills management system** (NEW)
  - List view with search functionality
  - Edit and delete operations
  - Status tracking and payment method display
  - Date-based filtering and sorting
- Comprehensive reporting (Sales, Itemwise, Category-wise)
- User support documentation with video tutorials

## External Dependencies
- Kotlin & Jetpack Compose for UI
- Hilt for dependency injection
- WorkManager for background tasks
- iTextPDF for report generation
- Apache POI for Excel exports
- Material Design components