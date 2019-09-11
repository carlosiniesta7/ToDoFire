package com.example.todofire

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), ItemRowListener {
    private lateinit var mDatabase: DatabaseReference
    var toDoItemList: MutableList<ToDoItem>? = null
    lateinit var adapter: ToDoItemAdapter
    private var listViewItems: ListView? = null
    private val RC_SIGN_IN : Int = 1
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mDatabase = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        mDatabase.addValueEventListener(itemListener)

        configureGoogleSignIn()

        add_button.setOnClickListener {
            addNewItemDialog()
        }

        sign_in_button.setOnClickListener{
            signIn()
        }

        sign_out_button.setOnClickListener{
            signOut()
        }

        toDoItemList = mutableListOf()
        adapter = ToDoItemAdapter(this, toDoItemList)
        listViewItems = items_list
        listViewItems?.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if(user!=null){
            changeVisibility(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    //---------------------AUTHENTICATION FUNCTIONS---------------------------\\
    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
               // startActivity(HomeActivity.getLaunchIntent(this))
                changeVisibility(true)
                Toast.makeText(this, "Google sign in succesfull! :D", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Google sign in failed :(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signOut() {
        //startActivity(SignInActivity.getLaunchIntent(this))
        changeVisibility(false)
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Sign Out correct. Bye! ;)", Toast.LENGTH_LONG).show()
    }


    //-----------------------DATABASE FUNCTIONS-----------------------------\\
    private fun addNewItemDialog() {
        val alert = AlertDialog.Builder(this)
        val itemEditText = EditText(this)
        alert.setMessage("Añadir nueva tarea")
        alert.setTitle("Enter To Do Item Text")
        alert.setView(itemEditText)
        alert.setPositiveButton("Añadir") { dialog, _ ->
            val todoItem = ToDoItem.create()
            todoItem.itemText = itemEditText.text.toString()
            todoItem.done = false
            //We first make a push so that a new item is made with a unique ID
            val newItem = mDatabase.child(Constants.FIREBASE_ITEM).push()
            todoItem.objectId = newItem.key
            //then, we used the reference to set the value on that ID
            newItem.setValue(todoItem)
            dialog.dismiss()
            Toast.makeText(this, "TAREA AÑADIDA: " + todoItem.itemText, Toast.LENGTH_SHORT).show()
        }
        alert.show()
    }

    private fun addDataToList(dataSnapshot: DataSnapshot) {
        val items = dataSnapshot.children.iterator()
        //Check if current database contains any collection
        if (items.hasNext()) {
            val toDoListIndex = items.next()
            val itemsIterator = toDoListIndex.children.iterator()
            //check if the collection has any to do items or not
            while (itemsIterator.hasNext()) {
                //get current item
                val currentItem = itemsIterator.next()
                val todoItem = ToDoItem.create()
                //get current data in a map
                val map = currentItem.value as HashMap<*, *>
                //key will return Firebase ID
                todoItem.objectId = currentItem.key
                todoItem.done = map["done"] as Boolean?
                todoItem.itemText = map["itemText"] as String?
                toDoItemList?.add(todoItem)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private var itemListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            toDoItemList?.clear()
            addDataToList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    override fun modifyItemState(itemObjectId: String, isDone: Boolean) {
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        itemReference.child("done").setValue(isDone)
    }

    override fun onItemDelete(itemObjectId: String) {
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        itemReference.removeValue()
    }



    //--------------------------OTHERS FUNCTIONS--------------------------\\
    @SuppressLint("RestrictedApi")
    private fun changeVisibility(flag: Boolean) {
        if (flag) {
            sign_in_button.visibility = GONE
            sign_out_button.visibility = VISIBLE
            add_button.visibility = VISIBLE
        } else {
            sign_in_button.visibility = VISIBLE
            sign_out_button.visibility = GONE
            add_button.visibility = GONE
        }
    }

}
