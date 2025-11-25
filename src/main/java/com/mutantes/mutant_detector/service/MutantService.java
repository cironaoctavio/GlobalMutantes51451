package com.mutantes.mutant_detector.service;

import com.mutantes.mutant_detector.entity.DnaRecord;
import com.mutantes.mutant_detector.exception.DnaHashCalculationException;
import com.mutantes.mutant_detector.repository.DnaRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MutantService {
    private final MutantDetector mutantDetector;
    private final DnaRecordRepository dnaRecordRepository;

    public boolean verifyAndSave(String[] dna) {
        //Calcular Hash para deduplicación
        String hash = calculateHash(dna);

        //Verificar si ya existe en BD (Caché)
        Optional<DnaRecord> existing = dnaRecordRepository.findByDnaHash(hash);
        if (existing.isPresent()) {
            return existing.get().isMutant();
        }

        //Si no existe, analizar
        boolean isMutant = mutantDetector.isMutant(dna);

        //Guardar resultado
        DnaRecord record = DnaRecord.builder()
                .dnaHash(hash)
                .isMutant(isMutant)
                .build();
        dnaRecordRepository.save(record);

        return isMutant;
    }

    private String calculateHash(String[] dna) {
        try {
            String raw = String.join("", dna);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            //Excepción personalizada
            throw new DnaHashCalculationException("Error calculando el hash del ADN", e);
        }
    }
}
