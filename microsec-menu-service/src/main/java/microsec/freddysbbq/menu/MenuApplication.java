package microsec.freddysbbq.menu;

import microsec.common.MenuBootstrap;
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

@SpringBootApplication
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
    public MenuBootstrap menuBootstrap() {
        return new MenuBootstrap();
    }

    @PostConstruct
    public void bootstrap() {
        if (menuRepository.count() == 0) {
            menuRepository.save(menuBootstrap().getItems());
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
//                logger.debug("securityProperties.isRequireSsl(): "+securityProperties.isRequireSsl());
//                if (securityProperties.isRequireSsl()) {
//                    http.requiresChannel().anyRequest().requiresSecure();
//                }
                http
//                        .addFilterBefore(new LogFilter(), X509AuthenticationFilter.class)
//                        .x509()
//                        .subjectPrincipalRegex("CN=(.*?),")
//                        .and()
                        .authorizeRequests()
                        .antMatchers(HttpMethod.GET, "/**").access("#oauth2.hasScope('menu.read')")
                        .antMatchers(HttpMethod.POST, "/**").access("#oauth2.hasScope('menu.write')")
                        .antMatchers(HttpMethod.PUT, "/**").access("#oauth2.hasScope('menu.write')")
                        .antMatchers(HttpMethod.DELETE, "/**").access("#oauth2.hasScope('menu.write')");
            }
        };
    }

//    static {
//        System.setProperty("javax.net.debug", "ssl");
//    }
//    static class LogFilter implements Filter {
//
//        @Override
//        public void init(FilterConfig filterConfig) {
//
//        }
//
//        @Override
//        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//            HttpServletRequest request =  (HttpServletRequest)servletRequest;
//            logger.debug("X-Forward-Client-Cert: " + request.getHeader("X-Forwarded-Client-Cert"));
//            filterChain.doFilter(servletRequest, servletResponse);
//        }
//
//        @Override
//        public void destroy() {
//
//        }
//    }
}