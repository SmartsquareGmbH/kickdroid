package de.smartsquare.kickdroid.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.smartsquare.kickdroid.R
import de.smartsquare.kickdroid.base.BaseFragment
import org.koin.android.viewmodel.ext.android.sharedViewModel

/**
 * @author Ruben Gees
 */
class SearchingFragment : BaseFragment() {

    companion object {
        fun newInstance() = SearchingFragment()
    }

    private val viewModel by sharedViewModel<GameViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.discover()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_searching, container, false)
    }
}
