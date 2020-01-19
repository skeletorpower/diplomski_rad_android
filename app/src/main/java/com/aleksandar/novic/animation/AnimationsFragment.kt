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

class AnimationsFragment : Fragment(R.layout.fragment_animations), Observer<List<Animation>> {

    private val viewModel by viewModels<AnimationsViewModel> { ViewModelProvider.NewInstanceFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        list.adapter = AnimationAdapter(requireContext())
        button_setting.setOnClickListener {
            val dialog = TriggersDialog()
            dialog.show(requireFragmentManager(), "Triggers")
        }
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        viewModel.animationsLiveData.observe(this, this)
        viewModel.animationsLiveData.value?.let {
            loader.visibility = View.GONE
            (list.adapter as AnimationAdapter).animations = it
        }
        (requireActivity() as MainActivity).app_bar_layout.visibility = View.VISIBLE
        viewModel.refresh()
    }


    override fun onChanged(t: List<Animation>?) {
        t?.let {
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

class AnimationAdapter(val context: Context) : RecyclerView.Adapter<AnimationAdapter.AnimationHolder>() {

    lateinit var recyclerView: RecyclerView

    var animations: List<Animation> = emptyList()
    set(value) {
        field = value
        val animationLayout = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_item)
        recyclerView.layoutAnimation = animationLayout
        notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimationHolder {
        return AnimationHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_animation,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return animations.size
    }

    override fun onBindViewHolder(holder: AnimationHolder, position: Int) {
        val animation = animations[position]
        holder.itemView.text_animation_name.text = animation.name
        holder.itemView.text_animation_like.text = animation.like.toString()
        holder.itemView.text_animation_dislike.text = animation.dislike.toString()
        holder.itemView.animation_preview.imageAssetsFolder = "images/"
        holder.itemView.animation_preview.setAnimationFromJson(animation.lottieFile, null)
    }

    inner class AnimationHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.item.setOnClickListener {
                it.findNavController().navigate(AnimationsFragmentDirections.actionAnimationsFragmentToCameraFragment(animations[adapterPosition]))
            }
        }
    }

}