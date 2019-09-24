package com.example.todofire.ui.ToDo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.todofire.R
import com.example.todofire.data.ItemRowListener
import com.example.todofire.data.ToDoItem
import com.example.todofire.utilities.Constants
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_to_do.view.*

class ToDoFragment : Fragment(), ItemRowListener {

    private lateinit var mDatabase: DatabaseReference
    var toDoItemList: MutableList<ToDoItem>? = null
    private lateinit var adapter: ToDoItemAdapter
    private var rowListener = this as ItemRowListener


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_to_do, container, false)

        view.add_button.setOnClickListener {
            addNewItemDialog(view.context)
        }

        toDoItemList = mutableListOf()
        mDatabase = FirebaseDatabase.getInstance().reference

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ToDoItemAdapter(activity, toDoItemList, rowListener)
        items_list?.adapter = adapter
        mDatabase.child(Constants.FIREBASE_TODO).addValueEventListener(itemListener)
    }

    private fun addNewItemDialog(context: Context) {
        val alert = AlertDialog.Builder(context)
        val itemEditText = EditText(context)
        alert.setMessage("Añadir nueva tarea")
        alert.setTitle("Enter To Do Item Text")
        alert.setView(itemEditText)
        alert.setPositiveButton("Añadir") { dialog, _ ->
            val todoItem = ToDoItem.create()
            todoItem.itemText = itemEditText.text.toString()
            todoItem.done = false
            //We first make a push so that a new item is made with a unique ID
            val newItem = mDatabase.child(Constants.FIREBASE_TODO).push()
            todoItem.objectId = newItem.key
            //then, we used the reference to set the value on that ID
            newItem.setValue(todoItem)
            dialog.dismiss()
            Toast.makeText(context , "TAREA AÑADIDA: " + todoItem.itemText, Toast.LENGTH_SHORT).show()
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
                val todoItem = ToDoItem.create()
                //get current data in a map
                val map = currentItem.value as HashMap<*,*>
                //key will return Firebase ID
                todoItem.objectId = currentItem.key
                todoItem.done = map["done"] as Boolean?
                todoItem.itemText = map["itemText"] as String?
                toDoItemList?.add(todoItem)
            }
        adapter.notifyDataSetChanged()
    }

    private var itemListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            toDoItemList?.clear()
            //val toDoItem =  dataSnapshot.getValue(ToDoItem::class.java)
            addDataToList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    override fun modifyItemState(itemObjectId: String, isDone: Boolean) {
        val itemReference = mDatabase.child(Constants.FIREBASE_TODO).child(itemObjectId)
        itemReference.child("done").setValue(isDone)
    }

    override fun onItemDelete(itemObjectId: String) {
        val itemReference = mDatabase.child(Constants.FIREBASE_TODO).child(itemObjectId)
        itemReference.removeValue()
    }

}
