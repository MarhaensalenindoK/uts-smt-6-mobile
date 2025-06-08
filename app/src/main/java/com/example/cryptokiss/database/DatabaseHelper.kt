package com.example.cryptokiss.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.cryptokiss.model.BotPrompt

// Kelas untuk mengelola semua operasi database (CRUD)
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "CryptoKISS.db"
        private const val TABLE_PROMPTS = "prompts"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_PROMPT = "prompt"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_PROMPTS("
                + "$KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$KEY_NAME TEXT,"
                + "$KEY_PROMPT TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PROMPTS")
        onCreate(db)
    }

    // Create
    fun addBotPrompt(bot: BotPrompt): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(KEY_NAME, bot.name)
            put(KEY_PROMPT, bot.prompt)
        }
        val success = db.insert(TABLE_PROMPTS, null, contentValues)
        db.close()
        return success
    }

    // Read
    fun getAllBotPrompts(): List<BotPrompt> {
        val botList = ArrayList<BotPrompt>()
        val selectQuery = "SELECT * FROM $TABLE_PROMPTS ORDER BY $KEY_ID DESC"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val bot = BotPrompt(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),
                        prompt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROMPT))
                    )
                    botList.add(bot)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
            db.close()
        }
        return botList
    }

    // Update
    fun updateBotPrompt(bot: BotPrompt): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(KEY_NAME, bot.name)
            put(KEY_PROMPT, bot.prompt)
        }
        val success = db.update(TABLE_PROMPTS, contentValues, "$KEY_ID=?", arrayOf(bot.id.toString()))
        db.close()
        return success
    }

    // Delete
    fun deleteBotPrompt(bot: BotPrompt): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_PROMPTS, "$KEY_ID=?", arrayOf(bot.id.toString()))
        db.close()
        return success
    }
}
