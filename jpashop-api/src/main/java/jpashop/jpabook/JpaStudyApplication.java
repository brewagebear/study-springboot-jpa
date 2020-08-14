package jpashop.jpabook;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpaStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaStudyApplication.class, args);
    }

    @Bean
    Hibernate5Module hibernate5Module() {
        //hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
        return new Hibernate5Module();
    }
}
