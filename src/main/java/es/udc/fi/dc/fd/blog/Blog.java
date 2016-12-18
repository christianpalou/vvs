package es.udc.fi.dc.fd.blog;

import es.udc.fi.dc.fd.account.Account;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "blog")
public class Blog implements java.io.Serializable {

  @Id
  @GeneratedValue
  private Long blogId;

  @Column(unique = true)
  private String name;

  private String title;

  private String description;

  /* Si privacy = true, es privado. En caso contrario, es público */
  private boolean privacy;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "id")
  private Account account;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "blog_follower", joinColumns = @JoinColumn(name = "blog_id", referencedColumnName = "blogId"), inverseJoinColumns = @JoinColumn(name = "follower_id", referencedColumnName = "id"))
  private List<Account> followers;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "blog_followerRequest", joinColumns = @JoinColumn(name = "blog_id", referencedColumnName = "blogId"), inverseJoinColumns = @JoinColumn(name = "followerRequest_id", referencedColumnName = "id"))
  private List<Account> followRequests;

  private boolean image;

  protected Blog() {

  }

  /**
   * h Blog
   * 
   * @param name
   *          nombre
   * @param title
   *          titulo
   * @param description
   *          descripcion
   * @param privacy
   *          privacidad
   * @param account
   *          cuenta
   */
  public Blog(String name, String title, String description, boolean privacy, Account account) {
    this.name = name;
    this.title = title;
    this.description = description;
    this.account = account;
    this.privacy = privacy;
    this.followers = new ArrayList<Account>();
    this.followRequests = new ArrayList<Account>();
    this.image = false;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public List<Account> getFollowers() {
    return followers;
  }

  public void setFollowers(List<Account> followers) {
    this.followers = followers;
  }

  public List<Account> getFollowRequests() {
    return followRequests;
  }

  public void setFollowRequests(List<Account> followRequests) {
    this.followRequests = followRequests;
  }

  public Long getBlogId() {
    return blogId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void addFollower(Account account) {
    this.followers.add(account);
  }

  public void addFollowRequest(Account account) {
    this.followRequests.add(account);
  }

  public boolean isImage() {
    return image;
  }

  public void setImage(boolean image) {
    this.image = image;
  }

}
