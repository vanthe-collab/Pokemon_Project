package com.example.pokedex.data.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pokedex.data.model.DataPokemon
import com.google.gson.Gson

class FavoriteDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "favorite_pokedex.db"
        private const val DATABASE_VERSION = 1

        // Cấu hình Tên bảng và các Cột
        private const val TABLE_FAVORITE = "favorite_pokemon"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_IMAGE = "image_url"
        private const val KEY_TYPES = "types_json"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = ("CREATE TABLE $TABLE_FAVORITE ("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_NAME TEXT,"
                + "$KEY_IMAGE TEXT,"
                + "$KEY_TYPES TEXT)")
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITE")
        onCreate(db)
    }

    // 🔥 HÀM 1: Thêm Pokemon vào danh sách yêu thích
    fun addFavorite(pokemon: DataPokemon): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_ID, pokemon.id)
            put(KEY_NAME, pokemon.name)
            put(KEY_IMAGE, pokemon.imageUrl)
            put(KEY_TYPES, Gson().toJson(pokemon.types)) // Chuyển List<String> thành JSON String
        }

        val result = db.insertWithOnConflict(TABLE_FAVORITE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        return result != -1L
    }

    // 🔥 HÀM 2: Xóa Pokemon khỏi danh sách yêu thích
    fun removeFavorite(pokemon: DataPokemon): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_FAVORITE, "$KEY_ID = ?", arrayOf(pokemon.id.toString()))
        db.close()
        return result
    }

    // 🔥 HÀM 3: Kiểm tra xem Pokemon này đã được thích chưa
    fun isFavorite(pokemon: DataPokemon): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLE_FAVORITE WHERE $KEY_ID = ?", arrayOf(pokemon.id.toString()))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // 🔥 HÀM 4: Lấy toàn bộ danh sách Pokemon đã thích ra ngoài
    fun getFavorites(): List<DataPokemon> {
        val list = ArrayList<DataPokemon>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FAVORITE", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
                val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE))
                val typesJson = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPES))

                // Map ngược JSON String về lại mảng List<String> cho đối tượng
                val typesList = Gson().fromJson(typesJson, Array<String>::class.java).toList()

                list.add(DataPokemon(id, name, typesList, imageUrl))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}