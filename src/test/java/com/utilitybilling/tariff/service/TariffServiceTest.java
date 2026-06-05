package com.utilitybilling.tariff.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.enums.TariffType;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.tariff.dto.TariffRequest;
import com.utilitybilling.tariff.dto.TariffResponse;
import com.utilitybilling.tariff.dto.TariffTierRequest;
import com.utilitybilling.tariff.entity.Tariff;
import com.utilitybilling.tariff.repository.TariffRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TariffServiceTest {

    @Mock
    private TariffRepository tariffRepository;

    @InjectMocks
    private TariffService tariffService;

    @Test
    void createFlatTariffRejectsTierPayload() {
        TariffRequest request = new TariffRequest(
            MeterType.WATER,
            TariffType.FLAT,
            BigDecimal.valueOf(500),
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(18),
            BigDecimal.valueOf(5),
            LocalDate.of(2026, 7, 1),
            null,
            List.of(new TariffTierRequest(BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.valueOf(100)))
        );

        assertThrows(BadRequestException.class, () -> tariffService.createTariff(request));
    }

    @Test
    void createTariffAssignsNextVersion() {
        Tariff existing = new Tariff();
        existing.setVersion(2);
        when(tariffRepository.findTopByMeterTypeOrderByVersionDesc(MeterType.WATER))
            .thenReturn(Optional.of(existing));
        when(tariffRepository.save(any(Tariff.class))).thenAnswer(invocation -> {
            Tariff saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        TariffRequest request = new TariffRequest(
            MeterType.WATER,
            TariffType.FLAT,
            BigDecimal.valueOf(500),
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(18),
            BigDecimal.valueOf(5),
            LocalDate.of(2026, 7, 1),
            null,
            null
        );

        TariffResponse response = tariffService.createTariff(request);

        assertEquals(3, response.version());
    }

    @Test
    void createTieredTariffRequiresContinuousTiers() {
        TariffRequest request = new TariffRequest(
            MeterType.ELECTRICITY,
            TariffType.TIERED,
            null,
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(18),
            BigDecimal.valueOf(5),
            LocalDate.of(2026, 7, 1),
            null,
            List.of(
                new TariffTierRequest(BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(100)),
                new TariffTierRequest(BigDecimal.TEN, null, BigDecimal.valueOf(150))
            )
        );

        assertThrows(BadRequestException.class, () -> tariffService.createTariff(request));
    }
}
