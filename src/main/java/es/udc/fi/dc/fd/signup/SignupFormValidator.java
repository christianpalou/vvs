package es.udc.fi.dc.fd.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import es.udc.fi.dc.fd.account.AccountRepository;

@Component
public class SignupFormValidator implements Validator {

	@Autowired
	private AccountRepository accountRepository;

	@Override
	public boolean supports(Class<?> clazz) {
		return SignupForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		SignupForm signupForm = (SignupForm) target;
		if (accountRepository.findOneByUserName(signupForm.getUserName()) != null){
			errors.rejectValue("userName", "accountUserName.message");
		}
	}
}