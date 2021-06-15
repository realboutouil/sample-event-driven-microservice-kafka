package com.boutouil;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@SpringBootApplication
public class MarketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketServiceApplication.class, args);
    }
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class StockEvent {
    private String stock;

    private float price;
}

@Log4j2
@Component
@RequiredArgsConstructor
class StockMovementConsumers {

    @Bean
    public Consumer<Message<StockEvent>> newStock() {
        return message -> {
            var event = message.getPayload();
            log.info("RECEIVED STOCK ==> {} NEW PRICE ==> {}", event.getStock(), event.getPrice());
        };
    }
}