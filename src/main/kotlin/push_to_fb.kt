import com.mongodb.client.MongoClients
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import org.bson.Document
import kotlinx.coroutines.runBlocking

object MongoClientConnectionExample {
    fun main(colection: String,  doc: Document) {
        // Replace the placeholders with your credentials and hostname
        val connectionString = "mongodb+srv://admin:admin@ecosorter.x4owlln.mongodb.net/?retryWrites=true&w=majority&appName=EcoSorter"

        // Create a new client and connect to the server
        val mongoClient: MongoClient = MongoClients.create(connectionString)
        val database = mongoClient.getDatabase("EcoSorter")

        // Get a collection reference
        val collection: MongoCollection<Document> = database.getCollection(colection)



        // Insert the document into the collection
        collection.insertOne(doc)

        println("Document inserted successfully")
    }
}