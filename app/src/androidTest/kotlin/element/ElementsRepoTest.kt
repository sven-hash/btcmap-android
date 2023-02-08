package element

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import db.inMemoryDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ElementsRepoTest {

    private lateinit var queries: ElementQueries

    private lateinit var repo: ElementsRepo

    @Before
    fun beforeEach() {
        queries = ElementQueries(inMemoryDatabase())

        repo = ElementsRepo(
            app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application,
            elementQueries = queries,
            json = Json.Default,
            httpClient = OkHttpClient(),
        )
    }

    @Test
    fun selectCategories() = runBlocking {
        queries.insertOrReplace(
            listOf(
                testElement().copy(tags = JsonObject(mapOf("category" to JsonPrimitive("a")))),
                testElement().copy(tags = JsonObject(mapOf("category" to JsonPrimitive("b")))),
            )
        )

        assertEquals(
            listOf(ElementCategory("a", "", 1), ElementCategory("b", "", 1)),
            repo.selectCategories()
        )
    }
}