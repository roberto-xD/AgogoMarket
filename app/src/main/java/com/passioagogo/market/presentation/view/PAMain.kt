package com.passioagogo.market.presentation.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.passioagogo.market.presentation.navigation.PANavigation
import com.passioagogo.market.ui.theme.PassioAgogoMarketTheme

class PAMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PassioAgogoMarketTheme {
                PANavigation()
            }
        }
    }
}
