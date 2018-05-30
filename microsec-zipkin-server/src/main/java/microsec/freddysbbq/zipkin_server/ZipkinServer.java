package microsec.freddysbbq.zipkin_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import zipkin.server.EnableZipkinServer;

@SpringBootApplication
@EnableZipkinServer
@EnableDiscoveryClient
public class ZipkinServer {

    public static void main(String[] args) {
        SpringApplication.run(ZipkinServer.class, args);
    }
}
