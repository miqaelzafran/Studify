<?php
header('Content-Type: text/plain');
require 'db_connect.php';

$task_id = isset($_POST['task_id']) ? (int)$_POST['task_id'] : 0;

if ($task_id <= 0) {
    echo "invalid_input";
    exit;
}

$stmt = $conn->prepare("DELETE FROM tasks WHERE id = ?");
$stmt->bind_param("i", $task_id);

if ($stmt->execute()) {
    echo "success";
} else {
    echo "error";
}

$stmt->close();
$conn->close();
?>
