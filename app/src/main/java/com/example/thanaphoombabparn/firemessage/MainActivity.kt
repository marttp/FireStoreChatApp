package com.example.thanaphoombabparn.firemessage

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.example.thanaphoombabparn.firemessage.fragment.MyAccountFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

//    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
//        when (item.itemId) {
//            R.id.navigation_home -> {
//                message.setText(R.string.title_home)
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_dashboard -> {
//                message.setText(R.string.title_dashboard)
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_notifications -> {
//                message.setText(R.string.title_notifications)
//                return@OnNavigationItemSelectedListener true
//            }
//        }
//        false
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
            navigation.setOnNavigationItemSelectedListener {
                when(it.itemId){
                    R.id.navigation_people -> {
                        //TODO: show people fragment
                        true
                    }
                    R.id.navigation_my_account -> {
                        //TODO: show my account fragment
                        replaceFragment(MyAccountFragment())
                        true
                    }
                    else -> false
                }
            }
    }

    @SuppressLint("CommitTransaction")
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_layout, fragment)
            commit()
        }
    }
}
