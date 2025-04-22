package com.testtask.bankcardmanagement.model;

import com.testtask.bankcardmanagement.converter.LimitTypeConverter;
import com.testtask.bankcardmanagement.model.enums.LimitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "limits")
@Entity
public class Limit {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "limit_type")
    @Convert(converter = LimitTypeConverter.class)
    private LimitType limitType;

    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Override
    public String toString() {
        return "Limit{" +
                "id=" + id +
                ", cardId='" + card + '\'' +
                ", limitType=" + limitType +
                ", maxAmount=" + maxAmount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Limit limit = (Limit) o;
        return Objects.equals(card, limit.card) && limitType == limit.limitType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(card, limitType);
    }
}
