package com.example.cryptokiss

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cryptokiss.adapter.SelectBotAdapter
import com.example.cryptokiss.database.DatabaseHelper
import com.example.cryptokiss.model.BotPrompt

class SelectBotActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_bot)

        setSupportActionBar(findViewById(R.id.toolbar_select_bot))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recycler_view_select_bot)
        emptyStateTextView = findViewById(R.id.text_view_empty_state_select)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val botList = dbHelper.getAllBotPrompts().toMutableList()

        // Tambahkan bot default di paling atas daftar
        botList.add(0, BotPrompt(id = -1, name = "Crypto Assistant (Default)", prompt = getString(R.string.default_crypto_prompt)))


        if (botList.size <= 1) { // Hanya ada bot default
            emptyStateTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        val adapter = SelectBotAdapter(botList) { selectedBot ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("BOT_NAME", selectedBot.name)
                putExtra("BOT_PROMPT", selectedBot.prompt)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
