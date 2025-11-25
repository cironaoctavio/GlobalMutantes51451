package com.mutantes.mutant_detector.service;

import com.mutantes.mutant_detector.dto.StatsResponse;
import com.mutantes.mutant_detector.repository.DnaRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private DnaRecordRepository repository;

    private StatsService statsService;
    @BeforeEach
    void setUp() {
        // Inyección manual: Tú mismo le pasas los mocks falsos
        statsService = new StatsService(repository);
    }

    @Test
    @DisplayName("1. Caso Normal: Mutantes y Humanos > 0 (Ratio decimal)")
    void testStatsNormalCase() {
        // Configuración: 40 mutantes, 100 humanos
        when(repository.countByIsMutant(true)).thenReturn(40L);
        when(repository.countByIsMutant(false)).thenReturn(100L);

        // Ejecución
        StatsResponse response = statsService.getStats();

        // Verificación
        // Ratio esperado: 40 / 100 = 0.4
        assertEquals(40L, response.getCountMutantDna());
        assertEquals(100L, response.getCountHumanDna());
        assertEquals(0.4, response.getRatio());
    }

    @Test
    @DisplayName("2. Caso Igualdad: Misma cantidad de ambos (Ratio 1.0)")
    void testStatsEqualCount() {
        // Configuración: 50 mutantes, 50 humanos
        when(repository.countByIsMutant(true)).thenReturn(50L);
        when(repository.countByIsMutant(false)).thenReturn(50L);

        StatsResponse response = statsService.getStats();

        // Ratio esperado: 50 / 50 = 1.0
        assertEquals(1.0, response.getRatio());
    }

    @Test
    @DisplayName("3. Caso Sin Mutantes: Ratio debe ser 0.0")
    void testStatsNoMutants() {
        // Configuración: 0 mutantes, 100 humanos
        when(repository.countByIsMutant(true)).thenReturn(0L);
        when(repository.countByIsMutant(false)).thenReturn(100L);

        StatsResponse response = statsService.getStats();

        // Ratio esperado: 0 / 100 = 0.0
        assertEquals(0, response.getCountMutantDna());
        assertEquals(0.0, response.getRatio());
    }

    @Test
    @DisplayName("4. Caso División por Cero: Sin Humanos (Regla del README)")
    void testStatsNoHumans() {
        // Configuración: 40 mutantes, 0 humanos
        // Según README: "40 mutantes, 0 humanos → ratio = 40.0"
        when(repository.countByIsMutant(true)).thenReturn(40L);
        when(repository.countByIsMutant(false)).thenReturn(0L);

        StatsResponse response = statsService.getStats();

        assertEquals(40L, response.getCountMutantDna());
        assertEquals(0L, response.getCountHumanDna());

        assertEquals(0.0, response.getRatio());
    }

    @Test
    @DisplayName("5. Caso Base de Datos Vacía: Todo en 0")
    void testStatsEmptyDatabase() {
        // Configuración: 0 mutantes, 0 humanos
        when(repository.countByIsMutant(true)).thenReturn(0L);
        when(repository.countByIsMutant(false)).thenReturn(0L);

        StatsResponse response = statsService.getStats();

        assertEquals(0L, response.getCountMutantDna());
        assertEquals(0L, response.getCountHumanDna());
        assertEquals(0.0, response.getRatio());
    }
}
