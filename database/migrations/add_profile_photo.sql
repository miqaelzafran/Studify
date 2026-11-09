-- Studify DB migration
-- Run this AFTER studify_db.sql and update_studify_db.sql, in phpMyAdmin's SQL tab.
-- It adds the column the app needs to store the user's profile picture:
--   users.photo -> base64-encoded image data (NULL until the user uploads one)

ALTER TABLE `users`
    ADD COLUMN `photo` LONGTEXT NULL DEFAULT NULL AFTER `start_date`;
