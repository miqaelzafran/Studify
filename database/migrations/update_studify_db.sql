-- Studify DB migration
-- Run this AFTER importing studify_db.sql, in phpMyAdmin's SQL tab.
-- It adds the columns Studify's Android app and PHP API need:
--   tasks.user_id  -> links each task to its owner (users.id)
--   tasks.status   -> "assigned" (default) | "done" | "missed"
-- Existing seed rows are assigned to user id 1 (miqa) so old data keeps working.

ALTER TABLE `tasks`
    ADD COLUMN `user_id` INT(11) NOT NULL DEFAULT 1 AFTER `location`,
    ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'assigned' AFTER `user_id`;

ALTER TABLE `tasks`
    ADD KEY `fk_tasks_user` (`user_id`);

ALTER TABLE `tasks`
    ADD CONSTRAINT `fk_tasks_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- Make sure the existing 6 seed tasks belong to the seed user (miqa, id 1)
UPDATE `tasks` SET `user_id` = 1 WHERE `user_id` IS NULL OR `user_id` = 0;
