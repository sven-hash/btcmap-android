package filter

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import api.ApiImpl
import db.inMemoryDatabase
import element.ElementQueries
import element.ElementsRepo
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FilterElementsModelTest {

    private lateinit var model: FilterElementsModel

    @Before
    fun beforeEach() {
        model = FilterElementsModel(ElementsRepo(
            api = ApiImpl(OkHttpClient(), Json.Default),
            app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application,
            queries = ElementQueries(inMemoryDatabase()),
            json = Json.Default,
        ))
    }

    @Test
    fun initialState() {
        assertEquals(
            FilterElementsModel.State.Loading,
            model.state.value,
        )
    }

    @Test
    fun loadData() = runBlocking {
        model.onViewCreated(emptyList())

        val state = model.state.filterIsInstance<FilterElementsModel.State.Loaded>().first()

        assertEquals(
            0,
            state.items.size,
        )
    }
}