package com.smple.routes;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
public class KafkaProducerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:producer?period=5000")
                .setBody(simple("Message generated at ${date:now:yyyy-MM-dd HH:mm:ss}"))
                .to("kafka:messages?brokers={{kafka.brokers}}")
                .log("Sent message: ${body}");
    }
}