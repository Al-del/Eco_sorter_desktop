import androidx.compose.runtime.identityHashCode
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import org.bson.Document
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.serialization.decodeFromString

import kotlin.random.Random
public var random_numero : Int = 0
@Serializable
data class LocationResponse(
    val latitude: Double,
    val longitude: Double
)
suspend fun getLocation(): LocationResponse? {
    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }

    // Replace 'YOUR_API_KEY' with your actual API key
    val url = "https://api.ip2location.io/?key=1610A6C51C6B6FDF301C33BA01F4A6D9&ip=2a02:2f0d:a1e:2400:1ff5:557e:4490:45ba"

    return try {
        val response: String = client.get(url)
        println("Raw response: $response")
        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<LocationResponse>(response)
    } catch (e: Exception) {
        println("Error: ${e.message}")
        null
    } finally {
        client.close()
    }
}
object MongoClientConnectionExample {
    suspend fun push_code(colection: String) {
        // Replace the placeholders with your credentials and hostname
        val connectionString = "mongodb+srv://admin:admin@ecosorter.x4owlln.mongodb.net/?retryWrites=true&w=majority&appName=EcoSorter"

        // Create a new client and connect to the server
        val mongoClient: MongoClient = MongoClients.create(connectionString)
        val database = mongoClient.getDatabase("EcoSorter")

        // Get a collection reference
        val collection: MongoCollection<Document> = database.getCollection(colection)
        var doc : Document? = null
        if(colection== "Redeem_code"){
            val code = generateRandomNumber()
            val existingDoc = collection.find(Document("code", code)).firstOrNull()
            if (existingDoc == null) {
                doc = Document("code", code)
                collection.insertOne(doc)
                println("Document inserted successfully")
            } else {
                println("Document with code $code already exists")
            }
        }else{
            if(colection== "location") {
                try {
                    val location = getLocation()
                    println(location)
                    if (location != null) {
                        println("Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                        val existingDoc = collection.find(Document("latitude", location.latitude).append("longitude", location.longitude)).firstOrNull()
                        if (existingDoc == null) {
                            doc = Document("latitude", location.latitude)
                                .append("longitude", location.longitude)
                            collection.insertOne(doc)
                            println("Document inserted successfully")
                        } else {
                            println("Document with location ${location.latitude}, ${location.longitude} already exists")
                        }
                    }
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                    return
                }
            }
        }
    }
    fun generateRandomNumber(): Int {
        random_numero = Random.nextInt(100000, 1000000)
        return random_numero
    }
}