package com.freightflow.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("deveSanitizarErroDeIntegridade")
    void deveSanitizarErroDeIntegridade() {
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/v1/test");

        var detail = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate key value violates unique constraint uq_secret"),
                request
        );

        assertThat(detail.getDetail()).isEqualTo("A operação não pôde ser concluída porque os dados informados entram em conflito com um registro existente.");
        assertThat(detail.getDetail()).doesNotContain("constraint");
        assertThat(detail.getTitle()).isEqualTo("Conflict");
    }
}
