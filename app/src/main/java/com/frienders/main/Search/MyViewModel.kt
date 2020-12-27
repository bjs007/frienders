package com.frienders.main.Search

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
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
import com.frienders.main.config.Configuration
import com.frienders.main.config.GroupFirebaseFields
import com.frienders.main.utility.Utility
import io.ktor.client.features.logging.LogLevel


class MyViewModel : ViewModel() {

    val client = ClientSearch(ApplicationID("8UQ8VWWZTV"), APIKey("b28196daf92444aae4ac082108be63a3"), LogLevel.ALL)
    val index = client.initIndex(IndexName("index"))
    val searcher = SearcherSingleIndex(index)
    var lang : String = "eng"
    var langName: String = "engName"
    var langDesc : String = "engDesc"

    init {
//        FirebasePaths.firebaseUserRef(FirebaseAuthProvider.getCurrentUserId()).child(UsersFirebaseFields.language)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onCancelled(p0: DatabaseError)
//                    {
//                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                    }
//
//                    override fun onDataChange(dataSnapshot : DataSnapshot)
//                    {
//                        if(dataSnapshot.exists())
//                        {
//
//                        }
//                    }
//
//                });

        lang = Utility.getDeviceLanguage();
        if(lang.equals("en"))
        {
            lang = "eng";
        }
        else
        {
            lang = "hin";
        }
        langName =  lang + "Name"
        langDesc = lang + "Desc";
    }


    val dataSourceFactory = SearcherSingleIndexDataSource.Factory(searcher) { hit ->
        GroupModel(
                hit.json.getPrimitive(langName).content,
                hit.json.getPrimitive(langDesc).content,
                hit.json.getPrimitive(GroupFirebaseFields.INDEX_ID).content
        )
    }

    val pagedListConfig = PagedList.Config.Builder().setEnablePlaceholders(false).setPageSize(50).build()
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
