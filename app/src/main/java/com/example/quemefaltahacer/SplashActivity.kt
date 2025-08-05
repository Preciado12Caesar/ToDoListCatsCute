package com.example.quemefaltahacer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.quemefaltahacer.MainActivity
import com.example.quemefaltahacer.R
import com.example.quemefaltahacer.ui.theme.QueMeFaltaHacerTheme
import com.example.quemefaltahacer.ui.theme.splash
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            QueMeFaltaHacerTheme {

                LaunchedEffect(key1 = true ) {
                    delay(2000)
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }
                Box(
                    modifier = Modifier.fillMaxSize()
                    .background(splash),
                    contentAlignment = Alignment.Center

                ) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null //
                    )
                }
            }
        }



    }
}
