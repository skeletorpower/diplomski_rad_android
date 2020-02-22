package com.aleksandar.novic.data.usecase

import android.util.Log.e
import com.aleksandar.novic.MyApplication
import com.aleksandar.novic.data.model.*
import com.aleksandar.novic.data.network.AnimationsService
import com.aleksandar.novic.data.network.RetrofitConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import kotlin.random.Random

/*Ovo je najvazniji useCase u aplikaciji
* Sluzi za dobijanje liste animacija sa servera i pretvaranje u listu Animation objekata koju koristimo posle u celoj aplikaciji */
object GetAnimations {

    /*Gson predstavlja biblioteku koja json pretvara u Java objekte*/
    private val gson = Gson()

    /*Ovo je instanciranje service-a (java interface) pomocu retrofit biblioteke*/
    private val animationsService: AnimationsService = RetrofitConfig.getAnimationService()

    /*Metod koji pravi listu Animation objekata*/
    fun get(): Single<List<Animation>>{
        /*poziva service i dobija listu AnimationData
        * nad tom listom poziva map finkciju*/return animationsService.getAnimations().map {
            /*vraca promenjenu listu u listu Animation objekata
            * svaki AnimationData pretvara u Animation objekat*/return@map it.map { animationData ->
                animationDataToAnimation(animationData) }
        }
    }

    private fun animationDataToAnimation(animationData: AnimationData): Animation{
        /*id, name, broj like i broj dislike se mapiraju 1:1
        * dok lottieFile ima svoj algoritam - getJsonAnimation*/
        return Animation(animationData.id, animationData.name, animationData.likes, animationData.dislikes, getJsonAnimation(animationData.keyframes, animationData.time))
    }

    /*Prihvata listu keyFrame-ova i vreme koliko animacija treba da traje*/
    private fun getJsonAnimation(keyFrames: List<KeyFrame>, time: Int): String{
        /*uzima naziv random slike iz definisanog obima
        * Te slike su postojane u folderu assets*/
        val image = getRandomImage()
        /*Ovde se kreira Anim objekat iz template-a data.json, pomocu gson biblioteke*/
        var anim: Anim = gson.fromJson(MyApplication.INSTANCE.assets.open("data.json").bufferedReader().readText(), object : TypeToken<Anim>(){}.type)

        /*Prvo sto se menja u template-u je naziv slike koju hocemo da koristimo u toj animacije
        * posto za template znamo da uvek ima samo jednu sliku onda sa sigurnoscu uzimamo prvi element iz liste assets Anim modela*/
        val asset: Asset
        anim.assets.first().let {
            /*Posto je Asset klasa kotlinova data klasa onda imamo vec implementirani copy metod i kopiramo Asset objekat iz
            * template i menjamo mu samo naziv slike
            * time smo dobili novi objekat asset sa promenjenom slikom i svim drugim podacima koji su isti*/
            asset = it.copy(p=image)
        }
        /*Sledece sto menjamo je layer, i to je najbitniji deo
        * u layer-u definisemo koliko da se krece animirani objekat, na koji nacin i pomocu kog interpolatora*/
        val layer: Layer
        /*opet posto znamo da u templateu je uvek samo jedan layer uzimamo prvi iz liste*/
        anim.layers.first().let {
            /*Ovde opet kopiramo layer kako bismo zadrzali podatke iz template-a koji nam trebaju i menjamo one koje zelimo
            * Menjamo polje ks koje predstavlja sve osobine koje mogu da animiraju (scale, position, alpha itd)
            * biramo position da menjamo
            * position ima listu position keyFrame-ova koji su definisani kako lottie moze da cita, pa prema tome morao sam da implementiram
            * algoritam za mapiranje keyFrame-ova sa servera u keyFrame-ove koje cita Lottie animation - keyFrameToPositionKeyFrame */
            layer = it.copy(ks = it.ks.copy(position = it.ks.position.copy(positionKeyFrames = keyFrameToPositionKeyFrame(keyFrames))), op = 30 * time)
        }

        /*Kada sam izmenio sve potrebno u template-u onda kreiram novi Anim objekat sa istim nepromenjenim poljima i sa poljima koje sam zeleo da menjam
        * assets (slike), layers (animacija pozicije) i vreme animacije*/
        anim = anim.copy(assets = listOf(asset), layers = listOf(layer), op = 30 * time)
        /*Da bih sve zavrsio pomocu gson biblioteke objekat pretvaram u json i vracam u obliku string-a*/
        return gson.toJsonTree(anim).toString()
    }

    /*Metod za random dobijanje slika*/
    private fun getRandomImage(): String{
        val random = Random.Default
        val number = random.nextInt(1,8)
        return images[number] ?: ""
    }

    private fun keyFrameToPositionKeyFrame(keyFrames: List<KeyFrame>): List<PositionKeyFrame>{

        if (keyFrames.size < 2) {
            return listOf(PositionKeyFrame(ControlPoint(0.167, 0.167), ControlPoint(0.833, 0.833), keyFrames[0].key_frame, listOf(178, keyFrames[0].value.toInt(),0), listOf(0.0,0.0,0.0), listOf(0.0,0.0,0.0)))
        }

        val listOfPositionKeyFrame = mutableListOf<PositionKeyFrame>()

        keyFrames.forEachIndexed { index, keyFrame ->
            val keyFrameTime = keyFrame.key_frame
            val values = listOf(178, keyFrame.value.toInt(),0)
            var firstControlPoint = ControlPoint(0.167,0.167)
            var secondControlPoint =  ControlPoint(0.833,0.833)
            when(keyFrame.interpolator){
                "linear" -> {
                    firstControlPoint = ControlPoint(0.167, 0.167)
                    secondControlPoint = ControlPoint(0.833, 0.833)
                }
                "easy_ease" -> {
                    if (index >= 1){
                        firstControlPoint = ControlPoint(0.333,0.0)
                        val previousKeyFrame = listOfPositionKeyFrame[index-1]
                        listOfPositionKeyFrame[index-1] = previousKeyFrame.copy(secondControlPoint = ControlPoint(0.667,1.0))
                        secondControlPoint = ControlPoint(0.833,0.833)
                    }
                    if (index < 1){
                        firstControlPoint = ControlPoint(0.333,0.0)
                        secondControlPoint = ControlPoint(0.833,0.833)
                    }
                    if (index > keyFrames.size-2){
                        firstControlPoint = ControlPoint(0.167,0.167)
                        val previousKeyFrame = listOfPositionKeyFrame[index-1]
                        listOfPositionKeyFrame[index-1] = previousKeyFrame.copy(secondControlPoint = ControlPoint(0.667,1.0))
                        secondControlPoint = ControlPoint(0.833,0.833)
                    }
                }
                "easy_ease_out"->{
                    firstControlPoint = ControlPoint(0.333,0.0)
                    secondControlPoint = ControlPoint(0.833,0.833)
                }
                "easy_ease_in"-> {
                    secondControlPoint = ControlPoint(0.833,0.833)
                    firstControlPoint = ControlPoint(0.167, 0.167)
                    if (index >= 1){
                        val previousKeyFrame = listOfPositionKeyFrame[index-1]
                        listOfPositionKeyFrame[index-1] = previousKeyFrame.copy(secondControlPoint = ControlPoint(0.667,1.0))
                    }
                }
            }
            listOfPositionKeyFrame.add(PositionKeyFrame(secondControlPoint, firstControlPoint, keyFrameTime, values, listOf(0.0,0.0,0.0), listOf(0.0,0.0,0.0)))
        }

        return listOfPositionKeyFrame
    }

    private val images = hashMapOf(1 to "baseball.png",
        2 to "basketball.png",
        3 to "beachball.png",
        4 to "football.png",
        5 to "tenis.png",
        6 to "valleyball.png",
        7 to "waterpolo.png")
}