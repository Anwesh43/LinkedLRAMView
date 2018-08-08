package com.anwesh.uiprojects.linkedrightanglemoverview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.lramview.LRAMView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LRAMView.create(this)
    }
}
