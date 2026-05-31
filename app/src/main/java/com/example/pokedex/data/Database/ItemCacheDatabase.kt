package com.example.pokedex.data.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pokedex.data.model.ItemData

class ItemCacheDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Tên file Database riêng biệt cho Item
        private const val DATABASE_NAME = "item_offline_cache.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_CACHE = "item_cache"

        // Các cột lưu trữ (Không cần ID vì Tên món đồ không bao giờ trùng)
        private const val KEY_NAME = "name" // Khóa chính (Primary Key)
        private const val KEY_IMAGE = "image_url"
        private const val KEY_INTRODUCE = "introduce"
        private const val KEY_EFFECT = "effect"
        private const val KEY_CATEGORY_COST = "category_cost"
        private const val KEY_CATEGORY_NAME = "category_name" // Cột quan trọng để Lọc
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createCacheTable = ("CREATE TABLE $TABLE_CACHE ("
                + "$KEY_NAME TEXT PRIMARY KEY,"
                + "$KEY_IMAGE TEXT,"
                + "$KEY_INTRODUCE TEXT,"
                + "$KEY_EFFECT TEXT,"
                + "$KEY_CATEGORY_COST TEXT,"
                + "$KEY_CATEGORY_NAME TEXT)")
        db?.execSQL(createCacheTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CACHE")
        onCreate(db)
    }

    // =========================================================================
    // 1. CẤT ĐỒ VÀO KHO (Gắn thêm mác categoryName để biết thuộc Tab nào)
    // =========================================================================
    fun insertCache(itemList: List<ItemData>, categoryName: String) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (item in itemList) {
                val values = ContentValues().apply {
                    put(KEY_NAME, item.itemName)
                    put(KEY_IMAGE, item.imageUrl)
                    put(KEY_INTRODUCE, item.itemIntroduce)
                    put(KEY_EFFECT, item.effectItem)
                    put(KEY_CATEGORY_COST, item.categoryCost)
                    put(KEY_CATEGORY_NAME, categoryName) // Dán mác cho nó
                }
                // Dùng CONFLICT_REPLACE: Nếu đã có món này rồi thì cập nhật, ko sợ Crash!
                db.insertWithOnConflict(TABLE_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    // =========================================================================
    // 2. LẤY TẤT CẢ TỪ KHO (Dành riêng cho Tab "ALL")
    // =========================================================================
    fun getAllCache(limit: Int, offset: Int): List<ItemData> {
        val list = ArrayList<ItemData>()
        val db = this.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CACHE LIMIT ? OFFSET ?",
            arrayOf(limit.toString(), offset.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
                val introduce = cursor.getString(cursor.getColumnIndexOrThrow(KEY_INTRODUCE))
                val effect = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EFFECT))
                val categoryCost = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COST))

                list.add(ItemData(imageUrl, name, introduce, effect, categoryCost))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // =========================================================================
    // 3. LỌC ĐỒ TRONG KHO (Dành cho các Tab khác, dùng lệnh WHERE)
    // =========================================================================
    fun getCacheByCategory(categoryName: String, limit: Int, offset: Int): List<ItemData> {
        val list = ArrayList<ItemData>()
        val db = this.readableDatabase

        // Lệnh SQL: Tìm những món có chung mác Category và cắt đúng số lượng
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CACHE WHERE $KEY_CATEGORY_NAME = ? LIMIT ? OFFSET ?",
            arrayOf(categoryName, limit.toString(), offset.toString())
        )

        if (cursor.moveToFirst()) {
            do {
                val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
                val introduce = cursor.getString(cursor.getColumnIndexOrThrow(KEY_INTRODUCE))
                val effect = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EFFECT))
                val categoryCost = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_COST))

                list.add(ItemData(imageUrl, name, introduce, effect, categoryCost))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // =========================================================================
    // 4. XÓA SẠCH KHO
    // =========================================================================
    fun clearAllCache() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_CACHE")
        db.close()
    }
}