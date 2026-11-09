<?php
header('Content-Type: application/json');
require 'db_connect.php';

$username = isset($_POST['username']) ? trim($_POST['username']) : '';
$password = isset($_POST['password']) ? trim($_POST['password']) : '';

if ($username === '' || $password === '') {
    echo json_encode(["status" => "error", "message" => "Missing credentials"]);
    exit;
}

$stmt = $conn->prepare("SELECT id, name, email, username, password, semester_weeks, start_date, photo FROM users WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 1) {
    $row = $result->fetch_assoc();

    // NOTE: passwords in this project's seed data are stored in plain text for simplicity.
    // In a production app these should be hashed with password_hash()/password_verify().
    if ($password === $row['password']) {
        echo json_encode([
            "status" => "success",
            "user" => [
                "id" => (int)$row['id'],
                "name" => $row['name'],
                "email" => $row['email'],
                "username" => $row['username'],
                "semester_weeks" => (int)$row['semester_weeks'],
                "start_date" => $row['start_date'],
                "photo" => $row['photo'] !== null ? $row['photo'] : ""
            ]
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid username or password"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid username or password"]);
}

$stmt->close();
$conn->close();
?>
