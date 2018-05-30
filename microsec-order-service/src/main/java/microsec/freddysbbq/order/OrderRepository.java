package microsec.freddysbbq.order;

import microsec.freddysbbq.order.model.v1.Order;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {
    Collection<Order> findByCustomerId(@Param("customerId") String customerId);
}