package de.smartsquare.kickchain.android.client.findmatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.smartsquare.kickchain.android.client.R
import de.smartsquare.kickchain.android.client.findmatch.PlayerAdapter.ViewHolder
import kotterknife.bindView

/**
 * @author Ruben Gees
 */
class PlayerAdapter : RecyclerView.Adapter<ViewHolder>() {

    var playerSelectedCallback: ((Player) -> Unit)? = null

    private var data = emptyList<Player>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    fun replaceData(newData: List<Player>) {
        DiffUtil
            .calculateDiff(diffCallback(newData))
            .dispatchUpdatesTo(this)

        data = newData.toList()
    }

    private fun diffCallback(newData: List<Player>) = object : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return data[oldItemPosition].name == newData[newItemPosition].name
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return data[oldItemPosition] == newData[newItemPosition]
        }

        override fun getOldListSize() = data.size
        override fun getNewListSize() = newData.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val selectedCheckBox by bindView<CheckBox>(R.id.selectedCheckBox)
        private val username by bindView<TextView>(R.id.username)
        private val status by bindView<TextView>(R.id.status)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    playerSelectedCallback?.invoke(data[adapterPosition])
                }
            }
        }

        fun bind(item: Player) {
            selectedCheckBox.isChecked = item.isTeamMate || item.isOpponent

            username.text = item.name
            status.text = status.context.getString(R.string.find_match_player_no_games)
        }
    }
}
