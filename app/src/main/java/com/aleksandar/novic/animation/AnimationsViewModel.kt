package com.aleksandar.novic.animation

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aleksandar.novic.data.model.Animation
import com.aleksandar.novic.data.usecase.GetAnimations
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/*Ovo je viewModel za nas AnimationsFragment
* Ovo je preporucena praksa Android tima koja izdvaja odredjenu logiku za koju fragment ne mora da zna
* ViewModel-i se isto instanciraju pomocu sistema jer tako imamo funkcionalnosti da je viewModel svestan lifecycle-a fragment-a*/
@SuppressLint("CheckResult")
class AnimationsViewModel : ViewModel() {

    /*Ovo je mutableLiveData na koju postujemo dobijene podatke
    * ona je private i ne moze se do nje doci van klase*/
    private val animationMutableLiveData = MutableLiveData<List<Animation>>()
    /*Ovo je API za dolazenje do liveData van klase.
    * MutableLiveData implementira i LiveData interface i samim tim mutableLiveData-u koju smo kreirali malo pre
    * setujemo na referencu liveData koja je public i onda fragment ima pristup liveData na koju ne moze da postuje neku vrednost vec samo da slusa*/
    val animationsLiveData: LiveData<List<Animation>> = animationMutableLiveData

    /*inicijalizacija u kojoj pozivamo poziv za dobijanje podataka*/
    init {
        refresh()
    }

    fun refresh() {
        /*GetAnimation je kotlinova object klasa. Pri definisanju klase umesto reci class mi koristimo object rec i time
        * smo ustvari implementirali Singleton, pri cemu ukoliko zelimo da pozovemo neki metod ili property te klase samo treba navesti naziv klase sa tackom
        * i pozove se metod pr. GetAnimations.get()*/
        GetAnimations.get()
                /*Dobijanje podataka i hendlanje multitredinga je implementirano pomocu rxJava-e
                * to je popularna biblioteka za Javu koja implementira Reactive programming*/
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
                /*subscribe metod se poziva kada hocemo da dobijemo podatke*/
            .subscribe { animationList, error ->
                /*kada smo dobili podatke setujemo podatke na nasu mutableLiveData-u*/
                animationMutableLiveData.value = animationList
            }
    }
}