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
import com.example.todofire.R
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
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

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val rcSignIn : Int = 1
    private lateinit var fireAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var itIn: MenuItem
    private lateinit var itOut: MenuItem


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initializeUi()
        initMenu()
    }

    override fun onStart() {
        super.onStart()
        initSignItemNav()
        checkUser()
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

    private fun initSignItemNav() {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val navMenu = navigationView.menu
        itIn = navMenu.findItem(R.id.nav_signIn)
        itOut = navMenu.findItem(R.id.nav_signOut)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        checkUser()
        return super.onPrepareOptionsMenu(menu)
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
            }
            R.id.nav_signOut -> {
                signOut()
            }
            R.id.nav_signIn -> {
                signIn()
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
        startActivityForResult(signInIntent, rcSignIn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == rcSignIn) {
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
                changeVisibility(true)
            } else {
                Toast.makeText(this, "Google sign in failed :(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        changeVisibility(false)
        Toast.makeText(this, "See u soon! ;)", Toast.LENGTH_LONG).show()
    }

    //--------------------------OTHERS FUNCTIONS--------------------------\\
    @SuppressLint("RestrictedApi")
    private fun changeVisibility(flag: Boolean) {
        if (flag) {
            sign_in_button.visibility = GONE
            itIn.isVisible = false
            sign_out_button.visibility = VISIBLE
            itOut.isVisible = true
        } else {
            sign_in_button.visibility = VISIBLE
            itIn.isVisible = true
            sign_out_button.visibility = GONE
            itOut.isVisible = false
        }
    }

    private fun checkUser() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            changeVisibility(true)
        }
        else {
            changeVisibility(false)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
