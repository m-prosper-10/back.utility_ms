package com.utilitybilling.tariff.entity;

import com.utilitybilling.common.entity.AuditableEntity;
import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.enums.TariffStatus;
import com.utilitybilling.common.enums.TariffType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tariffs")
public class Tariff extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeterType meterType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TariffType tariffType;

    @Column(nullable = false)
    private Integer version;

    @Column(precision = 19, scale = 2)
    private BigDecimal ratePerUnit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fixedCharge;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal vatPercentage;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal penaltyPercentage;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TariffStatus status = TariffStatus.ACTIVE;

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TariffTier> tiers = new ArrayList<>();
}
