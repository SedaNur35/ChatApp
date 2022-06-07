package com.seda.chatapp.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging
import com.seda.chatapp.R
import com.seda.chatapp.adapter.UserAdapter
import com.seda.chatapp.databinding.ActivityUserBinding
import com.seda.chatapp.firebase.firebaseMessagingService
import com.seda.chatapp.model.User

class UserActivity : AppCompatActivity() {
    private lateinit var binding :ActivityUserBinding
    var userList =ArrayList<User>()
    private lateinit var adapter: UserAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseMessagingService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            firebaseMessagingService.token = it
        }

        binding.userRecycler.layoutManager =LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
getUserList()
        adapter =UserAdapter(this@UserActivity,userList)
        binding.userRecycler.adapter =adapter
        binding.userRecycler.setHasFixedSize(true)

binding.imgback.setOnClickListener{
    val intent = Intent(this, LoginActivity::class.java)
    startActivity(intent)
}
        binding.imgprofile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun getUserList(){
        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        var userid = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")

        var databaseReference:DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for(c in snapshot.children){
                val user = c.getValue(User::class.java)
                    if(user != null){
                        user.userId = c.key.toString()
                        userList.add(user)


                    }
                    if (user!!.profileImage == ""){
                        binding.imgprofile.setImageResource(R.drawable.acount)
                    }
                    else{
                        Glide.with(this@UserActivity).load(user!!.profileImage)
                            .placeholder(R.drawable.acount).into(binding.imgprofile)
                    }
                }

               adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
Toast.makeText(applicationContext,"error",Toast.LENGTH_SHORT).show()

            }

        })
    }
}