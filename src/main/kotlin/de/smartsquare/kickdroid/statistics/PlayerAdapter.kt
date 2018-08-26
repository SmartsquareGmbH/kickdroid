package de.smartsquare.kickdroid.statistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.uber.autodispose.autoDisposable
import de.smartsquare.kickdroid.R
import de.smartsquare.kickdroid.base.BaseViewHolder
import de.smartsquare.kickdroid.base.getSimpleQuantityString
import de.smartsquare.kickdroid.statistics.PlayerAdapter.ViewHolder
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView

/**
 * @author Ruben Gees
 */
class PlayerAdapter : RecyclerView.Adapter<ViewHolder>() {

    val clickSubject = PublishSubject.create<Player>()

    var items = emptyList<Player>()
        set(value) {
            field = value.toList()

            notifyDataSetChanged()
        }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_statistics, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return items[position].name.hashCode().toLong()
    }

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        private val ranking by bindView<TextView>(R.id.ranking)
        private val name by bindView<TextView>(R.id.name)
        private val info by bindView<TextView>(R.id.info)

        fun bind(player: Player, position: Int) {
            itemView.clicks()
                .map { adapterPosition }
                .filter { it != RecyclerView.NO_POSITION }
                .map { items[it] }
                .autoDisposable(this)
                .subscribe(clickSubject)

            val infoText = info.resources.getSimpleQuantityString(R.plurals.statistics_wins, player.totalWins) + " | " +
                info.resources.getSimpleQuantityString(R.plurals.statistics_goals, player.totalGoals)

            ranking.text = (position + 4).toString()
            name.text = player.name
            info.text = infoText
        }
    }
}
