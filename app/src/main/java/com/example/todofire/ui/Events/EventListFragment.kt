package com.example.todofire.ui.Events

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.todofire.R
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.todofire.data.EventItem
import com.example.todofire.data.ItemRowListener
import com.example.todofire.utilities.Constants
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event.view.*
import java.util.*


class EventListFragment : Fragment(), ItemRowListener {

    private lateinit var mDatabase: DatabaseReference
    var eventItemList: MutableList<EventItem>? = null
    var eventEx = EventItem("17", "ejemplo", "17/07/2017")
    private lateinit var adapter: EventItemAdapter
    private var rowListener = this as ItemRowListener


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_event, container, false)

        view.add_button.setOnClickListener {
            addNewEventDialog(view.context)
        }

        eventItemList = mutableListOf(eventEx)
        mDatabase = FirebaseDatabase.getInstance().reference

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventItemAdapter(activity, eventItemList, rowListener)
        events_list?.adapter = adapter
        mDatabase.child(Constants.FIREBASE_EVENT).addValueEventListener(itemListener)
    }

    private fun addNewEventDialog(context: Context) {
        val alert = AlertDialog.Builder(context)
        val itemEditText = EditText(context)
        alert.setTitle("Añadir Nuevo Evento")
        alert.setMessage("Enter Event Title")
        alert.setView(itemEditText)
        alert.setPositiveButton("Añadir") { dialog, _ ->
            val eventItem = EventItem.create()
            eventItem.eventText = itemEditText.text.toString()
            val d = Date()
            val calendarNow = DateFormat.format("d/M/yyyy", d.time)
            eventItem.eventDate = calendarNow.toString()
            //We first make a push so that a new item is made with a unique ID
            val newItem = mDatabase.child(Constants.FIREBASE_EVENT).push()
            eventItem.objectId = newItem.key
            //then, we used the reference to set the value on that ID
            newItem.setValue(eventItem)
            dialog.dismiss()
            Toast.makeText(context , "EVENTO CREADO: " + eventItem.eventText, Toast.LENGTH_SHORT).show()
        }
        alert.show()
    }

    //-----------------------DATABASE FUNCTIONS-----------------------------\\
    private fun addDataToList(dataSnapshot: DataSnapshot) {
            val itemsIterator = dataSnapshot.children.iterator()
            //check if the collection has any to do items or not
            while (itemsIterator.hasNext()) {
                //get current item
                val currentItem = itemsIterator.next()
                val eventIt = EventItem.create()
                //get current data in a map
                val map = currentItem.value as HashMap<*,*>
                //key will return Firebase ID
                eventIt.objectId = currentItem.key
                eventIt.eventDate = map["eventDate"] as String?
                eventIt.eventText = map["eventText"] as String?
                eventItemList?.add(eventIt)
            }
        adapter.notifyDataSetChanged()
    }

    private var itemListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            eventItemList?.clear()
            //val toDoItem =  dataSnapshot.getValue(ToDoItem::class.java)
            addDataToList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    override fun modifyItemState(itemObjectId: String, isDone: Boolean) {
        val itemReference = mDatabase.child(Constants.FIREBASE_EVENT).child(itemObjectId)
        itemReference.child("done").setValue(isDone)
    }

    override fun onItemDelete(itemObjectId: String) {
        val itemReference = mDatabase.child(Constants.FIREBASE_EVENT).child(itemObjectId)
        itemReference.removeValue()
    }
}