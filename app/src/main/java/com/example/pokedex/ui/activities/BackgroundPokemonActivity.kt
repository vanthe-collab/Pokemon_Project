package com.example.pokedex.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.pokedex.databinding.ActivityBackgroundPokemonBinding
import com.example.pokedex.ultils.NotificationHelper

class BackgroundPokemonActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBackgroundPokemonBinding
    private var REQ = 999999
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackgroundPokemonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        NotificationHelper.createNotificationChannel(this)
        if (checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {

        } else {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQ)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ && permissions.size == grantResults.size) {
            for (item in grantResults) {
                if (item != PackageManager.PERMISSION_GRANTED) {
                    finishAffinity()
                    return
                }
            }
        }
    }
}