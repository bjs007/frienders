package com.frienders.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.frienders.main.Search.GroupSearchFragment
import com.frienders.main.config.UsersFirebaseFields
import com.frienders.main.db.refs.FirebaseAuthProvider
import com.frienders.main.db.refs.FirebasePaths
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.serialization.UnionKind

class SearchGroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_group)
        showProductFragment()

    }

    fun showProductFragment() {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.container, GroupSearchFragment())
                .commit()
    }
    }
