package com.mutantes.mutant_detector.service;

import com.mutantes.mutant_detector.exception.InvalidDnaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MutantDetectorTest {

    private final MutantDetector mutantDetector = new MutantDetector();

    @Test
    @DisplayName("1. Mutante con 2 Secuencias Horizontales")
    void testMutantTwoHorizontal() {
        String[] dna = {
                "AAAA",
                "CCCC",
                "TCAG",
                "GGTC"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("2. Mutante con 2 Secuencias Verticales")
    void testMutantTwoVertical() {
        String[] dna = {
                "ATCG",
                "ATCG",
                "ATCG",
                "ATCG"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("3. Mutante con 2 Secuencias en Diagonal Principal (Descendentes)")
    void testMutantTwoMainDiagonals() {
        String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("4. Mutante con 2 Secuencias en Diagonal Secundaria (Ascendentes)")
    void testMutantTwoSecondaryDiagonals() {
        String[] dna = {
                "GTGCAT",
                "CAGTTC",
                "TTATGT",
                "AGTATG",
                "CTCTTA",
                "TCTCTG"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("5. Mutante con 1 Horizontal y 1 Vertical")
    void testMutantHorizontalAndVertical() {
        String[] dna = {
                "AAAACG",   // Horizontal: AAAA
                "TCTGCA",
                "TCTGCT",
                "TCTGCC",
                "GAGTCA",
                "CAGTGC"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("6. Mutante con 1 Horizontal y 1 Diagonal Principal")
    void testMutantHorizontalAndMainDiagonal() {
        String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("7. Mutante con 1 Horizontal y 1 Diagonal Secundaria")
    void testMutantHorizontalAndSecondaryDiagonal() {
        String[] dna = {
                "ATGCCG",
                "CAGTGC",
                "TAGGTA",
                "GGGGAA",
                "TCACTA",
                "TCACTG"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("8. Mutante con 1 Vertical y 1 Diagonal Principal")
    void testMutantVerticalAndMainDiagonal() {
        String[] dna = {
                "ACGT",
                "AACG",
                "ATAG",
                "ATTA",
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("9. Mutante con 1 Vertical y 1 Diagonal Secundaria")
    void testMutantVerticalAndSecondaryDiagonal() {
        String[] dna = {
                "AGCT",
                "TGTT",
                "ATGT",
                "TATT",
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("10. Mutante con 1 Diagonal Principal y 1 Diagonal Secundaria (Forma X)")
    void testMutantBothDiagonalsXShape() {
        String[] dna = {
                "ATGAA",
                "CAGAA",
                "TCAAC",
                "AATAA",
                "AGTCA"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("11. Mutante con 3 Secuencias Horizontales")
    void testMutantTripleHorizontal() {
        String[] dna = {
                "AAAA",
                "CCCC",
                "GGGG",
                "TTTT"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("12. Mutante Triple (Horizontal + Vertical + Diagonal)")
    void testMutantTripleMixed() {
        String[] dna = {
                "AAAAGA",
                "AAGTGC",
                "ATATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("13. Mutante en Matriz Mínima (4x4) llena")
    void testMutant4x4Full() {
        String[] dna = {
                "AAAA",
                "AAAA",
                "AAAA",
                "AAAA"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("14. Mutante en Matriz Grande (10x10) dispersa")
    void testMutantLargeScattered() {
        String[] dna = {
                "ATGCGTACGA",
                "CAGTGCTAGC",
                "TTATGTTAGT",
                "AGAAAAGCTA",
                "CCCCTAGGTA",
                "TCACTGACCA",
                "GGTACCGTAA",
                "GTAAGGCCAA",
                "GCGGTTAAGG",
                "GAGGTTCCGG"
        };
        assertTrue(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("15. Humano: ADN sin ninguna secuencia")
    void testHumanNoSequences() {
        String[] dna = {
                "ATGC",
                "CAGT",
                "TTAT",
                "AGAC"
        };
        assertFalse(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("16. Humano: ADN con EXACTAMENTE UNA secuencia (Horizontal)")
    void testHumanOneSequenceHorizontal() {
        String[] dna = {
                "AAAA", // ← 1 secuencia aquí
                "CAGT",
                "TTAT",
                "AGAC"
        };
        assertFalse(mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("17. Humano: ADN con EXACTAMENTE UNA secuencia (Vertical)")
    void testHumanOneSequenceVertical() {
        String[] dna = {
                "ATGC",
                "ACGC",
                "ATTC",
                "ATGA"
        };
        assertFalse(mutantDetector.isMutant(dna));
    }


    @Test
    @DisplayName("18. Inválido: Matriz no cuadrada (NxM)")
    void testInvalidRectangleMoreCols() {
        String[] dna = {
                "ATGCG",
                "CAGTG",
                "TTATG",
                "AGAAG"
        };
        // Ahora esperamos que lance excepción
        assertThrows(InvalidDnaException.class, () -> {
            mutantDetector.isMutant(dna);
        });
    }

    @Test
    @DisplayName("19. Inválido: Matriz irregular")
    void testInvalidRaggedArray() {
        String[] dna = {
                "ATGC",
                "CAGT",
                "TTA",
                "AGAAG"
        };
        assertThrows(InvalidDnaException.class, () -> mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("20. Inválido: Null DNA")
    void testNullDna() {
        assertThrows(InvalidDnaException.class, () -> mutantDetector.isMutant(null));
    }

    @Test
    @DisplayName("21. Inválido: Letras prohibidas")
    void testInvalidCharX() {
        String[] dna = {
                "ATGC",
                "CAGT",
                "TTXT",
                "AGAC"
        };
        assertThrows(InvalidDnaException.class, () -> mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("22. Inválido: Números")
    void testInvalidNumbers() {
        String[] dna = {
                "ATGC",
                "C1GT",
                "TTAT",
                "AGAC"
        };
        assertThrows(InvalidDnaException.class, () -> mutantDetector.isMutant(dna));
    }

    @Test
    @DisplayName("23. Inválido: Contiene minúsculas")
    void testInvalidLowerCase() {
        String[] dna = {
                "ATGC",
                "cagt",
                "TTAT",
                "AGAC"
        };
        assertThrows(InvalidDnaException.class, () -> mutantDetector.isMutant(dna));
    }
}
