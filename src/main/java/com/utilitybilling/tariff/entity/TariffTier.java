package com.utilitybilling.tariff.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tariff_tiers")
public class TariffTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minUnits;

    @Column(precision = 19, scale = 2)
    private BigDecimal maxUnits;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal ratePerUnit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;
}
