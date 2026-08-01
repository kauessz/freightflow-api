package com.freightflow.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

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

    @Test
    @DisplayName("deveRetornarBadRequestSeguroParaParametroInvalido")
    void deveRetornarBadRequestSeguroParaParametroInvalido() throws NoSuchMethodException {
        WebRequest request = mock(WebRequest.class);
        Method method = SampleController.class.getDeclaredMethod("sample", SampleEnum.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        var detail = handler.handleMethodArgumentTypeMismatch(
                new MethodArgumentTypeMismatchException("NOT_A_TYPE", SampleEnum.class, "valueType", parameter, null),
                request
        );

        assertThat(detail.getStatus()).isEqualTo(400);
        assertThat(detail.getTitle()).isEqualTo("Bad Request");
        assertThat(detail.getDetail()).isEqualTo("Invalid value for parameter 'valueType'.");
        assertThat(detail.getDetail()).doesNotContain("SampleEnum");
    }

    @Test
    @DisplayName("deveRetornarMethodNotAllowedSeguro")
    void deveRetornarMethodNotAllowedSeguro() {
        WebRequest request = mock(WebRequest.class);

        var detail = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException(HttpMethod.POST.name()),
                request
        );

        assertThat(detail.getStatus()).isEqualTo(405);
        assertThat(detail.getTitle()).isEqualTo("Method Not Allowed");
        assertThat(detail.getDetail()).isEqualTo("The requested HTTP method is not available for this endpoint.");
    }

    @Test
    @DisplayName("deveManterErroInesperadoComo500Seguro")
    void deveManterErroInesperadoComo500Seguro() {
        WebRequest request = mock(WebRequest.class);

        var detail = handler.handleGenericException(new RuntimeException("sensitive details"), request);

        assertThat(detail.getStatus()).isEqualTo(500);
        assertThat(detail.getTitle()).isEqualTo("Internal Server Error");
        assertThat(detail.getDetail()).isEqualTo("An unexpected error occurred");
        assertThat(detail.getDetail()).doesNotContain("sensitive");
    }

    private enum SampleEnum {
        VALUE
    }

    private static final class SampleController {
        @SuppressWarnings("unused")
        void sample(SampleEnum valueType) {
        }
    }
}
