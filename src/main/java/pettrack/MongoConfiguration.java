package pettrack;

import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfiguration {

    @Bean
    public MongoClient createConnection() {
        return new MongoClient("vb-ubuntu", 27017);
    }
}
