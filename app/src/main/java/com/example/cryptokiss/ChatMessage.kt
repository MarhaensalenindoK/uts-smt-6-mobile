package com.example.cryptokiss

// Data class untuk merepresentasikan satu pesan dalam chat
data class ChatMessage(
    val id: String, // ID unik untuk setiap pesan, bisa menggunakan UUID atau timestamp
    val text: String, // Isi teks dari pesan
    val senderType: SenderType, // Enum untuk menentukan pengirim (PENGGUNA atau AI)
    val timestamp: Long // Waktu pesan dikirim atau diterima (dalam milidetik)
)

// Enum untuk tipe pengirim
enum class SenderType {
    USER,
    AI
}
