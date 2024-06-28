package com.smple.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

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
                .to("kafka:logs?brokers={{kafka.brokers}}")
                .setBody(constant("{\"status\": \"Log received and processed\"}"));
    }

}
