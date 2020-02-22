package com.aleksandar.novic

import android.app.Application

/*Glavna klasa koja nasledjuje klasu Application Android framework-a
* ona nam sluzi da ispratimo lifecycle aplikacije (kada je ona kreirana ili kada je ona unistena)
* Application klasa je uvek sigleton*/
class MyApplication: Application(){

    override fun onCreate() {
        super.onCreate()
        /*Setovanje INSTANCE na this koji je singleton, u trenutku kada je aplikacija kreirana*/
        INSTANCE = this
    }

    /*Companion object je Kotlinov nacin da oznacimo staticke promenljive ili metode
    * INSTANCE promenljivu u onCreate() setujemo na this
    * Ovu promenljivu cemo koristiti kao vezu izmedju nase aplikacije i Android sistema*/
    companion object{
        lateinit var INSTANCE: Application
    }
}