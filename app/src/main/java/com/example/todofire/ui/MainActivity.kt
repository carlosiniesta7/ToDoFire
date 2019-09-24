package com.example.todofire.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import com.example.todofire.R
import com.example.todofire.ui.ToDo.ToDoFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val RC_SIGN_IN : Int = 1
    private lateinit var fireAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initializeUi()
        initMenu()
    }

    //---------------------INIT FUNCTIONS---------------------------\\
    private fun initializeUi() {
        nav_view.setNavigationItemSelectedListener(this)
        fireAuth = FirebaseAuth.getInstance()

        configureGoogleSignIn()
        sign_in_button.setOnClickListener {
            signIn()
        }
        sign_out_button.setOnClickListener {
            signOut()
        }
    }

    private fun initMenu() {
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        (drawer_layout)?.addDrawerListener(toggle)
        toggle.syncState()
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
         when (item.itemId) {
            R.id.action_settings ->
                startActivity(Intent(this, SettingsActivity::class.java))
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0.itemId) {
            R.id.nav_todoList -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.listFragment)
            }
            R.id.nav_eventList -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.eventFragment)
                //val action = MainFragmentDirections.actionlistFragmentToeventFragment
            }
            R.id.nav_signOut -> {
                signOut()
            }
            R.id.nav_about -> {
                Toast.makeText(this, "Navegar Activity About", Toast.LENGTH_SHORT).show()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
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
                    fireAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fireAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        fireAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
               // startActivity(HomeActivity.getLaunchIntent(this))
                changeVisibility(true)
                Toast.makeText(this, "Google sign in succesfull! :D" + it.result, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Google sign in failed :(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signOut() {
        //startActivity(SignInActivity.getLaunchIntent(this))
        changeVisibility(false)
        FirebaseAuth.getInstance().signOut()
        //Toast.makeText(this, "Sign Out correct. Bye! ;)", Toast.LENGTH_LONG).show()
    }

    //--------------------------OTHERS FUNCTIONS--------------------------\\
    @SuppressLint("RestrictedApi")
    private fun changeVisibility(flag: Boolean) {
        if (flag) {
            sign_in_button.visibility = GONE
            sign_out_button.visibility = VISIBLE
        } else {
            sign_in_button.visibility = VISIBLE
            sign_out_button.visibility = GONE
        }
    }
}
