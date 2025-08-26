package com.testtask.bankcardmanagement.model.transaction;

import com.testtask.bankcardmanagement.model.Card;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("REPLENISHMENT")
public class ReplenishmentTransaction extends AbstractPaymentTransaction {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_card_id", nullable = false)
    private Card targetCard;
}
