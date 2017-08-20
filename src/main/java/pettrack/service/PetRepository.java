package pettrack.service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import gherkin.deps.com.google.gson.Gson;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pettrack.domain.Pet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@Service
public class PetRepository implements IPetRepository {

    @Autowired
    private MongoClient mongoClient;

    @Override
    public List<Pet> getPetsByUserId(final String userID) {
        final MongoCollection<Document> collection = getPetsCollection();

        final Gson gson = new Gson();
        final List<Pet> petsFound = new ArrayList<>();
        for (final Document petDoc : collection.find(eq("userId", userID))) {
            final Pet pet = gson.fromJson(petDoc.toJson(), Pet.class);
            petsFound.add(pet);
        }

        return petsFound;
    }

    @Override
    public void save(final Pet pet) {
        final MongoCollection<Document> collection = getPetsCollection();

        if (collection.find(eq("id", pet.getId())).first() == null) {
            collection.insertOne(Document.parse(new Gson().toJson(pet)));
        }
        else {
            collection.updateOne(eq("id", pet.getId()), new Document("$set", Document.parse(new Gson().toJson(pet))));
        }
    }

    @Override
    public void uploadImage(final Pet pet, final MultipartFile image) {
        try {
            pet.setImage(StringUtils.newStringUtf8(Base64.encodeBase64(image.getBytes(), false)));
            save(pet);
        }
        catch (final IOException exception) {
            throw new ImageUploadException("Failed to upload image " + image.getName() + " for " + pet.getName() + '.');
        }
    }

    private MongoCollection<Document> getPetsCollection() {
        final MongoDatabase db = this.mongoClient.getDatabase("pettrack1");
        return db.getCollection("pets");
    }
}
