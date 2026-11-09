# Studify – Task & Semester Tracker (CSC264 Individual Assignment)

Android app (Java) + PHP/MySQL backend for tracking assignments, quizzes and semester weeks.

**Author:** Miqael Zafran Bin Azmi  
**Demo video:** https://youtu.be/lpqapffGeyg

## Repository layout
| Folder | Contents |
|---|---|
| `Studify/` | Android Studio project (Java, min SDK 24) |
| `studify_api/` | PHP API endpoints (run on XAMPP/Apache) |
| `database/studify_db.sql` | Full MySQL export (schema + sample data) |
| `database/migrations/` | Old incremental scripts – **not needed** if you import `studify_db.sql` |
| `release/Studify-debug.apk` | Pre-built debug APK |
| `docs/AI_Log_Report.md` | AI usage log |

## How to run
1. Install **XAMPP**, start **Apache** and **MySQL**.
2. Copy the `studify_api` folder into `C:\xampp\htdocs\` (so `http://localhost/studify_api/login.php` works).
3. Open `http://localhost/phpmyadmin`, create a database named **`studify_db`** (collation `utf8mb4_general_ci`), then **Import** `database/studify_db.sql`.
4. Open the `Studify/` folder in **Android Studio** and let Gradle sync (internet required; JDK 21 is auto-downloaded).
5. Start an **Android emulator** and press Run. The app calls the server at `http://10.0.2.2/studify_api/` (the emulator's alias for your PC's localhost).
   - Physical phone? Replace `10.0.2.2` with your PC's LAN IP in the 7 Activity files (same Wi-Fi required).

## Sample login
Username `miqazaf` / Password `password123`

> Note: passwords are stored in plain text for coursework simplicity. Do not use this in production.
