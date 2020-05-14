package com.frienders.main.Search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.list.SearcherSingleIndexDataSource
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.stats.StatsConnector
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import io.ktor.client.features.logging.LogLevel

class MyViewModel : ViewModel() {

    val client = ClientSearch(ApplicationID("8UQ8VWWZTV"), APIKey("b28196daf92444aae4ac082108be63a3"), LogLevel.ALL)
    val index = client.initIndex(IndexName("index"))
    val searcher = SearcherSingleIndex(index)


    val dataSourceFactory = SearcherSingleIndexDataSource.Factory(searcher) { hit ->
        GroupModel(
                hit.json.getPrimitive("engName").content,
                hit.json.getPrimitive("engDesc").content
        )
    }


    val pagedListConfig = PagedList.Config.Builder().setPageSize(50).build()
    val groups: LiveData<PagedList<GroupModel>> = LivePagedListBuilder(dataSourceFactory, pagedListConfig).build()

    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(groups))
    val stats = StatsConnector(searcher)
    val connection = ConnectionHandler()

    init {
        connection += searchBox
        connection += stats
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
        connection.clear()
    }
}
