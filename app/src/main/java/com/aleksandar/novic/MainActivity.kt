package com.aleksandar.novic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavHostController
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity(){

    val navController: NavHostController by lazy { findNavController(R.id.nav_host) as NavHostController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        navController.enableOnBackPressed(true)
    }
}