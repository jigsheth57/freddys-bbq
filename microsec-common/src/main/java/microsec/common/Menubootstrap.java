package microsec.common;

import microsec.freddysbbq.menu.model.v1.MenuItem;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("menubootstrap")
public class Menubootstrap {
    private List<MenuItem> items;

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }
}
