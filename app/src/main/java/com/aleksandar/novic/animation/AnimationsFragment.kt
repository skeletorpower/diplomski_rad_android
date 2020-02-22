package com.aleksandar.novic.animation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aleksandar.novic.MainActivity
import com.aleksandar.novic.R
import com.aleksandar.novic.data.model.Animation
import com.aleksandar.novic.dialog.TriggersDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_animations.*
import kotlinx.android.synthetic.main.item_animation.view.*

/*AnimationsFragment je Fragment klasa. On predstavlja inicijalni ekran u aplikaciji
* Fragment ima takodje svoje callback metode koji predstavljaju lifecycle Fragmenta
* Takodje, fragment ima definisan svoj layout pomocu layout xml file-a*/
class AnimationsFragment : Fragment(R.layout.fragment_animations), Observer<List<Animation>> {

    /*Svaki fragment ima svoj viewModel ukoliko ekran po funkcionalnosti ima slozeniju logiku
    * ViewModele kraira system jer po implementaciji imaju dodatne "moci" da znaju o lifecycle-u ekrana i na taj nacin da cuvaju podatke tako da oni prezivljavaju neke krajnje slucajeve
    * Za kreiranje se koristi Kotlinova funkcija koju implementira Android sistem, kojoj se prosledjuje Potpis klase konkretnog viewModel-a*/
    private val viewModel by viewModels<AnimationsViewModel> { ViewModelProvider.NewInstanceFactory() }

    /*onViewCreated je lifecycle metod fragment-a kada je view kreiran*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /*list je referenca na listu animacija. Listu animacija smo definisali u xml-u layout AnimationsFragment-a
        * list kao referenca je definisana pomocu kotlin extension biblioteke koja u build time-u generise reference i povezuje sa definisanim view-evima iz xml-a.
        * svaka lista mora da ima svog menadzera koji odredjuje kako ce redjati elemente i svoj adapter koji zna o podacima i zna kako da ih poveze sa view-om jednog item-a*/
        list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        list.adapter = AnimationAdapter(requireContext())
        /*listener na klik dugmeta koji sluzi za podesavanje AI-a*/
        button_setting.setOnClickListener {
            val dialog = TriggersDialog()
            dialog.show(requireFragmentManager(), "Triggers")
        }
        /*Setuje se sistem da pri ovom ekranu prikazuje sistemske elemente (status bar, navigation bar itd)*/
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        /*Observer patter: u viewModel-u se nalazi sistemska implementacija observer pattern-a - LiveData. Postoje dve vrste liveData = MutableLiveData i obicna LiveData
        * obicna liveData je nacin da se ekspouzuje API dok MutableLiveData se koristi u viewModel-u kako bismo mogli da postujemo vrednost
        * na obicnu liveDate-u i da obavestimo Observer-a koji je u nasem slucaju nas Fragment jer implementira interface Observer
        * prvo this je owner sto nam da je funkciju da ukoliko fragment nije vise aktivan observer nece ni primati vrednosti
        * drugo this se odnosi na implementaicju Observer interface-a*/
        viewModel.animationsLiveData.observe(this, this)
        /*Ovde se radi rucna provera da li u liveDate-i imamo podatke, ukoliko imamo onda gasimo loading i setujemo date-u u adapter
        * ?.let je kotlinova funkcija da pita da li je nesto null ili nije*/
        viewModel.animationsLiveData.value?.let {
            loader.visibility = View.GONE
            (list.adapter as AnimationAdapter).animations = it
        }
        /*oznacavamo actionBar vidiljivim na ovom ekranu*/
        (requireActivity() as MainActivity).app_bar_layout.visibility = View.VISIBLE
        /*nas metod u viewModel-u koji poziva ponovo API za podatke o animacijama*/
        viewModel.refresh()
    }


    /*Ovo je override-ovan metod iz Interface-a Observer, u ovom metodu smo implementirali popunjavanje liste sa animacijama koje smo dobili preko API-a*/
    override fun onChanged(animationList: List<Animation>?) {
        /*Ovde opet koristimo ?.let i pitamo da li je pristigla lista animacija not null
        * ako nije ulazi u blok koji ima animaciju fade out za loader i setovanje date-e u adapter*/
        animationList?.let {
            loader.startAnimation(AlphaAnimation(1f,0f).apply { duration = 1000; setAnimationListener(object : android.view.animation.Animation.AnimationListener{
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    loader.visibility = View.GONE
                }
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            }) })
            (list.adapter as AnimationAdapter).animations = it
        }
    }
}

/*Ovo je klasa koja predstavlja adapter za listu animacija na ovom ekranu
* Android sistem implementira RecyclerView kao listu item-a, a specificnosti su sto ima pool viewHolder-a i rebind-uje ih sa datom.
* Prema tome, ukoliko imamo 1000 item-a, RecyclerView ce kreirati tipa 5 viewHolder-a koji su dovoljni da popune ekran i onda kada korisnik
* krene da skrola pri svakom dolazenju novog item-a recyclerView menja podatke u viewHolder sa podacima item-a koji je na redu*/
class AnimationAdapter(val context: Context) : RecyclerView.Adapter<AnimationAdapter.AnimationHolder>() {

    /*Ovo je referenca na recyclerView u nasem adapteru i koristi nam samo radi animacije pri setovanju data-e.
    * lateinit rec je kotlinova funkcionalnost koja daje mogucnost da ne definises promenljivu kao nullable i da je ne iniciras odmah pri deklaraciji
    * vec u nekom sledecem trenutku. Ukoliko se pozove neki metod nad referencom a pre toga nismo joj dodelili neku vrednost doci ce do exception-a*/
    lateinit var recyclerView: RecyclerView

    /*Ovo je data
    * Referenca ima custom set metod (u kotlinu se to set i get metodi generisu u compile time-u) zato sto pri setovanju data-e moramo da obavestimo
    * adapter da se set podataka promenio*/
    var animations: List<Animation> = emptyList()
    set(value) {
        field = value
        val animationLayout = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_item)
        recyclerView.layoutAnimation = animationLayout
        notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()
    }

    /*Sistemski metod Adapter-a koji nam omogucuje da iskoristimo recyclerView na koji je ovaj adapter prikacen*/
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    /*U ovom sistemskom metodu govorimo adapter-u koje sve tipove viewHolder-a treba da kreira. U nasoj aplikaciji mi imamo samo jednu vrstu pa tako
    * vracamo samo kreirani objekat naseg ViewHolder-a
    * U kostruktor viewHolder-a prosledjujemo view koji smo kreirali pomocu sistemskog layoutInflater-a kome opet prosledjujemo id layout-a koji smo kreirali preko xml file-a*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimationHolder {
        return AnimationHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_animation,
                parent,
                false
            )
        )
    }

    /*Metod gde govorimo adapteru koliko ima item-a*/
    override fun getItemCount(): Int {
        return animations.size
    }

    /*Ovde povezujemo view sa podacima
    * isto smo koristili Kotlin extension koji nam pravi u build time-u reference na view-ove preko njihovih id-eva u xml-u*/
    override fun onBindViewHolder(holder: AnimationHolder, position: Int) {
        val animation = animations[position]
        holder.itemView.text_animation_name.text = animation.name
        holder.itemView.text_animation_like.text = animation.like.toString()
        holder.itemView.text_animation_dislike.text = animation.dislike.toString()
        holder.itemView.animation_preview.imageAssetsFolder = "images/"
        holder.itemView.animation_preview.setAnimationFromJson(animation.lottieFile, null)
    }

    /*unutrasnja klasa koja predstavlja implementaciju naseg viewHolder-a*/
    inner class AnimationHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            /*Na ceo viewHolder setujemo click listener*/
            view.item.setOnClickListener {
                /*Kada korisnik klikne na animaciju, navigate metod se poziva i odvodi nas na sledeci ekran
                * findNavController je kotlinova funkcija koja trazi navController koji nas je doveo na ovaj ekran,
                * kada smo nasli controller pozivamo navigate metod i prosledjujemo izgenerisane pravce u build time-u jer sve pravce smo definisali
                * u xml file-u u res/navigation paketu*/
                it.findNavController().navigate(AnimationsFragmentDirections.actionAnimationsFragmentToCameraFragment(animations[adapterPosition]))
            }
        }
    }

}