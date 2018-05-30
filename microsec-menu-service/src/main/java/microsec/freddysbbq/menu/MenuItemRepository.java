package microsec.freddysbbq.menu;

import microsec.freddysbbq.menu.model.v1.MenuItem;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MenuItemRepository extends PagingAndSortingRepository<MenuItem, Long> {
}