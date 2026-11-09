<?php
header('Content-Type: application/json');
require 'db_connect.php';

$user_id = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 0;

if ($user_id <= 0) {
    echo json_encode([]);
    exit;
}

$stmt = $conn->prepare("SELECT id, title, type, due_date, due_time, location, status FROM tasks WHERE user_id = ? ORDER BY id DESC");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$tasks = [];
while ($row = $result->fetch_assoc()) {
    $tasks[] = [
        "id" => (int)$row['id'],
        "title" => $row['title'],
        "type" => $row['type'],
        "due_date" => $row['due_date'],
        "due_time" => $row['due_time'],
        "location" => $row['location'],
        "status" => $row['status'] !== null ? $row['status'] : 'assigned'
    ];
}

echo json_encode($tasks);

$stmt->close();
$conn->close();
?>
