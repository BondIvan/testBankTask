package com.testtask.bankcardmanagement.model;

import com.testtask.bankcardmanagement.converter.TransactionDirectionConverter;
import com.testtask.bankcardmanagement.converter.TransactionTypeConverter;
import com.testtask.bankcardmanagement.model.enums.TransactionDirection;
import com.testtask.bankcardmanagement.model.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_transactions_group_source_card_direction",
                        columnNames = {"transfer_group_id", "source_card_id", "direction"}
                )
        }
)
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

    @Convert(converter = TransactionDirectionConverter.class)
    @Column(name = "direction", nullable = false)
    private TransactionDirection direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_card_id", nullable = false)
    private Card sourceCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_card_id")
    private Card targetCard;

    @Column(name = "transfer_group_id", columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID transferGroupId;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(3)")
    private Instant createdAt;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", type=" + type +
                ", direction=" + direction +
                ", sourceCardId=" + (sourceCard != null ? sourceCard.getId() : null) +
                ", targetCardId=" + (targetCard != null ? targetCard.getId() : null) +
                ", transferGroupId=" + transferGroupId +
                ", createdAt=" + createdAt +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(direction, that.direction)
                && Objects.equals(getSourceCardId(), that.getSourceCardId())
                && Objects.equals(transferGroupId, that.transferGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction, getSourceCardId(), transferGroupId);
    }

    private Long getSourceCardId() {
        return (sourceCard != null) ? sourceCard.getId() : null;
    }
}
