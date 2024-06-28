package com.smple.routes;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
public class KafkaConsumerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kafka:logs?brokers={{kafka.brokers}}")
                .log("Received message: ${body}")
                .to("log:received-messages");
    }
}