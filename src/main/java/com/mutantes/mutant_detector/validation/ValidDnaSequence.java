package com.mutantes.mutant_detector.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;
//SOLO PARA CAMPOS DTO
@Target({ElementType.FIELD})
//Para ser detectado en tiempo de ejecucion
@Retention(RetentionPolicy.RUNTIME)
//VINCULA A ValidDnaSequenceValidator
@Constraint(validatedBy = ValidDnaSequenceValidator.class)
@Documented
public @interface ValidDnaSequence {
    String message() default "Secuencia de ADN inválida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
