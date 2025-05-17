package com.example.cryptokiss

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val messageList: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatMessageViewHolder>() {

    // ViewHolder untuk setiap item pesan
    class ChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageRootLayout: LinearLayout = itemView.findViewById(R.id.chat_message_root_layout)
        val messageBubble: CardView = itemView.findViewById(R.id.card_chat_message_bubble)
        val messageContent: TextView = itemView.findViewById(R.id.text_view_chat_message_content)
        // Pastikan Anda sudah menambahkan TextView untuk timestamp di item_chat_message.xml
        // dan uncomment baris di bawah jika ingin menampilkan timestamp.
        // Beri ID misal: android:id="@+id/text_view_chat_timestamp"
        val messageTimestamp: TextView? = itemView.findViewById(R.id.text_view_chat_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatMessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        val currentMessage = messageList[position]

        holder.messageContent.text = currentMessage.text

        if (currentMessage.senderType == SenderType.USER) {
            holder.messageRootLayout.gravity = android.view.Gravity.END
            holder.messageBubble.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.chat_user_bubble)
            )
            holder.messageContent.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.chat_text_light)
            )
            // Atur warna timestamp jika berbeda untuk pengguna
            holder.messageTimestamp?.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white_text_secondary) // Contoh
            )
        } else {
            holder.messageRootLayout.gravity = android.view.Gravity.START
            holder.messageBubble.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.chat_ai_bubble)
            )
            holder.messageContent.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.chat_text_light)
            )
            // Atur warna timestamp jika berbeda untuk AI
            holder.messageTimestamp?.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white_text_secondary) // Contoh
            )
        }

        // Set timestamp jika TextView untuk timestamp ada
        holder.messageTimestamp?.let {
            it.text = formatTimestamp(currentMessage.timestamp)
            it.visibility = View.VISIBLE // Pastikan terlihat
        } ?: run {
            // Jika TextView timestamp tidak ditemukan, pastikan tidak ada error atau handle dengan baik
            // Untuk sekarang, kita bisa abaikan jika tidak ada
        }
    }

    override fun getItemCount() = messageList.size

    // Fungsi untuk memformat timestamp Long ke String yang mudah dibaca
    private fun formatTimestamp(timestamp: Long): String {
        // Format: "10:30 AM" atau "14:45" (tergantung locale default)
        // Anda bisa mengganti pola ini sesuai keinginan
        // "hh:mm a" -> 12 jam dengan AM/PM
        // "HH:mm" -> 24 jam
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
