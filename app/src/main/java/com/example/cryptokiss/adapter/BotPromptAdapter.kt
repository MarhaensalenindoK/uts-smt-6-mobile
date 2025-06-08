package com.example.cryptokiss.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cryptokiss.R
import com.example.cryptokiss.model.BotPrompt

class BotPromptAdapter(
    private var botList: List<BotPrompt>,
    private val onEditClick: (BotPrompt) -> Unit,
    private val onDeleteClick: (BotPrompt) -> Unit
) : RecyclerView.Adapter<BotPromptAdapter.BotViewHolder>() {

    class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_bot_name)
        val promptSnippetTextView: TextView = itemView.findViewById(R.id.text_view_bot_prompt_snippet)
        val editButton: ImageButton = itemView.findViewById(R.id.button_edit_bot)
        val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete_bot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bot_prompt, parent, false)
        return BotViewHolder(view)
    }

    override fun onBindViewHolder(holder: BotViewHolder, position: Int) {
        val bot = botList[position]
        holder.nameTextView.text = bot.name
        holder.promptSnippetTextView.text = bot.prompt
        holder.editButton.setOnClickListener { onEditClick(bot) }
        holder.deleteButton.setOnClickListener { onDeleteClick(bot) }
    }

    override fun getItemCount() = botList.size

    fun updateData(newBotList: List<BotPrompt>) {
        botList = newBotList
        notifyDataSetChanged()
    }
}
