package com.infybuzz.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmationEvent {
    private Long orderId;
    private String orderStatus;
}
