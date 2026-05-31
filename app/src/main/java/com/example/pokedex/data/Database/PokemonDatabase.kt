package com.example.pokedex.data.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pokedex.data.model.DataPokemon
import com.google.gson.Gson

class PokedexCacheDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Tên file Database hoàn toàn khác biệt, không sợ đụng hàng!
        private const val DATABASE_NAME = "pokedex_offline_cache.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_CACHE = "pokedex_cache"

        // Các cột lưu trữ
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_IMAGE = "image_url"
        private const val KEY_TYPES = "types_json"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createCacheTable = ("CREATE TABLE $TABLE_CACHE ("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_NAME TEXT,"
                + "$KEY_IMAGE TEXT,"
                + "$KEY_TYPES TEXT)")
        db?.execSQL(createCacheTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CACHE")
        onCreate(db)
    }

    // =========================================================================
    // CẤT ĐỒ VÀO KHO (Dùng Transaction để lưu cực nhanh)
    // =========================================================================
    fun insertCache(pokemonList: List<DataPokemon>) {
        val db = this.writableDatabase
        db.beginTransaction() // Bật chế độ lưu tốc độ cao
        try {
            for (pokemon in pokemonList) {
                val values = ContentValues().apply {
                    put(KEY_ID, pokemon.id)
                    put(KEY_NAME, pokemon.name)
                    put(KEY_IMAGE, pokemon.imageUrl)
                    put(KEY_TYPES, Gson().toJson(pokemon.types))
                }
                // Dùng CONFLICT_REPLACE: Trùng ID thì tự động cập nhật, ko sợ Crash!
                db.insertWithOnConflict(TABLE_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    // =========================================================================
    //2. LẤY ĐỒ TỪ KHO RA (Có hỗ trợ cuộn phân trang)
    // =========================================================================
    fun getCache(limit: Int, offset: Int): List<DataPokemon> {
        val list = ArrayList<DataPokemon>()
        val db = this.readableDatabase

        // Lệnh SQL: Lấy đúng số lượng (limit) bắt đầu từ vị trí (offset)
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CACHE LIMIT ? OFFSET ?",
            arrayOf(limit.toString(), offset.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
                val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE))
                val typesJson = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPES))

                // Giải mã chuỗi JSON về lại List<String>
                val typesList = Gson().fromJson(typesJson, Array<String>::class.java).toList()

                list.add(DataPokemon(id, name, typesList, imageUrl))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // Lấy 1 con Pokemon từ Cache bằng ID
    fun getPokemonFromCacheById(id: Int): DataPokemon? {
        val db = this.readableDatabase
        // Tìm trong bảng Cache xem có con nào mang cái ID này không
        val cursor = db.rawQuery("SELECT * FROM pokedex_cache WHERE id = ?", arrayOf(id.toString()))

        var pokemon: DataPokemon? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"))
            val typesJson = cursor.getString(cursor.getColumnIndexOrThrow("types_json"))
            val typesList = Gson().fromJson(typesJson, Array<String>::class.java).toList()

            pokemon = DataPokemon(id, name, typesList, imageUrl)
        }
        cursor.close()
        db.close()
        return pokemon
    }


    // =========================================================================
    // 3. XÓA SẠCH KHO (Dành cho trường hợp muốn làm nút "Clear Cache")
    // =========================================================================
    fun clearAllCache() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_CACHE")
        db.close()
    }
}