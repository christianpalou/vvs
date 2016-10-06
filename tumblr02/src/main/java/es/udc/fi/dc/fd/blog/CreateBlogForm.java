package es.udc.fi.dc.fd.blog;

import org.hibernate.validator.constraints.NotBlank;

public class CreateBlogForm {

	private static final String NOT_BLANK_MESSAGE = "{notBlank.message}";

	@NotBlank(message = CreateBlogForm.NOT_BLANK_MESSAGE)
	private String name;

	@NotBlank(message = CreateBlogForm.NOT_BLANK_MESSAGE)
	private String title;

	@NotBlank(message = CreateBlogForm.NOT_BLANK_MESSAGE)
	private String description;

	private boolean privacy;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isPrivacy() {
		return privacy;
	}

	public void setPrivacy(boolean privacy) {
		this.privacy = privacy;
	} 

	public Blog createBlog(){
		return new Blog(name, title,description,privacy,null);
	}

}
