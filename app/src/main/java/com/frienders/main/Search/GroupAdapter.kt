package com.frienders.main.Search

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.frienders.main.R
import com.frienders.main.SplashActivity
import com.frienders.main.activity.group.GroupChatActivity
import com.frienders.main.activity.profile.SettingActivity
import com.frienders.main.config.ActivityParameters
import com.frienders.main.config.GroupFirebaseFields

class GroupAdapter(context: Context?) : PagedListAdapter<GroupModel, GroupViewHolder>(GroupAdapter) {
    val contexc = context;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)


        return GroupViewHolder(view);
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val groupModel = getItem(position)

        if (groupModel != null)
        {
            holder.bind(groupModel)
            holder.itemView.setOnClickListener(View.OnClickListener {
                val groupId = groupModel.id;
                val intent = Intent(contexc, GroupChatActivity::class.java);
                intent.putExtra(GroupFirebaseFields.GROUPID, groupId);
                if (contexc != null) {
                    contexc.startActivity(intent)
                };
            });
        }
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
