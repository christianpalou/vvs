package es.udc.fi.dc.fd.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class AccountConfigurationFormValidator implements Validator {

	@Autowired
	private AccountRepository accountRepository;

	@Override
	public boolean supports(Class<?> clazz) {
		return AccountConfigurationForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		AccountConfigurationForm signupForm = (AccountConfigurationForm) target;
		if (accountRepository.findOneByUserName(signupForm.getUserName()) != null){
			errors.rejectValue("userName", "accountUserName.message");
		}
	}
}