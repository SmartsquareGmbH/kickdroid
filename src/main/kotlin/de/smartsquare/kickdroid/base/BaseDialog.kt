package de.smartsquare.kickdroid.base

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotterknife.KotterKnife

/**
 * @author Ruben Gees
 */
@Suppress("UnnecessaryAbstractClass")
abstract class BaseDialog : DialogFragment() {

    val alertDialog get() = dialog as? AlertDialog ?: throw IllegalStateException("dialog is not initialized yet")

    override fun onStop() {
        KotterKnife.reset(this)

        super.onStop()
    }
}
