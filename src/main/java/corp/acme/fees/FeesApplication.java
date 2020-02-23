package corp.acme.fees;

import corp.acme.common.domain.Classification;
import corp.acme.common.domain.Fee;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication
public class FeesApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeesApplication.class, args);
    }

}
