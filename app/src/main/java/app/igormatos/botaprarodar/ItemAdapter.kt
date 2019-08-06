package app.igormatos.botaprarodar

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import app.igormatos.botaprarodar.local.model.Bicycle
import app.igormatos.botaprarodar.local.model.Item
import app.igormatos.botaprarodar.local.model.User
import app.igormatos.botaprarodar.local.model.Withdraw
import app.igormatos.botaprarodar.network.FirebaseHelper
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_cell.view.*
import org.parceler.Parcels

class ItemAdapter(private var activity: Activity? = null) :
    androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>(),
    Filterable {

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchWord = constraint.toString()

                filteredList = if (searchWord.isEmpty()) {
                    itemsList
                } else {
                    filteredList.filter { item ->
                        when (item) {
                            is User -> {
                                item.name!!.contains(searchWord, true) ||
                                        item.doc_number.toString().contains(searchWord, true)
                            }
                            else -> {
                                return@filter true
                            }
                        }
                    }.toMutableList()
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults

            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results!!.values as MutableList<Item>

                notifyDataSetChanged()
            }
        }

    }

    var itemsList: MutableList<Item> = mutableListOf()
    var filteredList: MutableList<Item> = mutableListOf()
    var withdrawalInProgress: Withdraw? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cell, parent, false)
        return ItemCellViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filteredList.count()
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, index: Int) {
        val item = filteredList[index]

        if (withdrawalInProgress == null) {
            (holder as ItemCellViewHolder).bind(item, activity)
        } else {
            (holder as ItemCellViewHolder).bindUserSelection(item, withdrawalInProgress!!, activity!!)
        }

    }

    fun addItem(item: Item) {
        itemsList.add(0, item)
        filteredList.add(0, item)
        notifyDataSetChanged()
    }

    fun removeItem(item: Item) {
        val index = itemsList.indexOfFirst { it.id == item.id }
        itemsList.removeAt(index)

        val filteredListIndex = filteredList.indexOfFirst { it.id == item.id }
        filteredList.removeAt(filteredListIndex)

        notifyItemRemoved(index)
    }

    fun updateItem(item: Item) {
        val index = itemsList.indexOfFirst { it.id == item.id }
        itemsList.removeAt(index)
        itemsList.add(0, item)

        val filteredListIndex = filteredList.indexOfFirst { it.id == item.id }
        filteredList.removeAt(filteredListIndex)
        filteredList.add(0, item)

        notifyDataSetChanged()
    }

    class ItemCellViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        fun bindUserSelection(item: Item, withdrawalInProgress: Withdraw, activity: Activity) {
            itemView.findViewById<TextView>(R.id.cellTitle).text = item.title()
            itemView.findViewById<TextView>(R.id.cellSubtitle).text = item.subtitle()

            val imageView = itemView.findViewById<ImageView>(R.id.cellAvatar)
            Glide.with(itemView.context)
                .load(item.iconPath())
                .into(imageView)

            itemView.setOnClickListener {
                val context = it.context
                val user = item as User
                withdrawalInProgress.user_id = user.id
                withdrawalInProgress.user = user
                withdrawalInProgress.user_name = user.name
                withdrawalInProgress.user_image_path = user.iconPath()

                val alertDialog = AlertDialog.Builder(itemView.context)
                alertDialog.setTitle(context.getString(R.string.withdraw_confirm_title))
                alertDialog.setMessage("Bicicleta: ${withdrawalInProgress.bicycle_name} \nUsuário: ${withdrawalInProgress.user_name}")
                alertDialog.setPositiveButton(context.getString(R.string.withdraw_confirm)) { _, _ ->

                    FirebaseHelper.updateBicycleStatus(withdrawalInProgress.bicycle_id!!, false) {
                        if (!it) {
                            return@updateBicycleStatus
                        }

                        withdrawalInProgress.saveRemote {
                            activity.setResult(Activity.RESULT_OK)
                            activity.finish()
                        }
                    }

                }.show()
            }

        }

        fun confirmToRemove(item: Item, title: String, subtitle: String = "") {
            val alertDialog = AlertDialog.Builder(itemView.context)
            alertDialog.setTitle(title)
            alertDialog.setMessage(subtitle)
            alertDialog.setOnCancelListener {

            }
            alertDialog.setPositiveButton("Confirmar") { _, _ -> item.removeRemote() }.show()
        }

        fun bind(
            item: Item,
            activity: Activity? = null
        ) {

            itemView.findViewById<TextView>(R.id.cellTitle).text = item.title()
            itemView.findViewById<TextView>(R.id.cellSubtitle).text = item.subtitle()


            val imageView = itemView.findViewById<ImageView>(R.id.cellAvatar)

            if (item !is Withdraw) {
                itemView.setOnLongClickListener {
                    confirmToRemove(item, "Deseja remover o item?", item.title())
                    true
                }
            }


            if (item is User) {
                itemView.setOnClickListener {
                    val intent = Intent(itemView.context, AddUserActivity::class.java)
                    intent.putExtra(USER_EXTRA, Parcels.wrap(User::class.java, item))
                    itemView.context.startActivity(intent)
                }
            }

            if (item is Bicycle && activity is WithdrawActivity) {
                val isAvailable = item.is_available ?: true
                if (!isAvailable) {
                    itemView.cellContainer.setBackgroundColor(itemView.resources.getColor(R.color.rent))
                } else {
                    itemView.cellContainer.setBackgroundColor(itemView.resources.getColor(R.color.white))
                }

                itemView.setOnClickListener {
                    if (isAvailable) {
                        val intent = Intent(itemView.context, ChooseUserActivity::class.java)
                        val withdrawalInProgress = Withdraw()
                        withdrawalInProgress.bicycle_name = item.name
                        withdrawalInProgress.bicycle_id = item.id
                        withdrawalInProgress.bicycle_image_path = item.photo_path

                        intent.putExtra(WITHDRAWAL_EXTRA, Parcels.wrap(Withdraw::class.java, withdrawalInProgress))
                        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                        activity.startActivityForResult(intent, Activity.RESULT_OK)
                    } else {
                        val intent = Intent(itemView.context, ReturnBikeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                        intent.putExtra(WITHDRAWAL_BICYCLE, Parcels.wrap(Bicycle::class.java, item))
                        activity.startActivityForResult(intent, Activity.RESULT_OK)

                    }
                }
            }

            if (item is Bicycle && activity != null && activity !is WithdrawActivity) {
                itemView.setOnClickListener {
                    val intent = Intent(it.context, AddBikeActivity::class.java)
                    intent.putExtra(BIKE_EXTRA, Parcels.wrap(Bicycle::class.java, item))
                    activity.startActivity(intent)
                }

            }

            if (item is Withdraw) {
                val withdrawIcon = if (item.isRent())
                    R.drawable.ic_bike_left_24dp
                else
                    R.drawable.ic_bike_returned_24dp

                imageView.setImageResource(withdrawIcon)
            } else {
                Glide.with(itemView.context)
                    .load(item.iconPath())
                    .into(imageView)
            }

        }
    }
}