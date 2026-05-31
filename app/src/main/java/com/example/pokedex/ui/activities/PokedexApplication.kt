package com.example.pokedex.ui.activities

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.pokedex.ultils.NotificationHelper

class PokedexApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Tạo Channel ngay khi app vừa khởi chạy
        NotificationHelper.createNotificationChannel(this)

        // 2. Lắng nghe trạng thái toàn bộ ứng dụng
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> {
                        // Khi người dùng thoát ra Home (App rơi vào background)
                        NotificationHelper.showBackgroundNotification(this@PokedexApplication)
                    }
                    Lifecycle.Event.ON_START -> {
                        // Khi người dùng mở lại App (App lên foreground)
                        NotificationHelper.cancelNotification(this@PokedexApplication)
                    }
                    else -> {}
                }
            }
        )
    }
}