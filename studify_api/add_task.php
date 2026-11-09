<?php
header('Content-Type: text/plain');
require 'db_connect.php';

$user_id = isset($_POST['user_id']) ? (int)$_POST['user_id'] : 0;
$title = isset($_POST['title']) ? trim($_POST['title']) : '';
$type = isset($_POST['type']) ? trim($_POST['type']) : 'ASSIGNMENT';
$due_date = isset($_POST['due_date']) ? trim($_POST['due_date']) : '';
$due_time = isset($_POST['due_time']) ? trim($_POST['due_time']) : '';
$location = isset($_POST['location']) ? trim($_POST['location']) : '-';

if ($user_id <= 0 || $title === '' || $due_date === '' || $due_time === '') {
    echo "missing_fields";
    exit;
}

$stmt = $conn->prepare("INSERT INTO tasks (title, type, due_date, due_time, location, user_id, status) VALUES (?, ?, ?, ?, ?, ?, 'assigned')");
$stmt->bind_param("sssssi", $title, $type, $due_date, $due_time, $location, $user_id);

if ($stmt->execute()) {
    echo "success";
} else {
    echo "error";
}

$stmt->close();
$conn->close();
?>
