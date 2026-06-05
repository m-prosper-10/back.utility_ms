package com.utilitybilling.tariff.repository;

import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.enums.TariffStatus;
import com.utilitybilling.tariff.entity.Tariff;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TariffRepository extends JpaRepository<Tariff, Long> {

    Optional<Tariff> findTopByMeterTypeOrderByVersionDesc(MeterType meterType);

    List<Tariff> findByMeterTypeAndStatus(MeterType meterType, TariffStatus status);

    @Query("""
        SELECT t FROM Tariff t
        WHERE t.meterType = :meterType
        AND t.status = com.utilitybilling.common.enums.TariffStatus.ACTIVE
        AND t.effectiveFrom <= :billingDate
        AND (t.effectiveTo IS NULL OR t.effectiveTo >= :billingDate)
        ORDER BY t.version DESC
    """)
    List<Tariff> findApplicableTariffs(
        @Param("meterType") MeterType meterType,
        @Param("billingDate") LocalDate billingDate
    );
}
