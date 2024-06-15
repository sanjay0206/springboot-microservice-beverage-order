package com.infybuzz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class OrderBeverageId implements Serializable {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order;

    @Column(name = "beverage_id")
    private Long beverageId;

}
