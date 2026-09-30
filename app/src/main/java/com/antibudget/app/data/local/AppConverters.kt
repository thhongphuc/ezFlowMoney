package com.antibudget.app.data.local

import androidx.room.TypeConverter
import com.antibudget.app.data.local.entity.BucketType
import com.antibudget.app.data.local.entity.WealthBucket

/**
 * Bộ chuyển đổi kiểu dữ liệu cho Room Database
 */
class AppConverters {

    @TypeConverter
    fun fromBucketType(value: BucketType): String = value.name

    @TypeConverter
    fun toBucketType(value: String): BucketType {
        return try {
            BucketType.valueOf(value)
        } catch (e: Exception) {
            BucketType.VARIABLE
        }
    }

    @TypeConverter
    fun fromWealthBucket(value: WealthBucket): String = value.name

    @TypeConverter
    fun toWealthBucket(value: String): WealthBucket {
        return try {
            WealthBucket.valueOf(value)
        } catch (e: Exception) {
            WealthBucket.BANK_SAVINGS
        }
    }
}
