package pettrack;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfiguration {

    @Value("${database.url}")
    private String db;

    @Bean
    public MongoClient createConnection() {
        return new MongoClient(db, 27017);
    }
}
