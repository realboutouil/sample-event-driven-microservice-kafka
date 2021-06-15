# Event Driven Architecture Using Spring Cloud Stream/Function - Apache Kafka

__[Event-driven architecture](https://en.wikipedia.org/wiki/Event-driven_architecture)__ is a software architecture
paradigm promoting the production, detection, consumption of, and reaction to events. It was created to help developers
have a decoupled and responsive application. Because of this, it has been widely used in applications that have been
broken down from monoliths to microservices.

__[Spring Cloud Stream](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/#_quick_start)__ improves
your productivity when working with Apache Kafka, RabbitMQ, Azure Event Hub, and more, providing three key abstractions
to simplify your code.

__[Spring Cloud Function](https://spring.io/projects/spring-cloud-function)__ enables you to write functions once and
run them anywhere (AWS, Azure, etc.), while continuing to use all the familiar and comprehensive Spring APIs. You can
chain multiple functions together to create new capabilities

The main goal of this __[repository](https://github.com/junbetterway/spring-cloud-func-kafka-stream-binder-basic)__ is
to demonstrate how to create an event driven system which is simplified by using __Spring Cloud Stream/Function__. In
particular, we will be
using __[Spring Cloud Stream for Apache Kafka Binder](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.0.10.RELEASE/reference/html/spring-cloud-stream-binder-kafka.html#_apache_kafka_binder)__
. Please visit my previous __[tutorial](https://github.com/junbetterway/spring-kafka-basic)__ to learn the basic of
producer-consumer using Apache Kafka with Spring.

## Run the needed dependencies: Apache Kafka, Zookeeper and (optional) Kafdrop using Docker

*__Note:__ We will be using __[Kafdrop](https://github.com/obsidiandynamics/kafdrop)__ which is a Web UI for viewing
Kafka topics and browsing consumer groups. The tool displays information such as brokers, topics, partitions, consumers,
and lets you view messages.*

1. Make sure to install **[Docker](https://docs.docker.com/get-docker/)** on your machine
2. Go to the root directory of the project where __docker-compose.yml__ is located.
3. Run the docker compose by

```
docker compose -f kafka-docker-compose.yml up -d
```

*__Note:__ Make sure no errors are present on the logs such connection refused etc. Now go to your browser and
access __http://localhost:9000/__ to see an overview of your Kafka cluster, brokers and topics*

## Spring Cloud Stream for Apache Kafka Binder

To
use __[Apache Kafka binder](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.1/reference/html/spring-cloud-stream-binder-kafka.html#_apache_kafka_binder)__
, we need to add below dependency to our application:

```
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-stream-binder-kafka</artifactId>
</dependency>
```

The Apache Kafka Binder implementation maps each destination to an Apache Kafka topic. The consumer group maps directly
to the same Apache Kafka concept. Partitioning also maps directly to Apache Kafka partitions as well.

If you are developing on your local setup, there will be no additional steps to setup
the __[Kafka Configuration Options](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.1/reference/html/spring-cloud-stream-binder-kafka.html#_configuration_options)__
as long as your Apache Kafka properties are the defaults used by Spring Cloud Streams for Apache Kafka Binder.

*__Note:__ The __docker-compose.yml__ ensures that the default configurations used by Spring Cloud Stream for Apache
Kafka Binder are already configured such as port 9092 etc.*

## Spring Cloud Functions

Our main class exposes three bean definitions:

```
@SpringBootApplication
@Log4j2
public class SpringcloudfuncApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringcloudfuncApplication.class, args); 
	}
}

@Configuration
@RequiredArgsConstructor
class StockEventConsumers {

    @Bean
    public Consumer<Message<StockEvent>> newStock() {
        return (o) -> {
            var event = o.getPayload();
            log.info("Received Stock ====> {} , PRICE ====> {}", event.getStock(), event.getPrice());
        };
    }
}
```

Hence, if we run the application - Spring must be able to identify what functions we intend to run and how do we want
them to be composed of. For this tutorial, we will have the following configuration on our __application.yml__ file:

```
spring:
  cloud:
    stream:
      kafka:
      function:
        definition: newStock;
      bindings:
        newStock-in-0:
          destination: new-stock-topic
        new-stock-out-0:
          destination: new-stock-topic
```

Based from this, we have two callable functions:

1. The __newStock__ function which accepts a String message from Kafka topic __newStock-in-0__ and outputs the result to
   Kafka topic __new-stock-out-0__.

*__Note:__ The Kafka topics I have mentioned above will all be generated automatically by Spring during startup. It uses
the function name with in-out tagging during discovery.*

## Run the Spring Boot Application Using Spring Tool Suite (STS)

1. Download STS version 3.4.* (or better) from the [Spring website](https://spring.io/tools). STS is a free Eclipse
   bundle with many features useful for Spring developers.
2. Right-click on the project, or the main application class then select "Run As" > "Spring Boot App"

## Testing Events - Produce/Consume

We will now test our application by attempting to produce messages on each topic and check the results via __Kafdrop__
Web or via server console (since we enabled logging).

*__Note:__ We will use the docker interactive shell by connecting to the container running the Kafka image.*

### A. Create Account Then New Stock Function Composition

1. Run __docker ps__ command and take note of the container ID running the Apache Kafka image
2. Run below command to connect to docker interactive shell.

```
winpty docker exec -it kafka-broker bash
```

I have used __winpty__ above since my local setup is running on Windows. If you are running on Linux or MacOS then
remove the __winpty__ and just run below command:

```
docker exec -it kafka-broker bash
```

3. Create/produce a JSON message to the target topic __newStock-in-0__

```
kafka-console-producer --broker-list localhost:19092 --topic new-stock-topic --property value.serializer=custom.class.serialization.JsonSe
rializer
```

4. On the line > Enter your desired JSON message (__e.g.,__  { "stock": "ZOOM", "price": 12000} )
5. Check your console, one should see something like this:

```
2021-06-15 01:01:37.176  INFO 10820 --- [container-0-C-1] c.boutouil.consumer.StockEventConsumers  : Received Stock ====> ZOOM , PRICE ====> 12000.0
```

6. Or you can go to Kafdrop URL (http://localhost:9000/) and click on the topic __new-stock-topic__ > __
   View Messages__ then on the filter section click __View Messages__. One should see something similar to below:

```
Offset: 203   Key: empty   Timestamp: 2021-06-15 00:01:36.170 Headers: empty
{
   "stock": "ZOOM",
   "price": 12000
}
```

## Powered By

Contact me at [Mohammed Amine BOUTOUIL](mailto:boutouilmohammedamine@gmail.com)

Happy coding!!!