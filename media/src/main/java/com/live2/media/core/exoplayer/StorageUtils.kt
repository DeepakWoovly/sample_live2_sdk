package com.example.videosdk.core

import android.os.StatFs
import java.io.File

class StorageUtils {
    companion object {
        fun calculateDiskCacheSize(dir: File, minDiskCacheSize: Long, maxDiskCacheSize: Long, percentage: Float = 1.0f): Long {
            var size = minDiskCacheSize
            try {
                val statFs = StatFs(dir.absolutePath)
                val available = statFs.blockCountLong * statFs.blockSizeLong
                size = (available * percentage / 50).toLong()
            } catch (ignored: IllegalArgumentException) {
            }
            // Bound inside min/max size for disk cache.
            return size.coerceAtMost(maxDiskCacheSize).coerceAtLeast(minDiskCacheSize)
        }
    }
}