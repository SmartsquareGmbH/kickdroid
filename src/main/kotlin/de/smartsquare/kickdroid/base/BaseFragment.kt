package de.smartsquare.kickdroid.base

import androidx.fragment.app.Fragment
import kotterknife.KotterKnife

/**
 * @author Ruben Gees
 */
abstract class BaseFragment : Fragment() {

    override fun onDestroyView() {
        KotterKnife.reset(this)

        super.onDestroyView()
    }

    fun requireArguments() = arguments ?: throw IllegalStateException("arguments are null")
}
