package com.smple.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.model.rest.RestBindingMode;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogReceiverRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        restConfiguration().bindingMode(RestBindingMode.json)
                .component("platform-http")
                .port(8080);

        rest("/logs")
                .post()
                .consumes("application/json")
                .produces("application/json")
                .to("direct:processLog");

        from("direct:processLog")
                .log("Received log: ${body}")
                .setHeader(KafkaConstants.KEY, method(UUID.class, "randomUUID"))
                .to("kafka:logs?brokers={{kafka.brokers}}")
                .setBody(simple("{\"key\": \"${header.kafka.KEY}\", \"status\": \"Log received and processed\"}"));
        }
}
