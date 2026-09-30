package com.antibudget.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bảng lưu trữ danh mục tài sản tích lũy theo 3 Lọ
 * Mục tiêu: Tích lũy 600 triệu đồng tuổi 40
 */
@Entity(tableName = "wealth_assets")
data class WealthAssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bucket: WealthBucket,
    val name: String,
    val units: Double,
    val unitPrice: Double,
    val totalValue: Double,
    val updatedAt: Long = System.currentTimeMillis()
)
