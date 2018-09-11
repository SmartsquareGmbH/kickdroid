package de.smartsquare.kickdroid.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.jakewharton.rxbinding2.view.clicks
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.R
import de.smartsquare.kickdroid.base.BaseFragment
import kotterknife.bindView

/**
 * @author Ruben Gees
 */
class IdleFragment : BaseFragment() {

    companion object {
        fun newInstance() = IdleFragment()
    }

    private val createGame by bindView<Button>(R.id.createGame)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_idle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createGame.clicks()
            .autoDisposable(this.scope())
            .subscribe {
                // TODO
            }
    }
}
