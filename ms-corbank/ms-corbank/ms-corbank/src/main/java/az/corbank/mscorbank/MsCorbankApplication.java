package az.corbank.mscorbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsCorbankApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsCorbankApplication.class, args);
    }

}
