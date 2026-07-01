package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FinanceViewModel
import com.example.ui.screens.FinanceApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    com.example.ui.theme.initThemePreference(applicationContext)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: FinanceViewModel = viewModel()
        FinanceApp(viewModel)
      }
    }
  }
}
