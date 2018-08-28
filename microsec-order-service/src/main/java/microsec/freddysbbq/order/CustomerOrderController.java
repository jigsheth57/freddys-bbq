package microsec.freddysbbq.order;

import microsec.common.TokenRelayingRestTemplate;
import microsec.freddysbbq.menu.model.v1.MenuItem;
import microsec.freddysbbq.order.model.v1.Order;
import microsec.freddysbbq.order.model.v1.OrderItem;
import microsec.uaa.model.v2.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import java.util.Map.Entry;

/**
 * An Order controller for customer use
 * 
 * @author will.tran
 *
 */
@RestController
public class CustomerOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    @Qualifier("loadBalancedTokenRelayingRestTemplate")
    private OAuth2RestTemplate loadBalancedTokenRelayingRestTemplate;

    private OAuth2RestTemplate tokenRelayingRestTemplate = new TokenRelayingRestTemplate();

    @Autowired
    private ResourceServerProperties resourceServerProperties;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PreAuthorize("#oauth2.hasScope('order.me')")
    @RequestMapping("/myorders")
    public Collection<Order> getMyOrders(Principal principal) {
        return orderRepository.findByCustomerId(principal.getName());
    }

    @PreAuthorize("#oauth2.hasScope('order.me')")
    @RequestMapping(value = "/myorders", method = RequestMethod.POST)
    public ResponseEntity<Void> placeOrder(Principal principal, @RequestBody Map<Long, Integer> itemsAndQuantites) {
        if (itemsAndQuantites.isEmpty()) {
            return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
        }
        UserInfo userInfo = tokenRelayingRestTemplate
                .getForObject(resourceServerProperties.getUserInfoUri(), UserInfo.class);
        logger.info("placeOrder: "+userInfo);
        Order order = new Order();
        order.setCustomerId(principal.getName());
        order.setEmail(userInfo.getEmail());
        order.setFirstName(userInfo.getGivenName());
        order.setLastName(userInfo.getFamilyName());
        order.setPlaced(new Date());
        Set<OrderItem> items = new LinkedHashSet<>();
        BigDecimal total = new BigDecimal(0);
        for (Entry<Long, Integer> itemAndQuantity : itemsAndQuantites.entrySet()) {
            Long itemId = itemAndQuantity.getKey();
            Integer quantity = itemAndQuantity.getValue();
            if (itemId == null || quantity != null && quantity < 0) {
                return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
            } else if (quantity == null || quantity == 0) {
                continue;
            }
            try {
                MenuItem item = loadBalancedTokenRelayingRestTemplate
                        .getForObject("//menu-service/menuItems/{id}", MenuItem.class, itemId);
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setMenuItemId(itemId);
                orderItem.setName(item.getName());
                orderItem.setPrice(item.getPrice());
                orderItem.setQuantity(quantity);
                items.add(orderItem);
                total = total.add(item.getPrice().multiply(new BigDecimal(quantity)));
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
                }
            }
        }
        order.setTotal(total);
        order.setOrderItems(items);
        orderRepository.save(order);
        return new ResponseEntity<Void>(HttpStatus.CREATED);
    }

    public void setLoadBalancedTokenRelayingRestTemplate(OAuth2RestTemplate oAuth2RestTemplate) {
        this.loadBalancedTokenRelayingRestTemplate = oAuth2RestTemplate;
    }

    public void setTokenRelayingRestTemplate(OAuth2RestTemplate userInfoRestTemplate) {
        this.tokenRelayingRestTemplate = userInfoRestTemplate;
    }

}