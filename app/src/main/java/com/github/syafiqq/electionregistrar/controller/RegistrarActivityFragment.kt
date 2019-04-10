package com.github.syafiqq.electionregistrar.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.syafiqq.electionregistrar.R
import timber.log.Timber

/**
 * A placeholder fragment containing a simple view.
 */
class RegistrarActivityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView [inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?]")
        return inflater.inflate(R.layout.fragment_registrar, container, false)
    }
}
