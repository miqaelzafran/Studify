<?php
header('Content-Type: text/plain');
require 'db_connect.php';

$user_id = isset($_POST['user_id']) ? (int)$_POST['user_id'] : 0;
$password = isset($_POST['password']) ? trim($_POST['password']) : '';

if ($user_id <= 0 || $password === '') {
    echo "missing_fields";
    exit;
}

$stmt = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
$stmt->bind_param("si", $password, $user_id);

if ($stmt->execute()) {
    echo "success";
} else {
    echo "error";
}

$stmt->close();
$conn->close();
?>
