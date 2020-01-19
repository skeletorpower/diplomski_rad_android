package com.aleksandar.novic.dialog

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import com.aleksandar.novic.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_triggers.*
import kotlinx.android.synthetic.main.dialog_triggers.view.*

class TriggersDialog : BottomSheetDialogFragment(), TriggerListener {

    lateinit var sharedPreferences: SharedPreferences

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("Animationary", Context.MODE_PRIVATE)
        text_like.text = sharedPreferences.getString("like", "")
        text_dislike.text = sharedPreferences.getString("dislike", "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_triggers, container, false)
        view.image_edit_like.setOnClickListener {
            val listDialog = TriggerListDialog()
            listDialog.likeOrDislike = 1
            listDialog.triggerListener = this
            listDialog.show(requireFragmentManager(), "Like")
        }
        view.image_edit_dislike.setOnClickListener {
            val listDialog = TriggerListDialog()
            listDialog.likeOrDislike = 2
            listDialog.triggerListener = this
            listDialog.show(requireFragmentManager(), "DisLike")
        }
        return view
    }

    override fun setTrigger(likeOrDislike: Int, trigger: String) {
        when(likeOrDislike){
            1 -> {
                sharedPreferences.edit {
                    putString("like", trigger)
                }
                text_like.text = trigger
            }
            2 -> {
                sharedPreferences.edit {
                    putString("dislike", trigger)
                }
                text_dislike.text = trigger
            }
        }
    }
}