package com.example.cryptokiss

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val toolbar: Toolbar = findViewById(R.id.toolbar_main_menu)
        setSupportActionBar(toolbar)

        val cardManageBots: MaterialCardView = findViewById(R.id.card_manage_bots)
        val cardSelectBot: MaterialCardView = findViewById(R.id.card_select_bot)

        cardManageBots.setOnClickListener {
            startActivity(Intent(this, ManageBotsActivity::class.java))
        }

        cardSelectBot.setOnClickListener {
            startActivity(Intent(this, SelectBotActivity::class.java))
        }
    }
}
