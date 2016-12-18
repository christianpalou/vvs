package es.udc.fi.dc.fd.signup;

import es.udc.fi.dc.fd.account.Account;
import es.udc.fi.dc.fd.account.AccountService;
import es.udc.fi.dc.fd.account.SaveNotAvailableException;
import es.udc.fi.dc.fd.support.web.MessageHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class SignupController {

  private static final String SIGNUP_VIEW_NAME = "signup/signup";

  @Autowired
  private AccountService accountService;

  @Autowired
  private SignupFormValidator signupValidator;

  @RequestMapping(value = "signup")
  public String signup(Model model) {
    model.addAttribute(new SignupForm());
    return SIGNUP_VIEW_NAME;
  }
/**
 * signup
 * @param signupForm Formulario para darse de alta
 * @param errors Errores
 * @param ra
 * @return
 */
  @RequestMapping(value = "signup", method = RequestMethod.POST)
  public String signup(@Valid @ModelAttribute SignupForm signupForm, Errors errors,
      RedirectAttributes ra) {
    signupValidator.validate(signupForm, errors);
    if (errors.hasErrors()) {
      return SIGNUP_VIEW_NAME;
    }
    Account account;
    try {
      account = accountService.save(signupForm.createAccount());
      accountService.signin(account);
    } catch (SaveNotAvailableException exception) {
      return SIGNUP_VIEW_NAME;
    }

    // see /WEB-INF/i18n/messages.properties and /WEB-INF/views/homeSignedIn.html
    MessageHelper.addSuccessAttribute(ra, "signup.success");
    return "redirect:/";
  }
}
