package com.aleksandar.novic.animation

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aleksandar.novic.data.model.Animation
import com.aleksandar.novic.data.usecase.GetAnimations
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@SuppressLint("CheckResult")
class AnimationsViewModel : ViewModel() {

    private val animationMutableLiveData = MutableLiveData<List<Animation>>()
    val animationsLiveData: LiveData<List<Animation>> = animationMutableLiveData

    init {
        refresh()
    }

    fun refresh() {
        GetAnimations.get()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t1, t2 ->
                animationMutableLiveData.value = t1
            }
    }
}