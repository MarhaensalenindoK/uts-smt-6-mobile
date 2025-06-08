package com.example.cryptokiss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cryptokiss.adapter.BotPromptAdapter
import com.example.cryptokiss.database.DatabaseHelper
import com.example.cryptokiss.model.BotPrompt
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ManageBotsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var botAdapter: BotPromptAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var emptyStateTextView: TextView
    private var botList = mutableListOf<BotPrompt>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_bots)

        setSupportActionBar(findViewById(R.id.toolbar_manage_bots))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recycler_view_bots)
        fab = findViewById(R.id.fab_add_bot)
        emptyStateTextView = findViewById(R.id.text_view_empty_state)

        setupRecyclerView()
        loadBots()

        fab.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun setupRecyclerView() {
        botAdapter = BotPromptAdapter(botList,
            onEditClick = { bot -> showAddEditDialog(bot) },
            onDeleteClick = { bot -> showDeleteConfirmationDialog(bot) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = botAdapter
    }

    private fun loadBots() {
        val bots = dbHelper.getAllBotPrompts()
        botList.clear()
        botList.addAll(bots)
        botAdapter.updateData(botList)
        checkEmptyState()
    }

    private fun checkEmptyState() {
        if (botList.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateTextView.visibility = View.GONE
        }
    }

    private fun showAddEditDialog(bot: BotPrompt?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_bot, null)
        val botNameEditText = dialogView.findViewById<EditText>(R.id.edit_text_bot_name)
        val botPromptEditText = dialogView.findViewById<EditText>(R.id.edit_text_bot_prompt)

        bot?.let {
            botNameEditText.setText(it.name)
            botPromptEditText.setText(it.prompt)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (bot == null) "Buat Bot Baru" else "Edit Bot")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val name = botNameEditText.text.toString().trim()
                val prompt = botPromptEditText.text.toString().trim()

                if (name.isNotEmpty() && prompt.isNotEmpty()) {
                    if (bot == null) {
                        dbHelper.addBotPrompt(BotPrompt(name = name, prompt = prompt))
                        Toast.makeText(this, "Bot berhasil dibuat!", Toast.LENGTH_SHORT).show()
                    } else {
                        val updatedBot = bot.copy(name = name, prompt = prompt)
                        dbHelper.updateBotPrompt(updatedBot)
                        Toast.makeText(this, "Bot berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    }
                    loadBots()
                } else {
                    Toast.makeText(this, "Nama dan Prompt tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .create()

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(bot: BotPrompt) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Bot")
            .setMessage("Apakah Anda yakin ingin menghapus '${bot.name}'?")
            .setPositiveButton("Hapus") { _, _ ->
                dbHelper.deleteBotPrompt(bot)
                Toast.makeText(this, "'${bot.name}' telah dihapus", Toast.LENGTH_SHORT).show()
                loadBots()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
