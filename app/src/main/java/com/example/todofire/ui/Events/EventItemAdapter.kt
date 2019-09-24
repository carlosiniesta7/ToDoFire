package com.example.todofire.ui.Events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.todofire.R
import com.example.todofire.data.EventItem
import com.example.todofire.data.ItemRowListener

class EventItemAdapter(
    context: FragmentActivity?,
    eventItemList: MutableList<EventItem>?,
    rowListener: ItemRowListener
) : BaseAdapter() {

    private var eventList = eventItemList
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var rowList = rowListener

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val objectId: String? = eventList?.get(position)?.objectId
        val eventText: String? = eventList?.get(position)?.eventText
        val eventDate: String? = eventList?.get(position)?.eventDate
        val view: View
        val vh: ListRowHolder

        if (convertView == null) {
            view = mInflater.inflate(R.layout.row_events, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ListRowHolder
        }

        vh.label.text = eventText
        vh.date.text = eventDate
        vh.ibDeleteObject.setOnClickListener {
            if (objectId != null) {
                rowList.onItemDelete(objectId)
            }
        }

        return view
    }

    override fun getItem(index: Int): EventItem? {
        return eventList?.get(index)
    }
    override fun getItemId(index: Int): Long {
        return index.toLong()
    }
    override fun getCount(): Int {
        return eventList?.size!!
    }

    private class ListRowHolder(row: View?) {
        val label: TextView = row!!.findViewById(R.id.tv_event_text) as TextView
        val date: TextView = row!!.findViewById(R.id.tv_event_date) as TextView
        val ibDeleteObject: ImageButton = row!!.findViewById(R.id.iv_cross_event) as ImageButton
    }
}