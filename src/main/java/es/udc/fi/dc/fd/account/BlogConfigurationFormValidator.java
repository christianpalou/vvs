package es.udc.fi.dc.fd.account;

import es.udc.fi.dc.fd.blog.BlogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class BlogConfigurationFormValidator implements Validator {

  @Autowired
  private BlogRepository blogRepository;

  @Override
  public boolean supports(Class<?> clazz) {
    return AccountConfigurationForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    BlogConfigurationForm blogConfigurationForm = (BlogConfigurationForm) target;
    if (blogRepository.findOneByName(blogConfigurationForm.getName()) != null) {
      errors.rejectValue("name", "blogName.message");
    }
  }
}
