package de.smartsquare.kickdroid.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import de.smartsquare.kickdroid.R
import kotterknife.bindView
import org.koin.android.viewmodel.ext.android.sharedViewModel

/**
 * @author Ruben Gees
 */
class PlayingFragment : Fragment() {

    companion object {
        fun newInstance() = PlayingFragment()
    }

    private val viewModel by sharedViewModel<GameViewModel>()

    private val scoreLeft by bindView<TextView>(R.id.scoreLeft)
    private val scoreRight by bindView<TextView>(R.id.scoreRight)
    private val leftTeam by bindView<TextView>(R.id.teamLeft)
    private val rightTeam by bindView<TextView>(R.id.teamRight)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.lobby.observe(this, Observer {
            if (it != null) {
                scoreLeft.text = it.scoreLeftTeam.toString()
                scoreRight.text = it.scoreRightTeam.toString()
                leftTeam.text = it.leftTeam.joinToString(separator = "\n")
                rightTeam.text = it.rightTeam.joinToString(separator = "\n")
            }
        })
    }
}
