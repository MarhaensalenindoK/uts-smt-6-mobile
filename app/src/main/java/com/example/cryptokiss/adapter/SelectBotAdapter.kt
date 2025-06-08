package com.example.cryptokiss.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cryptokiss.R
import com.example.cryptokiss.model.BotPrompt

class SelectBotAdapter(
    private val botList: List<BotPrompt>,
    private val onItemClick: (BotPrompt) -> Unit
) : RecyclerView.Adapter<SelectBotAdapter.SelectViewHolder>() {

    class SelectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_select_bot_name)
        val promptSnippetTextView: TextView = itemView.findViewById(R.id.text_view_select_bot_prompt_snippet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_bot, parent, false)
        return SelectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectViewHolder, position: Int) {
        val bot = botList[position]
        holder.nameTextView.text = bot.name
        holder.promptSnippetTextView.text = bot.prompt
        holder.itemView.setOnClickListener { onItemClick(bot) }
    }

    override fun getItemCount() = botList.size
}
