<?php
header('Content-Type: text/plain');
require 'db_connect.php';

$name = isset($_POST['name']) ? trim($_POST['name']) : '';
$email = isset($_POST['email']) ? trim($_POST['email']) : '';
$username = isset($_POST['username']) ? trim($_POST['username']) : '';
$password = isset($_POST['password']) ? trim($_POST['password']) : '';
$semester_weeks = isset($_POST['semester_weeks']) ? (int)$_POST['semester_weeks'] : 14;
$start_date = isset($_POST['start_date']) ? trim($_POST['start_date']) : '';
// Required: base64-encoded profile photo picked during sign up.
$photo = isset($_POST['photo']) ? trim($_POST['photo']) : '';

if ($name === '' || $email === '' || $username === '' || $password === '' || $start_date === '' || $photo === '') {
    echo "missing_fields";
    exit;
}

// Check if username already exists
$check = $conn->prepare("SELECT id FROM users WHERE username = ?");
$check->bind_param("s", $username);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    echo "username_taken";
    $check->close();
    $conn->close();
    exit;
}
$check->close();

$stmt = $conn->prepare("INSERT INTO users (name, email, username, password, semester_weeks, start_date, photo) VALUES (?, ?, ?, ?, ?, ?, ?)");
$stmt->bind_param("ssssiss", $name, $email, $username, $password, $semester_weeks, $start_date, $photo);

if ($stmt->execute()) {
    echo "success";
} else {
    echo "error";
}

$stmt->close();
$conn->close();
?>
