package com.boutouil;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
public class StockServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockServiceApplication.class, args);
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

@Log4j2(topic = "[Stock - Service]")
@Service
@RequiredArgsConstructor
class StockService {

    static List<String> stocks = Arrays.asList("ZOOM", "ORCL", "TSLA");
    static Random RAN = new Random();

    final ApplicationEventPublisher publisher;
    Map<String, StockEvent> lastTrade;

    static String randomStock() {
        return stocks.get(RAN.nextInt(stocks.size()));
    }

    static float newPrice(float oldPrice) {
        return oldPrice * (RAN.nextFloat() * 0.5F);
    }

    @PostConstruct
    public void init() {
        this.lastTrade = stocks.stream()
                .map(stock -> StockEvent.builder().stock(stock).price(RAN.nextInt() * 100.9F).build())
                .collect(Collectors.toMap(StockEvent::getStock, Function.identity()));
    }

    @Scheduled(fixedRate = 800L)
    @Transactional
    void marketMovement() {
        var lastTrade = this.lastTrade.get(randomStock());
        float newPrice = newPrice(lastTrade.getPrice());
        lastTrade.setPrice(newPrice);
        publisher.publishEvent(lastTrade);

    }
}

@Configuration
@RequiredArgsConstructor
@Log4j2(topic = "[Stock - Event Listener]")
class StockEventListener {

    final StreamBridge bridge;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onIncomingStocks(StockEvent event) {
        log.info("STOCK ==> {} NEW PRICE ==> {}", event.getStock(), event.getPrice());
        bridge.send("new-stock-out-0", event);
    }
}