package com.example.cryptokiss

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope // Import untuk lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
// Import exception yang relevan dari library Gemini
// import com.google.ai.client.generativeai.type.GenerationException // Sementara dikomentari jika unresolved
import com.google.ai.client.generativeai.type.ServerException
// import com.google.ai.client.generativeai.type.RequestException // Sementara dikomentari jika unresolved
import com.google.ai.client.generativeai.type.content // Untuk membangun histori chat
import kotlinx.coroutines.launch // Import untuk launch coroutine
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var toolbarChat: Toolbar
    private lateinit var recyclerViewChatMessages: RecyclerView
    private lateinit var editTextChatMessage: EditText
    private lateinit var buttonSendChatMessage: ImageButton

    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    // PENTING: Ganti dengan API Key Anda yang sebenarnya!
    private val GEMINI_API_KEY = "AIzaSyD4-u25dcY64aXNKLQlDbh4FJwOE6Jn7xo" // GANTI INI
    private lateinit var generativeModel: GenerativeModel
    private var chatSession: com.google.ai.client.generativeai.Chat? = null

    // ID untuk pesan loading, agar mudah ditemukan dan dihapus
    private var loadingMessageId: String? = null

    private val cryptoSystemPrompt = """
        Anda adalah asisten AI yang berpengetahuan luas tentang cryptocurrency dan pasar terkait.
        Tujuan Anda adalah untuk menjawab pertanyaan pengguna secara akurat, jelas, dan ringkas.
        Fokus hanya pada topik cryptocurrency, teknologi blockchain, analisis pasar (secara umum, bukan nasihat finansial),
        berita crypto, dan definisi istilah terkait crypto. Hindari membahas topik di luar itu.
        Jika pengguna bertanya di luar topik crypto, ingatkan mereka dengan sopan untuk tetap pada topik.
        Jaga agar jawaban tetap sederhana dan mudah dimengerti (Keep It Simple & Smart).
        Jangan memberikan nasihat keuangan atau investasi secara langsung.
    """.trimIndent()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toolbarChat = findViewById(R.id.toolbar_chat)
        setSupportActionBar(toolbarChat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        recyclerViewChatMessages = findViewById(R.id.recycler_view_chat_messages)
        editTextChatMessage = findViewById(R.id.edit_text_chat_message)
        buttonSendChatMessage = findViewById(R.id.button_send_chat_message)

        // Pastikan API Key sudah diisi
        if (GEMINI_API_KEY == "MASUKKAN_API_KEY_ANDA_DI_SINI") {
            Toast.makeText(this, "Harap masukkan API Key Gemini Anda di ChatActivity.kt", Toast.LENGTH_LONG).show()
            Log.e("ChatActivity", "API Key belum diatur.")
            editTextChatMessage.isEnabled = false
            buttonSendChatMessage.isEnabled = false
            return // Hentikan eksekusi lebih lanjut jika API Key tidak ada
        }

        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash-latest", // Atau model lain yang valid
                apiKey = GEMINI_API_KEY,
            )
        } catch (e: Exception) {
            Log.e("ChatActivity", "Error initializing GenerativeModel: ${e.message}", e)
            Toast.makeText(this, "Gagal menginisialisasi AI. Periksa API Key dan koneksi internet.", Toast.LENGTH_LONG).show()
            editTextChatMessage.isEnabled = false
            buttonSendChatMessage.isEnabled = false
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
            // stackFromEnd = true
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
        // Hanya jalankan jika generativeModel sudah diinisialisasi
        if (!::generativeModel.isInitialized) {
            Log.w("ChatActivity", "GenerativeModel not initialized. Skipping initial greeting.")
            return
        }

        lifecycleScope.launch {
            try {
                chatSession = generativeModel.startChat(
                    history = listOf(
                        content(role = "user") { text(cryptoSystemPrompt) },
                        content(role = "model") { text("Tentu, saya mengerti. Saya akan fokus pada topik cryptocurrency. Ada yang bisa saya bantu?") }
                    )
                )
                val greetingText = "Halo! Saya Crypto KISS Assistant. Siap membantu Anda menjelajahi dunia crypto. Silakan bertanya!"
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
        if (currentChatSession == null) {
            Log.e("ChatActivity", "Chat session is not initialized.")
            removeLoadingMessage()
            addTechnicalDifficultyMessage("Sesi chat belum siap.")
            return
        }

        if (!::generativeModel.isInitialized) {
            Log.e("ChatActivity", "GenerativeModel not initialized. Cannot send message.")
            removeLoadingMessage()
            addTechnicalDifficultyMessage("AI belum siap.")
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
                    val blockReason = response.candidates.firstOrNull()?.finishReason
                    val safetyRatings = response.candidates.firstOrNull()?.safetyRatings
                    Log.w("ChatActivity", "AI response text is null. Block reason: $blockReason, Safety Ratings: $safetyRatings")
                    addTechnicalDifficultyMessage("Tidak ada respons teks dari AI. Kemungkinan diblokir karena alasan keamanan atau lainnya.")
                }

                // Hapus atau komentari blok catch untuk GenerationException dan RequestException jika unresolved
                /*
                } catch (e: GenerationException) {
                    Log.e("ChatActivity", "GenerationException: ${e.message}", e)
                    removeLoadingMessage()
                    addTechnicalDifficultyMessage("Error saat menghasilkan respons: ${e.message?.take(100)}")
                */
            } catch (e: ServerException) {
                Log.e("ChatActivity", "ServerException: ${e.message}", e)
                removeLoadingMessage()
                addTechnicalDifficultyMessage("Error server dari AI: ${e.message?.take(100)}")
                /*
                } catch (e: RequestException) {
                    Log.e("ChatActivity", "RequestException: ${e.message}", e)
                    removeLoadingMessage()
                    addTechnicalDifficultyMessage("Error permintaan ke AI: ${e.message?.take(100)}")
                */
            }
            catch (e: Exception) { // Tangkap semua exception lain sebagai fallback
                Log.e("ChatActivity", "Generic error sending message: ${e.message}", e)
                removeLoadingMessage()
                addTechnicalDifficultyMessage()
            }
        }
    }

    private fun addTechnicalDifficultyMessage(customMessage: String? = null) {
        val errorMessage = customMessage ?: "Maaf, terjadi sedikit kendala teknis saat menghubungi AI."
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
