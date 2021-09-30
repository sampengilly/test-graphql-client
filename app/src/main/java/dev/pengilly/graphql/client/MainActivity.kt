package dev.pengilly.graphql.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import dev.pengilly.graphql.client.ui.theme.GraphqlclientTheme
import dev.pengilly.starwars.HeroesQuery
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraphqlclientTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    var heroes by remember { mutableStateOf<List<HeroViewData>>(emptyList()) }

                    LaunchedEffect(true) {
                        heroes = getHeroes()
                    }

                    Heroes(heroes = heroes)
                }
            }
        }
    }
}

@Composable
fun Heroes(heroes: List<HeroViewData>) {
    Column {
        heroes.forEach {
            Hero(name = it.name)
        }
    }
}

@Composable
fun Hero(name: String) {
    Text(text = "Hello $name!")
}

val starWarsClient = GraphClient("http://10.0.2.2:8080/graphql")

private suspend fun getHeroes(): List<HeroViewData> = starWarsClient.query(HeroesQuery()).heroes
    .map { HeroViewData(it.name) }

data class HeroViewData(
    val name: String
)

class GraphClient(baseUrl: String, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

    private val apolloClient = ApolloClient.builder()
        .serverUrl(baseUrl)
        .build()

    suspend fun <D : Operation.Data, T, V : Operation.Variables> query(query: Query<D, T, V>): T =
        withContext(dispatcher) {
            val response = try {
                apolloClient.query(query).await()
            } catch (ex: ApolloException) {
                throw Exception("Something went wrong")
            }

            val data = response.data
            if (data == null || response.hasErrors()) {
                throw Exception("Something went wrong")
            }

            data
        }

}