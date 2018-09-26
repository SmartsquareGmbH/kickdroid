package de.smartsquare.kickdroid.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gojuno.koptional.Some
import com.gojuno.koptional.toOptional
import com.jakewharton.rxbinding2.view.clicks
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.R
import de.smartsquare.kickdroid.base.BaseFragment
import de.smartsquare.kickdroid.base.DefaultErrorHandler
import de.smartsquare.kickdroid.base.getSimpleQuantityString
import de.smartsquare.kickdroid.view.LoadingIndicator
import io.reactivex.Observable
import kotterknife.bindView
import org.koin.android.ext.android.inject

/**
 * @author Ruben Gees
 */
class StatisticsFragment : BaseFragment() {

    companion object {
        private const val TYPE_ARGUMENT = "type"

        fun newInstance(type: StatisticsType): StatisticsFragment {
            return StatisticsFragment().apply {
                arguments = Bundle().also {
                    it.putSerializable(TYPE_ARGUMENT, type)
                }
            }
        }
    }

    private val type get() = requireArguments().getSerializable(TYPE_ARGUMENT) as StatisticsType

    private val viewModel by inject<StatisticsViewModel>()
    private val listAdapter = PlayerAdapter()

    private val content by bindView<ViewGroup>(R.id.content)
    private val empty by bindView<View>(R.id.empty)
    private val error by bindView<View>(R.id.error)
    private val loading by bindView<LoadingIndicator>(R.id.loading)

    private val first by bindView<ViewGroup>(R.id.first)
    private val firstName by bindView<TextView>(R.id.firstName)
    private val firstWins by bindView<TextView>(R.id.firstWins)
    private val firstGoals by bindView<TextView>(R.id.firstGoals)

    private val second by bindView<ViewGroup>(R.id.second)
    private val secondName by bindView<TextView>(R.id.secondName)
    private val secondWins by bindView<TextView>(R.id.secondWins)
    private val secondGoals by bindView<TextView>(R.id.secondGoals)

    private val third by bindView<ViewGroup>(R.id.third)
    private val thirdName by bindView<TextView>(R.id.thirdName)
    private val thirdWins by bindView<TextView>(R.id.thirdWins)
    private val thirdGoals by bindView<TextView>(R.id.thirdGoals)

    private val list by bindView<RecyclerView>(R.id.list)

    private val errorText by bindView<TextView>(R.id.errorText)
    private val errorButton by bindView<Button>(R.id.errorButton)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listAdapter.clickSubject
            .autoDisposable(this.scope())
            .subscribe {
                // TODO
            }

        viewModel.loadStatistics(type)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.isNestedScrollingEnabled = false
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = listAdapter

        errorButton.clicks()
            .autoDisposable(viewLifecycleOwner.scope())
            .subscribe { viewModel.loadStatistics(type) }

        Observable
            .merge(
                first.clicks().map { 0 },
                second.clicks().map { 1 },
                third.clicks().map { 2 }
            )
            .map { viewModel.statisticsSuccess.value?.getOrNull(it).toOptional() }
            .filter { it is Some }
            .map { (it as Some).value }
            .autoDisposable(viewLifecycleOwner.scope())
            .subscribe {
                // TODO
            }

        viewModel.statisticsSuccess.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.isEmpty()) {
                    content.visibility = View.GONE
                    empty.visibility = View.VISIBLE
                } else {
                    content.visibility = View.VISIBLE
                    empty.visibility = View.GONE
                }

                error.visibility = View.GONE

                bindTopThreePlayer(it.getOrNull(0), second, secondName, secondWins, secondGoals)
                bindTopThreePlayer(it.getOrNull(1), first, firstName, firstWins, firstGoals)
                bindTopThreePlayer(it.getOrNull(2), third, thirdName, thirdWins, thirdGoals)

                listAdapter.items = it.drop(3)
            }
        })

        viewModel.statisticsError.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                content.visibility = View.GONE
                empty.visibility = View.GONE
                error.visibility = View.VISIBLE

                errorText.text = getString(DefaultErrorHandler.handle(it))
            }
        })

        viewModel.statisticsLoading.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                loading.visibility = View.VISIBLE
                content.visibility = View.GONE
                empty.visibility = View.GONE
                error.visibility = View.GONE
            } else {
                loading.visibility = View.GONE
            }
        })
    }

    private fun bindTopThreePlayer(
        player: Player?,
        container: ViewGroup,
        nameView: TextView,
        winsView: TextView,
        goalsView: TextView
    ) {
        if (player == null) {
            container.visibility = View.INVISIBLE
        } else {
            container.visibility = View.VISIBLE
            nameView.text = player.name
            winsView.text = resources.getSimpleQuantityString(R.plurals.statistics_wins, player.totalWins)
            goalsView.text = resources.getSimpleQuantityString(R.plurals.statistics_goals, player.totalGoals)
        }
    }
}
