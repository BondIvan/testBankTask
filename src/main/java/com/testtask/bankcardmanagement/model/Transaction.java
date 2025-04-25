package com.testtask.bankcardmanagement.model;

import com.testtask.bankcardmanagement.converter.TransactionTypeConverter;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "type", nullable = false)
    @Convert(converter = TransactionTypeConverter.class)
    private TransactionType type;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(name = "target_masked_card")
    private String targetMaskedCard;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Override
    public String toString() {
        return "Transaction{" +
                "amount=" + amount +
                ", type=" + type +
                ", card=" + card +
                ", targetMaskedCard='" + targetMaskedCard + '\'' +
                ", transactionDate=" + transactionDate +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(type, that.type)
                && Objects.equals(card, that.card)
                && Objects.equals(transactionDate, that.transactionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, card, transactionDate);
    }
}
