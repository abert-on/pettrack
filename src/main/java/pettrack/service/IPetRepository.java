package pettrack.service;

import org.springframework.web.multipart.MultipartFile;
import pettrack.domain.Pet;

import java.util.List;

public interface IPetRepository {

    List<Pet> getPetsByUserId(final String userID);

    void save(final Pet pet);

    void uploadImage(final Pet pet, final MultipartFile image);

    Pet getById(final String petId);
}
