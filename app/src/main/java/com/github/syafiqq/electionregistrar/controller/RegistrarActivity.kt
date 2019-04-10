package com.github.syafiqq.electionregistrar.controller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.syafiqq.electionregistrar.R
import kotlinx.android.synthetic.main.activity_registrar.*

class RegistrarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)
        setSupportActionBar(toolbar)
    }

}
