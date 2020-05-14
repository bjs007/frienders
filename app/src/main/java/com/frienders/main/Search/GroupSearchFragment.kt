package com.frienders.main.Search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.item.StatsTextView
import com.algolia.instantsearch.helper.android.list.autoScrollToStart
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxViewAppCompat
import com.algolia.instantsearch.helper.android.searchbox.connectView
import com.algolia.instantsearch.helper.stats.StatsPresenterImpl
import com.algolia.instantsearch.helper.stats.connectView
import com.frienders.main.R
import kotlinx.android.synthetic.main.group_fragment.*

public class GroupSearchFragment : Fragment() {

    private val connection = ConnectionHandler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.group_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProviders.of(requireActivity())[MyViewModel::class.java]

        // Hits
        // ...

        val groupProduct = GroupAdapter()
        viewModel.groups.observe(viewLifecycleOwner, Observer { hits -> groupProduct.submitList(hits) })
        groupList.let {
            it.itemAnimator = null
            it.adapter = groupProduct
            it.layoutManager = LinearLayoutManager(requireContext())
            it.autoScrollToStart(groupProduct)
        }

        val searchBoxView = SearchBoxViewAppCompat(searchView)

        connection += viewModel.searchBox.connectView(searchBoxView)

        val statsView = StatsTextView(stats)
        connection += viewModel.stats.connectView(statsView, StatsPresenterImpl())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        connection.clear()
    }


}
