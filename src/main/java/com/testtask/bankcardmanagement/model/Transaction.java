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
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
@Entity
public class Transaction {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "type", nullable = false)
    @Convert(converter = TransactionTypeConverter.class)
    private TransactionType type;

    @ManyToOne
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    @ManyToOne
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", type=" + type +
                ", fromCardId='" + fromCard + '\'' +
                ", toCardId='" + toCard + '\'' +
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
                && Objects.equals(fromCard, that.fromCard)
                && Objects.equals(toCard, that.toCard)
                && Objects.equals(transactionDate, that.transactionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, fromCard, toCard, transactionDate);
    }
}
