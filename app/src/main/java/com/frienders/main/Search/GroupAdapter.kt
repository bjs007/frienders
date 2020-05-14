package com.frienders.main.Search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.frienders.main.R

class GroupAdapter : PagedListAdapter<GroupModel, GroupViewHolder>(GroupAdapter) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)

        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val groupModel = getItem(position)

        if (groupModel != null) holder.bind(groupModel)
    }

    companion object : DiffUtil.ItemCallback<GroupModel>() {

        override fun areItemsTheSame(
                oldItem: GroupModel,
                newItem: GroupModel
        ): Boolean {
            return oldItem::class == newItem::class
        }

        override fun areContentsTheSame(
                oldItem: GroupModel,
                newItem: GroupModel
        ): Boolean {
            return oldItem.name == newItem.name
        }
    }
}
