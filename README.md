# CS528_Pantry_N_Health

Pantry&Health has 5 screens including Exercise, Pantry&Barcode Scanner, Freshness Scanner, Login&Register, Settings.

Here are the different folders/files responsible for:

1. api/ - It it a food API to load the data on UI from the database
2. data/ - It is the main work space for ROOM library, including database, tables, and queries create
3. notification/ - It is used to provide notifications for expiry date reminders
4. ui.theme/ - It includes the UI designs in the project
5. ml/ - It contains the MobileNetv2 training model for freshness detection
6. AuthViewModel.kt - Create view model for accounts table to login/register
7. FoodViewModel - Create view model for food_item table to pantry&barcode scanning
8. StepsViewModel - Create view model for steps table to exercise screen
9. globals.kt - Storage the global values
10. Exercise.kt - Exercise Screen
11. Barcode.kt - Pantry&Barcode Screen
12. Classify.kt - Freshness Detection Screen
13. Login.kt - Login&Register Screen
14. Settings.kt - Settings Screen

When running Pantry&Health, the mobile device must deployed with Google Play Service. Before running our project, you need to sync the dependencies by entering the libs.version.toml file. And make sure the compileSdk version and targetSdk version are the same with your android device inside app build.gradle.kts.

