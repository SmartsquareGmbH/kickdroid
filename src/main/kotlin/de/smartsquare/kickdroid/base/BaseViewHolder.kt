package de.smartsquare.kickdroid.base

import android.view.View
import androidx.recyclerview.widget.AutoDisposeViewHolder
import kotterknife.KotterKnife

/**
 * @author Ruben Gees
 */
abstract class BaseViewHolder(itemView: View) : AutoDisposeViewHolder(itemView) {

    override fun onUnbind() {
        KotterKnife.reset(this)

        super.onUnbind()
    }
}
