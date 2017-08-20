package pettrack.controller;

import pettrack.storage.StorageFileNotFoundException;
import pettrack.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
public class ImageUploadController {

    private final StorageService storageService;

    public ImageUploadController(final StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/images")
    public String listUploadedImages(final Model model) {
        model.addAttribute("images", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(ImageUploadController.class,
                        "serveImage", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveImage(@PathVariable final String filename) {
        final Resource file = storageService.loadAsResource(filename);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/images")
    public String handleImageUpload(@RequestParam("image") final MultipartFile file,
                                  final RedirectAttributes redirectAttributes) {
        storageService.store(file);

        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + '!');

        return "redirect:/images";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(final StorageFileNotFoundException exception) {
        return ResponseEntity.notFound().build();
    }

}
