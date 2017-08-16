package pettrack;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import pettrack.storage.StorageFileNotFoundException;
import pettrack.storage.StorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class ImageUploadControllerTest {

    private final UserDetails userDetails = new User("user", "password", Collections.singletonList(new SimpleGrantedAuthority("USER")));

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StorageService storageService;

    @Test
    public void shouldListAllFiles() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));

        this.mvc.perform(get("/images").with(csrf()).with(user(this.userDetails)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("images",
                        Matchers.contains("http://localhost/images/first.txt",
                                "http://localhost/images/second.txt")));
    }

    @Test
    public void shouldSaveUploadedFile() throws Exception {
        final MockMultipartFile multipartFile =
                new MockMultipartFile("image", "test.txt", "text/plain", "Spring Framework".getBytes());

        this.mvc.perform(fileUpload("/images").file(multipartFile).with(csrf()).with(user(this.userDetails)))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/images"));

        then(this.storageService).should().store(multipartFile);
    }

    @Test
    public void should404WhenMissingFile() throws Exception {
        given(this.storageService.loadAsResource("test.txt"))
                .willThrow(StorageFileNotFoundException.class);

        this.mvc.perform(get("/images/test.txt").with(csrf()).with(user(this.userDetails)))
                .andExpect(status().isNotFound());

        then(this.storageService).should(times(1)).loadAsResource("test.txt");
    }
}