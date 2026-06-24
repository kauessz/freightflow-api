package com.freightflow.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StorageConfig")
class StorageConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(StorageConfig.class)
            .withPropertyValues(
                    "freightflow.storage.bucket=freightflow-docs",
                    "freightflow.storage.access-key-id=test-access-key",
                    "freightflow.storage.secret-access-key=test-secret-key"
            );

    @Test
    @DisplayName("Nao cria beans S3 quando endpoint esta em branco")
    void naoCriaBeansS3QuandoEndpointEstaEmBranco() {
        contextRunner
                .withPropertyValues("freightflow.storage.endpoint=")
                .run(context -> {
                    assertThat(context).hasSingleBean(StorageProperties.class);
                    assertThat(context).doesNotHaveBean(S3Client.class);
                    assertThat(context).doesNotHaveBean(S3Presigner.class);
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    @DisplayName("Cria beans S3 quando endpoint valido esta configurado")
    void criaBeansS3QuandoEndpointValidoEstaConfigurado() {
        contextRunner
                .withPropertyValues("freightflow.storage.endpoint=https://example-bucket.local")
                .run(context -> {
                    assertThat(context).hasSingleBean(S3Client.class);
                    assertThat(context).hasSingleBean(S3Presigner.class);
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    @DisplayName("Falha com mensagem clara quando endpoint nao tem scheme")
    void falhaQuandoEndpointNaoTemScheme() {
        contextRunner
                .withPropertyValues("freightflow.storage.endpoint=r2.local")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("freightflow.storage.endpoint must include http:// or https://");
                });
    }
}
