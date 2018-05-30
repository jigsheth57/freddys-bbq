package microsec.common;

import microsec.freddysbbq.menu.model.v1.MenuItem;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("menuBootstrap")
public class MenuBootstrap {
    private List<MenuItem> items;

    public List<MenuItem> getItems() {
        return items;
    }

    public void setItems(List<MenuItem> items) {
        this.items = items;
    }
}
