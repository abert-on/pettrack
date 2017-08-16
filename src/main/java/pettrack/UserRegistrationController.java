package pettrack;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pettrack.domain.User;
import pettrack.service.CustomerUserDetailsService;
import pettrack.service.EmailService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Controller
public class UserRegistrationController {

    @Autowired
    private CustomerUserDetailsService userDetailsService;

    @Autowired
    private EmailService mailService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @GetMapping("/registration")
    public ModelAndView showRegistrationPage(final ModelAndView modelAndView, final User user) {
        modelAndView.addObject("user", user);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @PostMapping("/registration")
    public ModelAndView processRegistrationForm(ModelAndView modelAndView, @Valid User user, BindingResult bindingResult, HttpServletRequest request) {
        if (this.userDetailsService.emailExists(user)) {
            modelAndView.addObject("alreadyRegisteredMessage", "Email address " + user.getUsername() + " is already registered.");
            bindingResult.reject("email");
        }

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("registration");
        }
        else {
            // disable user and set a confirmation token
            user.setEnabled(false);
            user.setGrantedAuthorities(Collections.singletonList("USER"));
            user.setConfirmationToken(UUID.randomUUID().toString());
            this.userDetailsService.save(user);

            String appUrl = request.getScheme() + "://" + request.getServerName();

            final SimpleMailMessage registrationEmail = new SimpleMailMessage();
            registrationEmail.setTo(user.getUsername());
            registrationEmail.setSubject("Registration Confirmation");
            registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
                    + appUrl + "/confirm?token=" + user.getConfirmationToken());
            registrationEmail.setFrom("mcttanglewood@aol.com");

            mailService.sendEmail(registrationEmail);

            modelAndView.addObject("confirmationMessage", "A confirmation e-mail has been sent to " + user.getUsername());
            modelAndView.setViewName("registration");
        }
        return modelAndView;
    }

    @GetMapping("/confirm")
    public ModelAndView showConfirmationPage(ModelAndView modelAndView, @RequestParam("token") final String token) {
        final User user = this.userDetailsService.findByConfirmationToken(token);

        if (user == null) {
            modelAndView.addObject("invalidToken", "This is an invalid confirmation link.");
        }
        else {
            modelAndView.addObject("confirmationToken", user.getConfirmationToken());
        }

        modelAndView.setViewName("confirm");
        return modelAndView;
    }

    @PostMapping("/confirm")
    public ModelAndView processConfirmation(final ModelAndView modelAndView, final BindingResult bindingResult, @RequestParam final Map<String, String> requestParams, final RedirectAttributes redir) {
        modelAndView.setViewName("confirm");

        final Zxcvbn passwordCheck = new Zxcvbn();

        final Strength strength = passwordCheck.measure(requestParams.get("password"));

        if (strength.getScore() < 3) {
            bindingResult.reject("password");
            redir.addFlashAttribute("errorMessage", "Your password is too weak. Choose a stronger one.");

            modelAndView.setViewName("redirect:confirm?token=" + requestParams.get("token"));
            return modelAndView;
        }

        final User user = userDetailsService.findByConfirmationToken(requestParams.get("token"));
        user.setPassword(this.encoder.encode(requestParams.get("password")));
        user.setEnabled(true);

        this.userDetailsService.save(user);

        modelAndView.addObject("successMessage", "Your password has been set.");
        return modelAndView;

    }
}
