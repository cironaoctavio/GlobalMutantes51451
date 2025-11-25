package com.mutantes.mutant_detector.service;

import com.mutantes.mutant_detector.entity.DnaRecord;
import com.mutantes.mutant_detector.exception.InvalidDnaException;
import com.mutantes.mutant_detector.repository.DnaRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class MutantServiceTest {

    @Mock
    private DnaRecordRepository repository; // Mock de la base de datos

    @Mock
    private MutantDetector mutantDetector; // Mock del algoritmo
    private MutantService mutantService;
    @BeforeEach
    void setUp() {
        // Inyección manual: Tú mismo le pasas los mocks falsos
        mutantService = new MutantService(mutantDetector, repository);
    }

    @Test
    @DisplayName("1. Si es Mutante y NO está en BD -> Guarda TRUE y retorna TRUE")
    void testAnalyzeNewMutant() {
        // DATOS
        String[] dna = {"ATGCGA", "CAGTGC", "TTATGT", "AGAAGG", "CCCCTA", "TCACTG"};

        // SIMULACIÓN (Mocks)
        // 1. Cuando busque en BD, retorna vacío (no existe)
        when(repository.findByDnaHash(anyString())).thenReturn(Optional.empty());
        // 2. Cuando analice el ADN, dice que ES mutante
        when(mutantDetector.isMutant(dna)).thenReturn(true);

        // EJECUCIÓN
        boolean result = mutantService.verifyAndSave(dna);

        // VERIFICACIÓN
        assertTrue(result, "Debe retornar true si es mutante");

        // Verifica que se guardó en BD con isMutant = true
        // Usamos any(DnaRecord.class) para evitar errores con el objeto exacto
        verify(repository).save(argThat(record -> record.isMutant()));
    }

    @Test
    @DisplayName("2. Si es Humano y NO está en BD -> Guarda FALSE y retorna FALSE")
    void testAnalyzeNewHuman() {
        String[] dna = {"ATGC", "CAGT", "TTAT", "AGAC"};

        // No existe en BD, Detector dice false
        when(repository.findByDnaHash(anyString())).thenReturn(Optional.empty());
        when(mutantDetector.isMutant(dna)).thenReturn(false);

        boolean result = mutantService.verifyAndSave(dna);

        assertFalse(result, "Debe retornar false si es humano");

        // Verifica que se guardó en BD con isMutant = false
        verify(repository).save(argThat(record -> !record.isMutant()));
    }

    @Test
    @DisplayName("3. Si ya existe como Mutante en BD -> Retorna TRUE directo (sin analizar)")
    void testAnalyzeExistingMutant() {
        String[] dna = {"AAAA", "CCCC", "TCAG", "GGTC"};

        // Simulamos que YA EXISTE un registro en la base de datos
        DnaRecord existingRecord = new DnaRecord();
        existingRecord.setMutant(true); // Usamos setter por si el Builder falla

        when(repository.findByDnaHash(anyString())).thenReturn(Optional.of(existingRecord));

        boolean result = mutantService.verifyAndSave(dna);

        assertTrue(result);

        // CRÍTICO: Verificar que NO se llamó al algoritmo pesado (ahorro de recursos)
        verify(mutantDetector, never()).isMutant(any());
        // CRÍTICO: Verificar que NO se intentó guardar de nuevo
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("4. Si ya existe como Humano en BD -> Retorna FALSE directo")
    void testAnalyzeExistingHuman() {
        String[] dna = {"ATGC", "CAGT", "TTAT", "AGAC"};

        DnaRecord existingRecord = new DnaRecord();
        existingRecord.setMutant(false);

        when(repository.findByDnaHash(anyString())).thenReturn(Optional.of(existingRecord));

        boolean result = mutantService.verifyAndSave(dna);

        assertFalse(result);
        verify(mutantDetector, never()).isMutant(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("5. Si el Detector lanza InvalidDnaException -> NO guarda nada y propaga error")
    void testDetectorThrowsException() {
        String[] dna = {null}; // ADN inválido

        // No existe en BD
        when(repository.findByDnaHash(anyString())).thenReturn(Optional.empty());

        // El detector explota con nuestra excepción personalizada
        when(mutantDetector.isMutant(dna)).thenThrow(new InvalidDnaException("ADN Nulo"));

        // Ejecutamos y esperamos la excepción
        assertThrows(InvalidDnaException.class, () -> mutantService.verifyAndSave(dna));

        // Aseguramos que NO se guardó basura en la BD
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("6. Si falla la conexión a BD al buscar -> Lanza RuntimeException")
    void testRepositoryFindFails() {
        String[] dna = {"AAAA"};

        // Simulamos error de conexión DB
        when(repository.findByDnaHash(any())).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> mutantService.verifyAndSave(dna));

        // No debió ni intentar analizar
        verify(mutantDetector, never()).isMutant(any());
    }

    @Test
    @DisplayName("7. Si falla el guardado en BD -> Lanza DataIntegrityViolationException")
    void testRepositorySaveFails() {
        String[] dna = {"AAAA"};

        when(repository.findByDnaHash(anyString())).thenReturn(Optional.empty());
        when(mutantDetector.isMutant(dna)).thenReturn(true);

        // Simulamos error al guardar (ej. disco lleno, constraint violation)
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThrows(DataIntegrityViolationException.class, () -> mutantService.verifyAndSave(dna));
    }

    @Test
    @DisplayName("8. Diferentes ADNs deben generar búsquedas distintas")
    void testDifferentDnaHashes() {
        // Este test verifica indirectamente que el hash se calcula
        String[] dna1 = {"AAAA"};
        String[] dna2 = {"CCCC"};

        // Configuramos mocks relajados
        when(repository.findByDnaHash(anyString())).thenReturn(Optional.empty());
        when(mutantDetector.isMutant(any())).thenReturn(true);

        mutantService.verifyAndSave(dna1);
        mutantService.verifyAndSave(dna2);

        // Se debe haber llamado al repositorio 2 veces
        verify(repository, times(2)).findByDnaHash(anyString());
        // Se debe haber llamado al detector 2 veces
        verify(mutantDetector, times(2)).isMutant(any());
    }

    @Test
    @DisplayName("9. Verificar que array vacío llega al detector (y este validará)")
    void testEmptyArrayProcessing() {
        String[] dna = {};

        when(repository.findByDnaHash(anyString())).thenReturn(Optional.empty());
        // Simulamos que el detector hace su trabajo de lanzar la excepción por array vacío
        when(mutantDetector.isMutant(dna)).thenThrow(new InvalidDnaException("Empty"));

        assertThrows(InvalidDnaException.class, () -> mutantService.verifyAndSave(dna));

        // Lo importante es que el servicio intentó buscar el hash antes de fallar
        verify(repository).findByDnaHash(anyString());
    }
}