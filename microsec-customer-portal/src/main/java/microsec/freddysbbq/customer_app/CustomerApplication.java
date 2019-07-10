package microsec.freddysbbq.customer_app;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import microsec.common.Branding;
import microsec.common.DumpTokenEndpointConfig;
import microsec.common.Menubootstrap;
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
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

@SpringBootApplication(exclude= {io.pivotal.spring.cloud.IssuerCheckConfiguration.class})
@Controller
@EnableOAuth2Sso
@EnableDiscoveryClient
@EnableCircuitBreaker
@Import(DumpTokenEndpointConfig.class)
public class CustomerApplication extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // Customize the application security
        http.requiresChannel().anyRequest().requiresSecure();
        http.authorizeRequests().anyRequest().authenticated()
                .antMatchers(HttpMethod.GET, "/manage/**").access("#oauth2.hasScope('admin.read')")
                .antMatchers(HttpMethod.POST, "/manage/**").access("#oauth2.hasScope('admin.write')");
    }

    @Autowired
    @Qualifier("loadBalancedOauth2RestTemplate")
    private OAuth2RestTemplate restTemplate;

    @Bean
    public Branding branding() {
        return new Branding();
    }

    @Bean
    public Menubootstrap menubootstrap() {
        return new Menubootstrap();
    }

    @LoadBalanced
    @Bean
    public OAuth2RestTemplate loadBalancedOauth2RestTemplate(
            OAuth2ProtectedResourceDetails resource,
            OAuth2ClientContext oauth2Context) {
        return new OAuth2RestTemplate(resource, oauth2Context);
    }

    @Autowired
    private Menubootstrap menubootstrap;

    @RequestMapping("/")
    public String index(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "index";
    }

    @HystrixCommand(fallbackMethod = "menuFallback",
            commandProperties = {
                    @HystrixProperty(name="execution.isolation.strategy", value="THREAD"),
                    @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="1500")
            })
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

    public String menuFallback(Model model) {
        logger.debug("Retrieve static menu info");
        model.addAttribute("menu", menubootstrap.getItems());
        model.addAttribute("menuServiceDown", true);
        return "menu";
    }

    @RequestMapping("/myorders")
    @HystrixCommand(fallbackMethod = "myOrdersFallback",
            commandProperties = {
                    @HystrixProperty(name="execution.isolation.strategy", value="THREAD"),
                    @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="1500")
            })
    public String myOrders(Model model) {
        logger.debug("Retrieve my order info from order-service");
        Collection<Order> orders = restTemplate
                .exchange(
                        "//order-service/myorders",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<Collection<Order>>() {
                        })
                .getBody();
        model.addAttribute("orders", orders);
        return "myorders";
    }

    public String myOrdersFallback(Model model) {
        model.addAttribute("orders", Collections.emptySet());
        model.addAttribute("orderServiceDown", true);
        return "myorders";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/myorders")
    @HystrixCommand(fallbackMethod = "placeOrderFallback",
            commandProperties = {
                    @HystrixProperty(name="execution.isolation.strategy", value="THREAD"),
                    @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="1500")
            })
    public String placeOrder(Model model, @ModelAttribute CustomerApplication.OrderForm orderForm) {
        restTemplate
                .postForObject("//order-service/myorders", orderForm.getOrder(), Void.class);
        return "redirect:.";
    }

    public String placeOrderFallback(Model model, CustomerApplication.OrderForm orderForm) {
        return myOrdersFallback(model);
    }

    public static class OrderForm {
        private LinkedHashMap<Long, Integer> order = new LinkedHashMap<Long, Integer>();

        public LinkedHashMap<Long, Integer> getOrder() {
            return order;
        }

        public void setOrder(LinkedHashMap<Long, Integer> order) {
            this.order = order;
        }
    }
}