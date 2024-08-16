package com.infybuzz.listener;

import brave.Tracer;
import com.infybuzz.event.OrderConfirmationEvent;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderConfirmationEventListener {
    Logger logger = LoggerFactory.getLogger(OrderConfirmationEventListener.class);
    private final ObservationRegistry observationRegistry;
    private final Tracer tracer;

    @KafkaListener(topics = "orderConfirmationTopic", groupId = "orderConfirmationId")
    public void handleNotification(OrderConfirmationEvent orderConfirmationEvent) {
        Observation.createNotStarted("on-message", this.observationRegistry).observe(() -> {
            logger.info("Got message <{}>", orderConfirmationEvent);

            logger.info("TraceId- {}, Received Notification for Order Confirmation with OrderId - {}, OrderStatus - {}",
                    this.tracer.currentSpan().context().traceId(),
                    orderConfirmationEvent.getOrderId(),
                    orderConfirmationEvent.getOrderStatus());

            // send out an email notification
        });
    }
}
