package com.passioagogo.market

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.passioagogo.market.ui.session.AppRoot
import com.passioagogo.market.ui.theme.PassioAgogoMarketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PassioAgogoMarketTheme {
                AppRoot()
            }
        }
    }
}
