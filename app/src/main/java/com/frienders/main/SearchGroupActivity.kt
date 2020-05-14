package com.frienders.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.frienders.main.Search.GroupSearchFragment

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
