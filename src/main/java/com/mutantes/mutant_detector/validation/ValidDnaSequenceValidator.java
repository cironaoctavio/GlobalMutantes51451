package com.mutantes.mutant_detector.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidDnaSequenceValidator implements ConstraintValidator<ValidDnaSequence, String[]> {

    @Override
    public boolean isValid(String[] dna, ConstraintValidatorContext context) {
        if (dna == null || dna.length == 0) return false;

        int n = dna.length;
        Pattern pattern = Pattern.compile("^[ATCG]+$");

        for (String row : dna) {
            if (row == null || row.length() != n) return false;
            if (!pattern.matcher(row).matches()) return false;
        }
        return true;
    }
}
