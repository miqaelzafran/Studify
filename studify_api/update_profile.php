<?php
header('Content-Type: text/plain');
require 'db_connect.php';

$user_id = isset($_POST['user_id']) ? (int)$_POST['user_id'] : 0;
$name = isset($_POST['name']) ? trim($_POST['name']) : '';
$username = isset($_POST['username']) ? trim($_POST['username']) : '';
// Optional: base64-encoded profile photo. Only present when the user picked a new photo.
$photo = isset($_POST['photo']) ? trim($_POST['photo']) : '';

if ($user_id <= 0 || $name === '' || $username === '') {
    echo "missing_fields";
    exit;
}

// Make sure the new username isn't already taken by someone else
$check = $conn->prepare("SELECT id FROM users WHERE username = ? AND id != ?");
$check->bind_param("si", $username, $user_id);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    echo "username_taken";
    $check->close();
    $conn->close();
    exit;
}
$check->close();

if ($photo !== '') {
    $stmt = $conn->prepare("UPDATE users SET name = ?, username = ?, photo = ? WHERE id = ?");
    $stmt->bind_param("sssi", $name, $username, $photo, $user_id);
} else {
    $stmt = $conn->prepare("UPDATE users SET name = ?, username = ? WHERE id = ?");
    $stmt->bind_param("ssi", $name, $username, $user_id);
}

if ($stmt->execute()) {
    echo "success";
} else {
    echo "error";
}

$stmt->close();
$conn->close();
?>
