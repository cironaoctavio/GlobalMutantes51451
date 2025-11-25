package com.mutantes.mutant_detector.service;

import com.mutantes.mutant_detector.exception.InvalidDnaException;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Service
public class MutantDetector {

    private static final int TAMANIO_MINIMO = 4;
    private static final int PARALELIZACION = 20;


    public boolean isMutant(String[] dna) {
        if (dna == null) {
            throw new InvalidDnaException("El array de ADN no puede ser nulo");
        }
        if (dna.length == 0) {
            throw new InvalidDnaException("El array de ADN no puede estar vacío");
        }

        int n = dna.length;

        // 2. Validación Estricta: Matriz NxN y Caracteres Válidos
        for (String row : dna) {
            if (row == null) {
                throw new InvalidDnaException("El ADN contiene filas nulas");
            }
            if (row.length() != n) {
                throw new InvalidDnaException("El ADN debe ser una matriz cuadrada (NxN)");
            }
            if (!row.matches("[ATCG]+")) {
                throw new InvalidDnaException("El ADN contiene caracteres inválidos (Solo se permite A, T, C, G)");
            }
        }
        // Convertimos la matriz de String a char[][] por eficiencia
        char[][] matrix = new char[n][];
        for (int i = 0; i < n; i++) {
            matrix[i] = dna[i].toCharArray();
        }

        // Selección de estrategia según tamaño
        if (n < PARALELIZACION) {
            return isMutantSequential(matrix);
        } else {
            return isMutantParallel(matrix);
        }
    }

    //ITERACION SECUENCIAL (para matrices chicas)
    private boolean isMutantSequential(char[][] matrix) {
        int n = matrix.length;
        int sequences = 0;

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {

                // Horizontal →
                if (col <= n - TAMANIO_MINIMO &&
                        checkHorizontal(matrix, row, col)) {
                    if (++sequences > 1) return true;
                }

                // Vertical ↓
                if (row <= n - TAMANIO_MINIMO &&
                        checkVertical(matrix, row, col)) {
                    if (++sequences > 1) return true;
                }

                // Diagonal ↘
                if (row <= n - TAMANIO_MINIMO &&
                        col <= n - TAMANIO_MINIMO &&
                        checkDiagonalDown(matrix, row, col)) {
                    if (++sequences > 1) return true;
                }

                // Diagonal ↗
                if (row >= TAMANIO_MINIMO - 1 &&
                        col <= n - TAMANIO_MINIMO &&
                        checkDiagonalUp(matrix, row, col)) {
                    if (++sequences > 1) return true;
                }
            }
        }

        return false;
    }

    //ITERACION PARALELA (para matrices grandes)
    private boolean isMutantParallel(char[][] matrix) {
        final int n = matrix.length;
        final AtomicInteger sequences = new AtomicInteger(0);

        return IntStream.range(0, n)
                .parallel()
                .anyMatch(row -> {

                    // Early termination global
                    if (sequences.get() > 1) return true;

                    for (int col = 0; col < n; col++) {

                        if (sequences.get() > 1) return true;

                        // Horizontal →
                        if (col <= n - TAMANIO_MINIMO &&
                                checkHorizontal(matrix, row, col)) {
                            if (sequences.incrementAndGet() > 1) return true;
                        }

                        // Vertical ↓
                        if (row <= n - TAMANIO_MINIMO &&
                                checkVertical(matrix, row, col)) {
                            if (sequences.incrementAndGet() > 1) return true;
                        }

                        // Diagonal ↘
                        if (row <= n - TAMANIO_MINIMO &&
                                col <= n - TAMANIO_MINIMO &&
                                checkDiagonalDown(matrix, row, col)) {
                            if (sequences.incrementAndGet() > 1) return true;
                        }

                        // Diagonal ↗
                        if (row >= TAMANIO_MINIMO - 1 &&
                                col <= n - TAMANIO_MINIMO &&
                                checkDiagonalUp(matrix, row, col)) {
                            if (sequences.incrementAndGet() > 1) return true;
                        }
                    }

                    return sequences.get() > 1;
                });
    }

    //VERIFICACIONES DE SECUENCIAS
    //Verifica secuencia horizontal
    private boolean checkHorizontal(char[][] m, int r, int c) {
        char base = m[r][c];
        return m[r][c + 1] == base &&
                m[r][c + 2] == base &&
                m[r][c + 3] == base;
    }

    //Verifica secuencia vertical
    private boolean checkVertical(char[][] m, int r, int c) {
        char base = m[r][c];
        return m[r + 1][c] == base &&
                m[r + 2][c] == base &&
                m[r + 3][c] == base;
    }

    //Verifica secuencia diagonal principal y paralelas
    private boolean checkDiagonalDown(char[][] m, int r, int c) {
        char base = m[r][c];
        return m[r + 1][c + 1] == base &&
                m[r + 2][c + 2] == base &&
                m[r + 3][c + 3] == base;
    }

    //Verifica secuencia diagonal secundaria y paralelas
    private boolean checkDiagonalUp(char[][] m, int r, int c) {
        char base = m[r][c];
        return m[r - 1][c + 1] == base &&
                m[r - 2][c + 2] == base &&
                m[r - 3][c + 3] == base;
    }


}
