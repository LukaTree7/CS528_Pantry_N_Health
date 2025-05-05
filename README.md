# CS528_Pantry_N_Health

Pantry&Health has 5 screens including Exercise, Pantry&Barcode Scanner, Freshness Scanner, Login&Register, Settings.

Here are the different folders/files responsible for:

1. Freshness_detection_model_dataset/ - Contains the ipynb file for the classification model and the structured dataset used to train the model
2. api/ - It it a food API to load the data on UI from the database
3. data/ - It is the main work space for ROOM library, including database, tables, and queries create
4. notification/ - It is used to provide notifications for expiry date reminders
5. ui.theme/ - It includes the UI designs in the project
6. ml/ - It contains the MobileNetv2 training model for freshness detection
7. AuthViewModel.kt - Create view model for accounts table to login/register
8. FoodViewModel - Create view model for food_item table to pantry&barcode scanning
9. StepsViewModel - Create view model for steps table to exercise screen
10. globals.kt - Storage the global values
11. Exercise.kt - Exercise Screen
12. Barcode.kt - Pantry&Barcode Screen
13. Classify.kt - Freshness Detection Screen
14. Login.kt - Login&Register Screen
15. Settings.kt - Settings Screen

When running Pantry&Health, the mobile device must deployed with Google Play Service. Before running our project, you need to sync the dependencies by entering the libs.version.toml file. And make sure the compileSdk version and targetSdk version are the same with your android device inside app build.gradle.kts.

Here's the datasets we used for MobileNetv2 training:
https://www.kaggle.com/datasets/swoyam2609/fresh-and-stale-classification/data

Here's the demo of Pantry&Health:
https://youtube.com/shorts/V105TiAgEis?feature=shared
