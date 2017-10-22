package pettrack.controller;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import pettrack.domain.Pet;
import pettrack.domain.User;
import pettrack.service.PetRepository;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Controller
public class PetController {

    @Autowired
    private PetRepository repository;

    @GetMapping("/pets")
    public ModelAndView getPets(final ModelAndView modelAndView) {
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        final List<Pet> petsByUserId = this.repository.getPetsByUserId(user.getId());
        modelAndView.addObject("pets", petsByUserId);
        modelAndView.setViewName("pets");
        return modelAndView;
    }

    @GetMapping("pets/add")
    public ModelAndView showAddPetPage(final ModelAndView modelAndView, final Pet pet) {
        modelAndView.addObject("pet", pet);
        modelAndView.setViewName("addpet");
        return modelAndView;
    }

    @PostMapping("pets/add")
    public ModelAndView processAddPetForm(final ModelAndView modelAndView, @Valid final Pet pet, final BindingResult bindingResult, final HttpServletRequest request) {
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        pet.setId(ObjectId.get().toString());
        pet.setUserId(user.getId());
        final Pet saved = this.repository.save(pet);

        modelAndView.setViewName("addpetimage");
        request.getSession().setAttribute("currentPet", saved);
        return modelAndView;
    }

    @PostMapping("pets/add/image")
    public String processAddPetImageForm(final ModelAndView modelAndView, @RequestParam("image") final MultipartFile file, final HttpServletRequest request) {
        if (null != file && !file.isEmpty()) {
            final Pet currentpet = (Pet) request.getSession().getAttribute("currentPet");
            this.repository.uploadImage(currentpet, file);
        }

        return "redirect:/pets";
    }

    @GetMapping("pets/pet")
    public ModelAndView showPetDetails(final ModelAndView modelAndView, @RequestParam final String id) {
        final Pet pet = this.repository.getById(id);
        modelAndView.addObject("pet", pet);
        modelAndView.setViewName("petinfo");
        return modelAndView;
    }
}
