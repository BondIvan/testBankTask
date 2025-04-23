package com.testtask.bankcardmanagement.model;

import com.testtask.bankcardmanagement.converter.CardStatusConverter;
import com.testtask.bankcardmanagement.model.enums.CardStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cards")
@Entity
public class Card {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "encrypted_number", nullable = false)
    private String encryptedNumber;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User user;

    @Column(name = "status", nullable = false)
    @Convert(converter = CardStatusConverter.class)
    private CardStatus status;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Override
    public String toString() {
        return String.format("encryptedNumber: %s, expirationDate: %s, status: %s, balance: %.2f",
                encryptedNumber, expirationDate, status, balance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(encryptedNumber, card.encryptedNumber)
                && Objects.equals(expirationDate, card.expirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encryptedNumber, expirationDate);
    }
}
