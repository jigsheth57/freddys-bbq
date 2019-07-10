package microsec.freddysbbq.admin_app;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import microsec.common.Branding;
import microsec.common.DumpTokenEndpointConfig;
import microsec.freddysbbq.menu.model.v1.MenuItem;
import microsec.freddysbbq.order.model.v1.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;

@SpringBootApplication(exclude= {io.pivotal.spring.cloud.IssuerCheckConfiguration.class})
@Controller
@EnableOAuth2Sso
@EnableDiscoveryClient
@EnableCircuitBreaker
@Import(DumpTokenEndpointConfig.class)
public class AdminApplication extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .expressionHandler(new OAuth2WebSecurityExpressionHandler())
                .anyRequest().access("#oauth2.hasScope('menu.write')")
                .antMatchers(HttpMethod.GET, "/manage/**").access("#oauth2.hasScope('admin.read')")
                .antMatchers(HttpMethod.POST, "/manage/**").access("#oauth2.hasScope('admin.write')");
        // Customize the application security
        http.requiresChannel().anyRequest().requiresSecure();
    }

    @Autowired
    @Qualifier("loadBalancedOauth2RestTemplate")
    private OAuth2RestTemplate restTemplate;

    @Bean
    public Branding branding() {
        return new Branding();
    }

    @LoadBalanced
    @Bean
    public OAuth2RestTemplate loadBalancedOauth2RestTemplate(
            OAuth2ProtectedResourceDetails resource,
            OAuth2ClientContext oauth2Context) {
        return new OAuth2RestTemplate(resource, oauth2Context);
    }

    @RequestMapping("/")
    public String index(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "index";
    }

    @HystrixCommand
    @RequestMapping("/menuItems")
    public String menu(Model model) {
        PagedResources<MenuItem> menu = restTemplate
                .exchange(
                        "//menu-service/menuItems",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<PagedResources<MenuItem>>() {
                        })
                .getBody();
        model.addAttribute("menu", menu.getContent());
        return "menu";
    }

    @RequestMapping("/menuItems/new")
    public String newMenuItem(Model model) {
        model.addAttribute(new MenuItem());
        return "menuItem";
    }

    @HystrixCommand
    @RequestMapping(method = RequestMethod.POST, value = "/menuItems/new/")
    public String saveNewMenuItem(@ModelAttribute MenuItem menuItem) {
        restTemplate
                .postForEntity("//menu-service/menuItems/", menuItem, Void.class);
        return "redirect:..";
    }

    @HystrixCommand
    @RequestMapping("/menuItems/{id}")
    public String viewMenuItem(Model model, @PathVariable String id) {
        Resource<MenuItem> item = restTemplate
                .exchange(
                        "//menu-service/menuItems/{id}",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<Resource<MenuItem>>() {
                        }, id)
                .getBody();
        model.addAttribute("menuItem", item.getContent());
        return "menuItem";
    }

    @HystrixCommand
    @RequestMapping(method = RequestMethod.POST, value = "/menuItems/{id}")
    public String saveMenuItem(@PathVariable String id, @ModelAttribute MenuItem menuItem) {
        restTemplate.put("//menu-service/menuItems/{id}", menuItem, id);
        return "redirect:..";
    }

    @HystrixCommand
    @RequestMapping(method = RequestMethod.POST, value = "/menuItems/{id}/delete")
    public String deleteMenuItem(@PathVariable String id, @ModelAttribute MenuItem menuItem) {
        restTemplate.delete("//menu-service/menuItems/{id}", id);
        return "redirect:..";
    }

    @HystrixCommand
    @RequestMapping("/orders/")
    public String viewOrders(Model model) {
        PagedResources<Order> orders = restTemplate
                .exchange(
                        "//order-service/orders",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<PagedResources<Order>>() {
                        })
                .getBody();
        model.addAttribute("orders", orders.getContent());
        return "orders";
    }

    @HystrixCommand
    @RequestMapping(method = RequestMethod.POST, value = "/orders/{id}/delete")
    public String deleteOrder(@PathVariable String id) {
        restTemplate.delete("//order-service/orders/{id}", id);
        return "redirect:..";
    }

}
