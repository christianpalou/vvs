package es.udc.fi.dc.fd.signup;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.udc.fi.dc.fd.account.Account;
import es.udc.fi.dc.fd.account.AccountService;
import es.udc.fi.dc.fd.account.SaveNotAvailableException;
import es.udc.fi.dc.fd.support.web.*;
import es.udc.fi.dc.fd.account.*;

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

	@RequestMapping(value = "signup", method = RequestMethod.POST)
	public String signup(@Valid @ModelAttribute SignupForm signupForm, Errors errors, RedirectAttributes ra) {
		signupValidator.validate(signupForm, errors);
		if (errors.hasErrors()) {
			return SIGNUP_VIEW_NAME;
		}
		Account account;
		try {
			account = accountService.save(signupForm.createAccount());
			accountService.signin(account);
		} catch (SaveNotAvailableException e) {
			return SIGNUP_VIEW_NAME;
		}

		// see /WEB-INF/i18n/messages.properties and /WEB-INF/views/homeSignedIn.html
		MessageHelper.addSuccessAttribute(ra, "signup.success");
		return "redirect:/";
	}
}
