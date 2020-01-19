package com.aleksandar.novic.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aleksandar.novic.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_trigger_list.view.*
import kotlinx.android.synthetic.main.item_trigger.view.*

class TriggerListDialog : DialogFragment() {

    lateinit var list: List<String>
    lateinit var triggerListener: TriggerListener

    var likeOrDislike: Int = -1

    lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = requireContext().assets.open("labels.txt").bufferedReader().lineSequence().toList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_trigger_list, null, false)

        view.list_trigger.layoutManager = LinearLayoutManager(requireContext())
        adapter = Adapter(this).apply {
            triggerListener = this@TriggerListDialog.triggerListener
            likeOrDislike = this@TriggerListDialog.likeOrDislike
            data = list
        }
        view.list_trigger.adapter = adapter

        view.edit_search.addTextChangedListener {
            if (it?.isNotBlank() == true){
                adapter.data = list.filter { string -> string.startsWith(it, true) }
            }else{
                adapter.data = list
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setBackgroundInsetBottom(0)
            .setBackgroundInsetTop(0)
            .setBackgroundInsetStart(0)
            .setBackgroundInsetEnd(0)
            .setView(view)
            .create()
    }
}

class Adapter(val fragment: TriggerListDialog): RecyclerView.Adapter<Adapter.Holder>() {

    lateinit var triggerListener: TriggerListener
    var likeOrDislike: Int = -1

    var data: List<String> = emptyList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_trigger, parent, false))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemView.item_trigger.text = data[position]
    }

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {
        init {
            itemView.item.setOnClickListener {
                triggerListener.setTrigger(likeOrDislike, data[layoutPosition])
                fragment.dismiss()
            }
        }
    }
}

interface TriggerListener {
    fun setTrigger(likeOrDislike: Int, trigger: String)
}