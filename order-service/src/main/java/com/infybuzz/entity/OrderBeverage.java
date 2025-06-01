package com.infybuzz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "order_beverage")
public class OrderBeverage {

    @EmbeddedId
    private OrderBeverageId id;

    @Column(name = "quantity")
    private Integer quantity;
}
