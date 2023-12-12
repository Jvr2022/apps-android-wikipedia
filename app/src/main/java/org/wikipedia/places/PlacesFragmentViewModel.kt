package org.wikipedia.places

import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.wikipedia.dataclient.ServiceFactory
import org.wikipedia.dataclient.WikiSite
import org.wikipedia.page.PageTitle
import org.wikipedia.util.ImageUrlUtil
import org.wikipedia.util.Resource

class PlacesFragmentViewModel(bundle: Bundle) : ViewModel() {

    var wikiSite: WikiSite = bundle.getParcelable(PlacesActivity.EXTRA_WIKI)!!

    val nearbyPages = MutableLiveData<Resource<List<NearbyPage>>>()

    fun fetchNearbyPages(latitude: Double, longitude: Double, radius: Int, maxResults: Int) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            nearbyPages.postValue(Resource.Error(throwable))
        }) {
            val response = ServiceFactory.get(wikiSite).getGeoSearch("$latitude|$longitude", radius, maxResults, maxResults)
            val pages = response.query?.pages.orEmpty()
                .filter { it.coordinates != null }
                .map {
                    NearbyPage(it.pageId, PageTitle(it.title, wikiSite,
                        if (it.thumbUrl().isNullOrEmpty()) null else ImageUrlUtil.getUrlForPreferredSize(it.thumbUrl()!!, PlacesFragment.THUMB_SIZE),
                        it.description,
                        it.displayTitle(wikiSite.languageCode)),
                        it.coordinates!![0].lat, it.coordinates[0].lon)
                }

            nearbyPages.postValue(Resource.Success(pages))
        }
    }

    class NearbyPage(
        val pageId: Int,
        val pageTitle: PageTitle,
        val latitude: Double,
        val longitude: Double,
        var annotation: Symbol? = null,
        var bitmap: Bitmap? = null
    )

    class Factory(private val bundle: Bundle) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlacesFragmentViewModel(bundle) as T
        }
    }
}