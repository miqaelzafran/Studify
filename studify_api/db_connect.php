<?php
// Database connection settings for XAMPP's default MySQL/MariaDB setup.
// Change these only if your XAMPP MySQL username/password/db name differ.
$host = "localhost";
$db_user = "root";
$db_pass = "";
$db_name = "studify_db";

$conn = new mysqli($host, $db_user, $db_pass, $db_name);

if ($conn->connect_error) {
    header('Content-Type: application/json');
    die(json_encode(["status" => "error", "message" => "Database connection failed: " . $conn->connect_error]));
}

$conn->set_charset("utf8mb4");
?>
