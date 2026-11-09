<?php
header('Content-Type: text/plain');
require 'db_connect.php';

$task_id = isset($_POST['task_id']) ? (int)$_POST['task_id'] : 0;
$status = isset($_POST['status']) ? trim($_POST['status']) : '';

$allowed = ['assigned', 'done', 'missed'];
if ($task_id <= 0 || !in_array($status, $allowed)) {
    echo "invalid_input";
    exit;
}

$stmt = $conn->prepare("UPDATE tasks SET status = ? WHERE id = ?");
$stmt->bind_param("si", $status, $task_id);

if ($stmt->execute()) {
    echo "success";
} else {
    echo "error";
}

$stmt->close();
$conn->close();
?>
