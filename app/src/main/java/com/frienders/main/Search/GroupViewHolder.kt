package com.frienders.main.Search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.group_item.view.*

class GroupViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(product: GroupModel) {
        view.groupName.text = product.name
        view.city.text = product.desc;
    }
}
