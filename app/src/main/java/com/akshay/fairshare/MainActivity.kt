package com.akshay.fairshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.akshay.fairshare.ui.group.GroupScreen
import com.akshay.fairshare.ui.theme.FairShareTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FairShareTheme {
                GroupScreen()
            }
        }
    }
}
