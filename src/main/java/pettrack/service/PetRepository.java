package pettrack.service;

import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pettrack.domain.Pet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class PetRepository implements IPetRepository {

    @Value("${pet.api}")
    private String apiUrl;

    private static final String PETS = "pets";
    private static final String PET = "pet";

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<Pet> getPetsByUserId(final String userID) {
        ResponseEntity<List<Pet>> petsResponse =
                this.restTemplate.exchange(apiUrl + PETS + "/" + userID,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Pet>>() {});
        return petsResponse.getBody();
    }

    @Override
    public Pet save(final Pet pet) {
        final Pet existingPet = getById(pet.getId());

        HttpEntity<Pet> request = new HttpEntity<>(pet);

        if (existingPet == null) {
            return restTemplate.postForObject(apiUrl + PETS, request, Pet.class);
        }
        else {
            restTemplate.put(apiUrl + PETS, request);
            return pet;
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

    @Override
    public Pet getById(final String petId) {
        return this.restTemplate.getForObject(apiUrl + PET + "/" + petId,
                Pet.class,
                Collections.singletonMap("id", petId));
    }
}
