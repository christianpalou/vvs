package es.udc.fi.dc.fd.account;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.udc.fi.dc.fd.blog.Blog;

@SuppressWarnings("serial")
@Entity
@Table(name = "account")
public class Account implements java.io.Serializable {
	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String userName;

	@JsonIgnore
	private String password;

	private String role = "ROLE_USER";

	private Instant created;

	@ManyToMany(mappedBy = "followers", fetch = FetchType.EAGER)
	private List<Blog> followingBlogs;

	@OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
	private List<Blog> blogs;

	private boolean image;

	protected Account() {

	}

	public Account(String email, String password, String role, String userName) {
		this.email = email;
		this.password = password;
		this.role = role;
		this.userName=userName;
		this.created = Instant.now();
		this.followingBlogs = new ArrayList<Blog>();
		this.blogs = new ArrayList<Blog>();
		this.image=false;
	}

	public List<Blog> getFollowingBlogs() {
		return followingBlogs;
	}

	public void setFollowingBlogs(List<Blog> following) {
		this.followingBlogs = following;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Instant getCreated() {
		return created;
	}
	public List<Blog> getBlogs() {
		return blogs;
	}
	public void setBlogs(List<Blog> blogs) {
		this.blogs = blogs;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void addBlog(Blog blog){
		blogs.add(blog);
		blog.setAccount(this);
	}

	public void addFollowingBlog(Blog blog){
		this.followingBlogs.add(blog);
		blog.addFollower(this);
	}

	public boolean isImage() {
		return image;
	}

	public void setImage(boolean image) {
		this.image = image;
	}
}
