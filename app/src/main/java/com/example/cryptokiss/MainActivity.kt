package com.example.cryptokiss

import android.content.Intent // Import Intent
import android.os.Bundle
import android.widget.Button
// import android.widget.Toast // Tidak lagi diperlukan jika langsung pindah Activity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// Pastikan R diimport dengan benar, biasanya otomatis jika package name sesuai
// import com.example.cryptokiss.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val startChatButton: Button = findViewById(R.id.button_start_chat)
        startChatButton.setOnClickListener {
            // Membuat Intent untuk berpindah dari MainActivity ke ChatActivity
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent) // Memulai ChatActivity
        }
    }
}
