package com.example.pokedex.data.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pokedex.data.model.DataPokemon
import com.google.gson.Gson
import kotlin.apply
import kotlin.collections.toList
import kotlin.jvm.java
import kotlin.text.isNullOrEmpty

class TeamDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "my_team_pokedex.db"
        // ĐỔI VERSION THÀNH 2 để app tự động xóa bảng cũ và tạo bảng mới có cột chiêu thức
        private const val DATABASE_VERSION = 2

        private const val TABLE_TEAM = "my_team"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_IMAGE = "image_url"
        private const val KEY_TYPES = "types_json"
        // THÊM CỘT LƯU 4 CHIÊU THỨC
        private const val KEY_MOVES = "moves_json"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Thêm cột KEY_MOVES vào câu lệnh khởi tạo bảng
        val createTableQuery = ("CREATE TABLE $TABLE_TEAM ("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_NAME TEXT,"
                + "$KEY_IMAGE TEXT,"
                + "$KEY_TYPES TEXT,"
                + "$KEY_MOVES TEXT)") // Cột mới ở đây
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TEAM")
        onCreate(db)
    }

    fun getTeamCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_TEAM", null)
        var count = 0
        if (cursor.moveToFirst()) { count = cursor.getInt(0) }
        cursor.close()
        db.close()
        return count
    }

    // Thêm mặc định vào đội (lúc mới thêm thì mảng chiêu thức sẽ trống)
    fun addPokemonToTeam(pokemon: DataPokemon): Pair<Boolean, String> {
        if (getTeamCount() >= 6) {
            return Pair(false, "Đội hình đã đầy (Tối đa 6)!")
        }

        val dbCheck = this.readableDatabase
        val cursor = dbCheck.rawQuery("SELECT 1 FROM $TABLE_TEAM WHERE $KEY_ID = ?",
            arrayOf(pokemon.id.toString())
        )
        val exists = cursor.count > 0
        cursor.close()
        if (exists) {
            dbCheck.close()
            return Pair(false, "Pokemon này đã có trong đội hình!")
        }
        dbCheck.close()

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_ID, pokemon.id)
            put(KEY_NAME, pokemon.name)
            put(KEY_IMAGE, pokemon.imageUrl)
            put(KEY_TYPES, Gson().toJson(pokemon.types))
            // Mới bắt vào đội thì cho danh sách chiêu thức trống rỗng
            put(KEY_MOVES, Gson().toJson(arrayListOf("---", "---", "---", "---")))
        }

        val result = db.insert(TABLE_TEAM, null, values)
        db.close()
        return if (result != -1L) Pair(true, "Đã thêm vào đội hình!") else Pair(false, "Lỗi!")
    }

    // Cập nhật 4 chiêu thức được chọn vào SQLite
    fun updatePokemonMoves(pokemonId: Int, selectedMoves: List<String>): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_MOVES, Gson().toJson(selectedMoves)) // Biến mảng thành chuỗi JSON để lưu
        }
        val result = db.update(TABLE_TEAM, values, "$KEY_ID = ?", arrayOf(pokemonId.toString()))
        db.close()
        return result > 0
    }

    // Lấy danh sách 4 chiêu thức đã lưu của một Pokemon ra ngoài
    fun getSavedMoves(pokemonId: Int): List<String> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $KEY_MOVES FROM $TABLE_TEAM WHERE $KEY_ID = ?",
            arrayOf(pokemonId.toString())
        )
        var list = listOf("---", "---", "---", "---")
        if (cursor.moveToFirst()) {
            val json = cursor.getString(0)
            if (!json.isNullOrEmpty()) {
                list = Gson().fromJson(json, Array<String>::class.java).toList()
            }
        }
        cursor.close()
        db.close()
        return list
    }

    fun removePokemonFromTeam(pokemonId: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_TEAM, "$KEY_ID = ?", arrayOf(pokemonId.toString()))
        db.close()
        return result > 0
    }

    fun getTeam(): List<DataPokemon> {
        val list = kotlin.collections.ArrayList<DataPokemon>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TEAM", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
                val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE))
                val typesJson = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPES))
                val typesList = Gson().fromJson(typesJson, Array<String>::class.java).toList()
                list.add(DataPokemon(id, name, typesList, imageUrl))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}