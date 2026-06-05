package com.utilitybilling.reading.entity;

import com.utilitybilling.common.entity.AuditableEntity;
import com.utilitybilling.meter.entity.Meter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "meter_readings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"meter_id", "billing_month", "billing_year"})
)
public class MeterReading extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal previousReading;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentReading;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal consumption;

    @Column(nullable = false)
    private LocalDate readingDate;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    @ManyToOne(optional = false)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;
}
