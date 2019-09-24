package com.example.todofire.ui.ToDo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.todofire.R
import com.example.todofire.data.ItemRowListener
import com.example.todofire.data.ToDoItem

class ToDoItemAdapter(
    context: FragmentActivity?,
    toDoItemList: MutableList<ToDoItem>?,
    rowListener: ItemRowListener
) : BaseAdapter() {

    private var itemList = toDoItemList
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var rowList = rowListener

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val objectId: String = itemList?.get(position)?.objectId as String
        val itemText: String = itemList?.get(position)?.itemText as String
        val done: Boolean = itemList?.get(position)?.done as Boolean
        val view: View
        val vh: ListRowHolder

        if (convertView == null) {
            view = mInflater.inflate(R.layout.row_items, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ListRowHolder
        }

        vh.label.text = itemText
        vh.isDone.isChecked = done
        vh.isDone.setOnClickListener {
            rowList.modifyItemState(objectId, !done) }
        vh.ibDeleteObject.setOnClickListener {
            rowList.onItemDelete(objectId) }

        return view
    }

    override fun getItem(index: Int): ToDoItem? {
        return itemList?.get(index)
    }
    override fun getItemId(index: Int): Long {
        return index.toLong()
    }
    override fun getCount(): Int {
        return itemList?.size!!
    }

    private class ListRowHolder(row: View?) {
        val label: TextView = row!!.findViewById(R.id.tv_item_text) as TextView
        val isDone: CheckBox = row!!.findViewById(R.id.cb_item_is_done) as CheckBox
        val ibDeleteObject: ImageButton = row!!.findViewById(R.id.iv_cross) as ImageButton
    }
}