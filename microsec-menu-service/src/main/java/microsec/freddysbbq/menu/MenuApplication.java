package microsec.freddysbbq.menu;

import microsec.common.Menubootstrap;
import microsec.freddysbbq.menu.model.v1.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

import javax.annotation.PostConstruct;

@SpringBootApplication(exclude= {io.pivotal.spring.cloud.IssuerCheckConfiguration.class})
@EntityScan(basePackageClasses = MenuItem.class)
@EnableResourceServer
@EnableDiscoveryClient
public class MenuApplication {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(MenuApplication.class, args);
    }

    @Autowired
    private MenuItemRepository menuRepository;

    @Bean
    public Menubootstrap menuBootstrap() {
        return new Menubootstrap();
    }

    @PostConstruct
    public void bootstrap() {
        if (menuRepository.count() == 0) {
            menuRepository.saveAll(menuBootstrap().getItems());
        }
    }

    @Configuration
    public static class RepositoryConfig extends RepositoryRestConfigurerAdapter {
        @Override
        public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
            config.exposeIdsFor(MenuItem.class);
        }
    }

    @Bean
    public ResourceServerConfigurer resourceServer(SecurityProperties securityProperties) {
        return new ResourceServerConfigurerAdapter() {
            @Override
            public void configure(ResourceServerSecurityConfigurer resources) {
                resources.resourceId("menu");
            }

            @Override
            public void configure(HttpSecurity http) throws Exception {
                http
                        .authorizeRequests()
                        .antMatchers(HttpMethod.GET, "/manage/**").access("#oauth2.hasScope('admin.read')")
                        .antMatchers(HttpMethod.POST, "/manage/**").access("#oauth2.hasScope('admin.write')")
                        .antMatchers(HttpMethod.GET, "/menuItems/**").access("#oauth2.hasScope('menu.read')")
                        .antMatchers(HttpMethod.POST, "/menuItems/**").access("#oauth2.hasScope('menu.write')")
                        .antMatchers(HttpMethod.PUT, "/menuItems/**").access("#oauth2.hasScope('menu.write')")
                        .antMatchers(HttpMethod.DELETE, "/menuItems/**").access("#oauth2.hasScope('menu.write')");
            }
        };
    }
}