**Project:** Studify - Task & Semester Tracking Mobile Application
**Name   :** Miqael Zafran Bin Azmi
**Course :** CSC264 - INTRODUCTION TO WEB AND MOBILE APPLICATION 
**Date   :** 15 July 2026

## Purpose of This Log

This Artificial Intelligence (AI) Log Report is a transparent and comprehensive record of the AI tools used in the development lifecycle of this project. The main purpose of this document is to support academic integrity by making a clear distinction between the student’s own work and any AI-assisted contributions.
AI platforms were used strictly as assistive tools to optimize workflow, troubleshoot technical errors, and outline foundational code structures throughout the development process. This log includes the exact prompts issued, the context in which the AI was asked and the critical human modifications applied to the generated outputs. The objective of this report is to fully document these interactions and be fully transparent about them, and to show that the core logic, problem-solving, and final implementations in the project are all the original work of the project team, and that AI is only used to aid and streamline the software development process.


---

| Date | AI Platform | Prompt / Query | Purpose of Use | Action Taken & Human Modification |
|---|---|---|---|---|
| 26 June 2026 | Claude | How to set up Android Studio for my individual assignment project | To get environment setup instructions since this was my first Android Studio project. | I followed the setup steps to setup android studio and connect with database using xampp |
| 26 June 2026 | Claude | Give me code for a starting layout and Java activity for the login screen following my propose design. | To get XML and Java structure for the login UI | I rewrote the XML layout entirely to match my own colour scheme and spacing. I changed the button colours from the default suggested hex codes to my app's theme colour and adjusted the font. |
| 1 July 2026 | Claude | Give me a starting layout and Java activity for the sign-up screen, including the semester weeks counter. | To get template for form validation and the Volley POST request to register.php. | I tested the generated code on my physical device and found the week counter buttons weren't clickable due to a missing OnClickListener, which I fixed with the help of build in android studio ai. I also restyled the input fields to use my `rounded_input` drawable instead of the default EditText style. |
| 8 July 2026 | Claude | Help me designing the PHP register.php and login.php endpoints and the corresponding MySQL users table. | To get a starting point for parameterized queries and password handling. | I reviewed the SQL, adjusted the column types to match my ERD, and decided independently to store the password as-is for this coursework scope rather than hashing, per my lecturer's instructions. |
| 10 July 2026 | Claude | Help me build the dashboard screen showing the week tracker and priority task. | To get a starting structure for fetching tasks and displaying the current week. | I tested this on my device and noticed the week number was wrong, which led to a separate debugging session. I also changed the card background colours to match my mockup. |
| 10 July 2026 | Claude | The week tracker was showing the wrong week number after entering a semester start date. | To diagnose a date-arithmetic bug in DateUtils.getCurrentWeek(). | I traced the bug against my own database records before accepting the diagnosis, then reviewed and tested the proposed fix on my device across several test dates before merging it in. |
| 12 July 2026 | Claude | How to add vibration when a task is marked done in my apps. | To add a UX polish feature and handle the Android Vibrator API across different SDK versions. | I decided myself that vibration should only trigger when marking a task done, not when un-marking it, and wired the helper into both places tasks can be completed after confirming those were the only two entry points in my code. |
| 12 July 2026 | Claude | Help me add a required profile photo picker to the sign-up screen. | To get template for image selection, resizing, and Base64 encoding consistent with my existing ProfileActivity. | I adjusted the avatar size and placement to fit my layout, and enforced the required-photo validation as my own design decision rather than making it optional. |
| 12 July 2026 | Claude | Final check all my coding to make sure there is no logic error, and tell me what or where to fix to improve my apps. | To catch any remaining errors before building the final APK. | I performed the actual build and on-device testing myself and fixed specific issue that only appeared at runtime. |

---

## Declaration of Academic Integrity

I confirm that Claude was used solely as a supplementary development tool for code review, defect diagnosis, and boilerplate generation. All resulting code was reviewed, tested where feasible, and integrated into the project under my own technical judgment. I retained final decision-making authority over the application's architecture, UI/UX design, data validation rules, and database schema, and I am accountable for the correctness and quality of the submitted implementation.

**Date:** 15 July 2026
