package es.udc.fi.dc.fd.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CreateBlogFormValidator implements Validator {

  @Autowired
  private BlogRepository blogRepository;

  @Override
  public boolean supports(Class<?> clazz) {
    return CreateBlogForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    CreateBlogForm createBlogForm = (CreateBlogForm) target;
    if (blogRepository.findOneByName(createBlogForm.getName()) != null) {
      errors.rejectValue("name", "blogName.message");
    }
  }
}
