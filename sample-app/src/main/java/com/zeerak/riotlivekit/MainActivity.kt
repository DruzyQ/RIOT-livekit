package com.zeerak.riotlivekit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.SpannableStringBuilder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.zeerak.riotlivekit.databinding.MainActivityBinding


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainActivityBinding.inflate(layoutInflater)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val urlString = preferences.getString(PREFERENCES_KEY_URL, URL)
        val tokenString = preferences.getString(PREFERENCES_KEY_TOKEN, TOKEN)
        binding.run {
            url.editText?.text = SpannableStringBuilder(urlString)
            token.editText?.text = SpannableStringBuilder(tokenString)
            connectButton.setOnClickListener {
                val intent = Intent(this@MainActivity, CallActivity::class.java).apply {
                    putExtra(
                        CallActivity.KEY_ARGS,
                        CallActivity.BundleArgs(
                            url.editText?.text.toString(),
                            token.editText?.text.toString()
                        )
                    )
                }

                startActivity(intent)
            }

            saveButton.setOnClickListener {
                preferences.edit {
                    putString(PREFERENCES_KEY_URL, url.editText?.text.toString())
                    putString(PREFERENCES_KEY_TOKEN, token.editText?.text.toString())
                }

                Toast.makeText(
                    this@MainActivity,
                    "Values saved.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            resetButton.setOnClickListener {
                preferences.edit {
                    clear()
                }
                url.editText?.text = SpannableStringBuilder(URL)
                token.editText?.text = SpannableStringBuilder(TOKEN)

                Toast.makeText(
                    this@MainActivity,
                    "Values reset.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setContentView(binding.root)

        requestPermissions()

    }

    private fun requestPermissions() {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { grants ->
                for (grant in grants.entries) {
                    if (!grant.value) {
                        Toast.makeText(
                            this,
                            "Missing permission: ${grant.key}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        val neededPermissions = listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
            .filter {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_DENIED
            }
            .toTypedArray()
        if (neededPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(neededPermissions)
        }
    }

    companion object {
        const val PREFERENCES_KEY_URL = "url"
        const val PREFERENCES_KEY_TOKEN = "token"


        const val URL = "wss://stephen-dzasntmt.livekit.cloud"

         var TOKEN =
            if(Constants.isListener)
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MDU0Mjg2MTUsImlzcyI6IkFQSUF6Rm52aG5GenZFZSIsIm5iZiI6MTcwNDc3ODYxNSwic3ViIjoiRHJ1enlCIiwidmlkZW8iOnsiY2FuUHVibGlzaCI6dHJ1ZSwiY2FuUHVibGlzaERhdGEiOnRydWUsImNhblN1YnNjcmliZSI6dHJ1ZSwicm9vbSI6Im9mZmljZSIsInJvb21Kb2luIjp0cnVlfX0.qkMzpY8-dhc_XQ1gXvSWQSWFPNRM1UZM-lbl-1R5NQs"
            else
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MDU0Mjg2NTMsImlzcyI6IkFQSUF6Rm52aG5GenZFZSIsIm5iZiI6MTcwNDc3ODY1Mywic3ViIjoiRHJ1enkiLCJ2aWRlbyI6eyJjYW5QdWJsaXNoIjp0cnVlLCJjYW5QdWJsaXNoRGF0YSI6dHJ1ZSwiY2FuU3Vic2NyaWJlIjp0cnVlLCJyb29tIjoib2ZmaWNlIiwicm9vbUpvaW4iOnRydWV9fQ.6Pm2L8_Mi7A3EAYNyvyagbwYbe_Kn_K5zbRZObSeZ0Y"

    }
}