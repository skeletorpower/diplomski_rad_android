package com.aleksandar.novic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavHostController
import androidx.navigation.findNavController

/*Ovo je glavni Activity aplikacije
* Activity predstavlja glavni controller u Android system-u. On ujedno predstavlja i jedan ekran. Vise aktivitija su vise ekrana aplikacije,
* koje imaju sistemske animirane tranzicije kako bi korisnik imao dozivljaj vise ekrana u aplikaciji
* U modernom programiranju Android aplikacija koristi se jedan Activity koji ima Fragmente kao ekrane. Vise o fragmentima u tim klasama*/
class MainActivity : AppCompatActivity(){

    /*NavController predstavlja ruter nase navigacije. Dobija se pomocu Kotlinovih ekstenzija i delegata. Ove Kotlinove funkcije implementirao je Google.
    * Ono sto je potrebno da prosledimo funkcijama je definisan navigacioni graf u obliku xmla. Xml file je prosledjen preko svog id-a koji generise Android Studio = R.id.nav_host
    * pritiskom na ctl i levim klikom na id, IDE odvodi programera u xml file.*/
    val navController: NavHostController by lazy { findNavController(R.id.nav_host) as NavHostController }

    /*Activity ima svoje callback metode koji oznacavaju lifecycle activity-a. Ono sto je bitno da klasu Activity-a ne instancira programer vec system.*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*U onCreate metodi Activity-a se setuje xml file koji definise layout naseg activity-a. IDE moze da navigira na taj file pomocu ctrl + levi click na id file-a*/
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        navController.enableOnBackPressed(true)
    }
}