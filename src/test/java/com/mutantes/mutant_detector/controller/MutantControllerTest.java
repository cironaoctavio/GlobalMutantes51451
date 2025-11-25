package com.mutantes.mutant_detector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mutantes.mutant_detector.dto.DnaRequest;
import com.mutantes.mutant_detector.dto.StatsResponse;
import com.mutantes.mutant_detector.service.MutantService;
import com.mutantes.mutant_detector.service.StatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MutantController.class, properties = {"springdoc.api-docs.enabled=false"})
class MutantControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simula peticiones HTTP

    @MockBean
    private MutantService mutantService; // Mock del servicio

    @MockBean
    private StatsService statsService; // Mock de estadísticas

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos a JSON

    @Test
    @DisplayName("1. POST /mutant - Es Mutante -> Retorna 200 OK")
    void testCheckMutant_IsMutant_Returns200() throws Exception {
        when(mutantService.verifyAndSave(any())).thenReturn(true);

        String[] dna = {"ATGCGA", "CAGTGC", "TTATGT", "AGAAGG", "CCCCTA", "TCACTG"};
        DnaRequest request = new DnaRequest();
        request.setDna(dna);

        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. POST /mutant - Es Humano -> Retorna 403 Forbidden")
    void testCheckMutantIsHumanReturns403() throws Exception {
        // Mock: El servicio dice que NO es mutante
        when(mutantService.verifyAndSave(any())).thenReturn(false);

        String[] dna = {"ATGC", "CAGT", "TTAT", "AGAC"};
        DnaRequest request = new DnaRequest();
        request.setDna(dna);

        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // Esperamos 403
    }

    @Test
    @DisplayName("3. POST /mutant - ADN Nulo -> Retorna 400 Bad Request")
    void testCheckMutantNullDnaReturns400() throws Exception {
        DnaRequest request = new DnaRequest();
        request.setDna(null); // Inválido

        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("4. POST /mutant - ADN Vacío -> Retorna 400 Bad Request")
    void testCheckMutantEmptyDnaReturns400() throws Exception {
        DnaRequest request = new DnaRequest();
        request.setDna(new String[]{}); // Inválido

        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("5. POST /mutant - Caracteres Inválidos -> Retorna 400 Bad Request")
    void testCheckMutantInvalidCharsReturns400() throws Exception {
        String[] dna = {"ATGX", "CAGT", "TTAT", "AGAC"}; // 'X' no permitida
        DnaRequest request = new DnaRequest();
        request.setDna(dna);

        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6. POST /mutant - Matriz No Cuadrada (NxM) -> Retorna 400 Bad Request")
    void testCheckMutantNonSquareReturns400() throws Exception {
        String[] dna = {"ATG", "CAGT", "TTAT", "AGAC"}; // Primera fila tiene 3 letras, otras 4
        DnaRequest request = new DnaRequest();
        request.setDna(dna);

        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("7. POST /mutant - Números en ADN -> Retorna 400 Bad Request")
    void testCheckMutantNumbersReturns400() throws Exception {
        String[] dna = {"1234", "5678", "9012", "3456"}; // Números no permitidos
        DnaRequest request = new DnaRequest();
        request.setDna(dna);

        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("8. POST /mutant - Fila Nula -> Retorna 400 Bad Request")
    void testCheckMutantNullRowReturns400() throws Exception {
        String[] dna = {"AAAA", null, "AAAA", "AAAA"}; // Fila nula
        DnaRequest request = new DnaRequest();
        request.setDna(dna);

        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("9. POST /mutant - Body Vacío/Inexistente -> Retorna 400 Bad Request")
    void testCheckMutantMissingBodyReturns400() throws Exception {
        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)) // Sin contenido
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("10. POST /mutant - JSON Malformado -> Retorna 400 Bad Request")
    void testCheckMutantMalformedJsonReturns400() throws Exception {
        String badJson = "{ \"dna\": [\"AAAA\", }"; // JSON roto

        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("11. GET /mutant - Método No Permitido -> Retorna 405 Method Not Allowed")
    void testCheckMutantWrongMethodReturns405() throws Exception {
        mockMvc.perform(get("/mutant")) // Usando GET en lugar de POST
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("12. POST /mutant - Error Interno del Servicio -> Retorna 500 Internal Server Error")
    void testCheckMutantServiceExceptionReturns500() throws Exception {
        // Simulamos que el servicio explota por algo inesperado (ej. BD caída)
        when(mutantService.verifyAndSave(any())).thenThrow(new RuntimeException("Database connection failed"));

        String[] dna = {"AAAA", "CCCC", "TTTT", "GGGG"};
        DnaRequest request = new DnaRequest();
        request.setDna(dna);

        mockMvc.perform(post("/mutant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()) // 500
                .andExpect(jsonPath("$.error").value("Internal Server Error")); // Mensaje del GlobalExceptionHandler
    }

    @Test
    @DisplayName("13. GET /stats - Éxito -> Retorna 200 OK y JSON correcto")
    void testGetStatsSuccessReturns200() throws Exception {
        // Mock del servicio de stats
        StatsResponse statsMock = new StatsResponse(40L, 100L, 0.4);
        when(statsService.getStats()).thenReturn(statsMock);

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verificamos los campos exactos del JSON (snake_case)
                .andExpect(jsonPath("$.count_mutant_dna").value(40))
                .andExpect(jsonPath("$.count_human_dna").value(100))
                .andExpect(jsonPath("$.ratio").value(0.4));
    }

    @Test
    @DisplayName("14. POST /stats - Método No Permitido -> Retorna 405 Method Not Allowed")
    void testGetStatsWrongMethodReturns405() throws Exception {
        mockMvc.perform(post("/stats")) // POST a un endpoint GET
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("15. GET /stats - Error en Servicio -> Retorna 500")
    void testGetStatsServiceErrorReturns500() throws Exception {
        when(statsService.getStats()).thenThrow(new RuntimeException("Error calculating stats"));

        mockMvc.perform(get("/stats"))
                .andExpect(status().isInternalServerError());
    }
}
