package es.udc.fi.dc.fd.config;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.springframework.security.web.FilterChainProxy;

import javax.inject.Inject;

public abstract class WebSecurityConfigurationAware extends WebAppConfigurationAware {

  @Inject
  private FilterChainProxy springSecurityFilterChain;

  @Before
  public void before() {
    this.mockMvc = webAppContextSetup(this.wac).addFilters(this.springSecurityFilterChain).build();
  }
}
