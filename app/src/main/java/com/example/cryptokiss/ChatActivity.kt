package com.example.cryptokiss

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var toolbarChat: Toolbar
    private lateinit var recyclerViewChatMessages: RecyclerView
    private lateinit var editTextChatMessage: EditText
    private lateinit var buttonSendChatMessage: ImageButton

    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    private val GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY
    private lateinit var generativeModel: GenerativeModel
    private var chatSession: Chat? = null

    private var loadingMessageId: String? = null

    // >>> PERUBAHAN: Variabel untuk menyimpan prompt yang aktif <<<
    private lateinit var activeSystemPrompt: String
    private lateinit var activeBotName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // >>> PERUBAHAN: Mengambil data dari Intent <<<
        activeBotName = intent.getStringExtra("BOT_NAME") ?: "Crypto Assistant"
        activeSystemPrompt = intent.getStringExtra("BOT_PROMPT") ?: getString(R.string.default_crypto_prompt)

        toolbarChat = findViewById(R.id.toolbar_chat)
        setSupportActionBar(toolbarChat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = activeBotName // Set judul toolbar sesuai nama bot

        recyclerViewChatMessages = findViewById(R.id.recycler_view_chat_messages)
        editTextChatMessage = findViewById(R.id.edit_text_chat_message)
        buttonSendChatMessage = findViewById(R.id.button_send_chat_message)

        if (GEMINI_API_KEY.isBlank() || GEMINI_API_KEY == "MASUKKAN_API_KEY_ANDA_DI_SINI") {
            Toast.makeText(this, "Harap masukkan API Key Gemini Anda di file local.properties", Toast.LENGTH_LONG).show()
            Log.e("ChatActivity", "API Key belum diatur.")
            editTextChatMessage.isEnabled = false
            buttonSendChatMessage.isEnabled = false
            return
        }

        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash-latest",
                apiKey = GEMINI_API_KEY,
            )
        } catch (e: Exception) {
            Log.e("ChatActivity", "Error initializing GenerativeModel: ${e.message}", e)
            Toast.makeText(this, "Gagal menginisialisasi AI. Periksa API Key dan koneksi internet.", Toast.LENGTH_LONG).show()
            editTextChatMessage.isEnabled = false
            buttonSendChatMessage.isEnabled = false
            // Hentikan eksekusi jika model gagal diinisialisasi
            return
        }

        setupRecyclerView()
        startChatSessionWithInitialGreeting()

        buttonSendChatMessage.setOnClickListener {
            val messageText = editTextChatMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val userMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = messageText,
                    senderType = SenderType.USER,
                    timestamp = System.currentTimeMillis()
                )
                addMessageToChat(userMessage)
                editTextChatMessage.setText("")

                // Tampilkan pesan loading sebelum mengirim ke AI
                showLoadingMessage()
                sendMessageToGemini(messageText)
            } else {
                Toast.makeText(this, "Pesan tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)
        recyclerViewChatMessages.layoutManager = LinearLayoutManager(this).apply {
            // stackFromEnd = true // Sebaiknya false agar chat mulai dari atas
        }
        recyclerViewChatMessages.adapter = chatAdapter
    }

    private fun addMessageToChat(chatMessage: ChatMessage) {
        messageList.add(chatMessage)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        recyclerViewChatMessages.scrollToPosition(messageList.size - 1)
    }

    private fun showLoadingMessage() {
        removeLoadingMessage() // Hapus loading message sebelumnya jika ada
        loadingMessageId = UUID.randomUUID().toString()
        val loadingMsg = ChatMessage(
            id = loadingMessageId!!,
            text = "Sedang memproses...",
            senderType = SenderType.AI,
            timestamp = System.currentTimeMillis()
        )
        addMessageToChat(loadingMsg)
    }

    private fun removeLoadingMessage() {
        loadingMessageId?.let { idToRemove ->
            val indexToRemove = messageList.indexOfFirst { it.id == idToRemove }
            if (indexToRemove != -1) {
                messageList.removeAt(indexToRemove)
                chatAdapter.notifyItemRemoved(indexToRemove)
            }
        }
        loadingMessageId = null
    }


    private fun startChatSessionWithInitialGreeting() {
        if (!::generativeModel.isInitialized) {
            Log.w("ChatActivity", "GenerativeModel not initialized. Skipping initial greeting.")
            return
        }

        lifecycleScope.launch {
            try {
                // >>> PERUBAHAN: Menggunakan prompt yang dinamis <<<
                chatSession = generativeModel.startChat(
                    history = listOf(
                        content(role = "user") { text(activeSystemPrompt) },
                        content(role = "model") { text("Tentu, saya siap. Ada yang bisa saya bantu?") }
                    )
                )
                val greetingText = "Halo! Saya $activeBotName. Siap membantu Anda. Silakan bertanya!"
                val aiGreetingMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = greetingText,
                    senderType = SenderType.AI,
                    timestamp = System.currentTimeMillis()
                )
                addMessageToChat(aiGreetingMessage)

            } catch (e: Exception) {
                Log.e("ChatActivity", "Error starting chat session or getting initial greeting: ${e.message}", e)
                addMessageToChat(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = "Gagal memulai sesi dengan AI. Periksa koneksi internet Anda.",
                        senderType = SenderType.AI,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }


    private fun sendMessageToGemini(userMessageText: String) {
        val currentChatSession = chatSession
        if (currentChatSession == null || !::generativeModel.isInitialized) {
            Log.e("ChatActivity", "Chat session or model is not initialized.")
            removeLoadingMessage()
            addTechnicalDifficultyMessage("Sesi chat belum siap.")
            return
        }

        lifecycleScope.launch {
            try {
                val response = currentChatSession.sendMessage(userMessageText)
                removeLoadingMessage()
                response.text?.let { aiResponseText ->
                    val aiMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = aiResponseText,
                        senderType = SenderType.AI,
                        timestamp = System.currentTimeMillis()
                    )
                    addMessageToChat(aiMessage)
                } ?: run {
                    Log.w("ChatActivity", "AI response text is null. Reason: ${response.candidates.firstOrNull()?.finishReason}")
                    addTechnicalDifficultyMessage("Tidak ada respons teks dari AI.")
                }
            } catch (e: ServerException) {
                Log.e("ChatActivity", "ServerException: ${e.message}", e)
                removeLoadingMessage()
                addTechnicalDifficultyMessage("Error server dari AI: ${e.message?.take(100)}")
            }
            catch (e: Exception) { // Tangkap semua exception lain sebagai fallback
                Log.e("ChatActivity", "Generic error sending message: ${e.message}", e)
                removeLoadingMessage()
                addTechnicalDifficultyMessage()
            }
        }
    }

    private fun addTechnicalDifficultyMessage(customMessage: String? = null) {
        val errorMessage = customMessage ?: "Maaf, terjadi sedikit kendala teknis."
        addMessageToChat(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = errorMessage,
                senderType = SenderType.AI,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
