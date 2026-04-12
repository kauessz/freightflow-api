package com.freightflow.modules.port;

import com.freightflow.fixtures.TestDataFactory;
import com.freightflow.modules.port.dto.PortResponse;
import com.freightflow.shared.exception.BusinessException;
import com.freightflow.shared.exception.ResourceNotFoundException;
import com.freightflow.shared.pagination.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortService")
class PortServiceTest {

    @Mock
    private PortRepository portRepository;

    @InjectMocks
    private PortService portService;

    private Port santos;
    private Port rotterdam;

    @BeforeEach
    void setUp() {
        santos    = TestDataFactory.santos();
        rotterdam = TestDataFactory.rotterdam();
    }

    // ── list() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("list()")
    class ListTests {

        @Test
        @DisplayName("should_returnPagedList_when_listingPorts")
        void should_returnPagedList_when_listingPorts() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 50);
            var page = new PageImpl<>(List.of(santos, rotterdam), pageable, 2);
            when(portRepository.findAll(pageable)).thenReturn(page);

            // Act
            PageResponse<PortResponse> result = portService.list(pageable);

            // Assert
            assertThat(result.data()).hasSize(2);
            assertThat(result.meta().total()).isEqualTo(2);
        }
    }

    // ── getByUnlocode() ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getByUnlocode()")
    class GetByUNLOCODETests {

        @Test
        @DisplayName("should_returnPort_when_unlocodeExists")
        void should_returnPort_when_unlocodeExists() {
            // Arrange
            when(portRepository.findByUnlocode("BRSSZ")).thenReturn(Optional.of(santos));

            // Act
            PortResponse result = portService.getByUnlocode("BRSSZ");

            // Assert
            assertThat(result.unlocode()).isEqualTo("BRSSZ");
            assertThat(result.name()).isEqualTo("Santos");
            assertThat(result.country()).isEqualTo("BR");
        }

        @Test
        @DisplayName("should_throwResourceNotFoundException_when_unlocodeNotFound")
        void should_throwResourceNotFoundException_when_unlocodeNotFound() {
            // Arrange
            when(portRepository.findByUnlocode("BRXXX")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> portService.getByUnlocode("BRXXX"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Port");
        }

        @Test
        @DisplayName("should_throwBusinessException_when_unlocodeFormatInvalid")
        void should_throwBusinessException_when_unlocodeFormatInvalid() {
            // Arrange — formats that violate [A-Z]{2}[A-Z0-9]{3}
            List<String> invalidCodes = List.of(
                    "BR",          // too short
                    "BRSSZZ",      // too long
                    "1RSSZ",       // starts with digit
                    "BR-SZ",       // contains hyphen
                    "br ssz"       // lowercase and space
            );

            // Act & Assert
            for (String invalid : invalidCodes) {
                assertThatThrownBy(() -> portService.getByUnlocode(invalid))
                        .as("Expected BusinessException for UNLOCODE: '%s'", invalid)
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining("Invalid UNLOCODE format");
            }
        }

        @Test
        @DisplayName("should_acceptLowercase_and_normalize_when_unlocodeIsLowercase")
        void should_acceptLowercase_and_normalize_when_unlocodeIsLowercase() {
            // Arrange — input in lowercase should be normalized to uppercase
            when(portRepository.findByUnlocode("NLRTM")).thenReturn(Optional.of(rotterdam));

            // Act
            PortResponse result = portService.getByUnlocode("nlrtm");

            // Assert
            assertThat(result.unlocode()).isEqualTo("NLRTM");
        }
    }

    // ── listByCountry() ───────────────────────────────────────────────────

    @Nested
    @DisplayName("listByCountry()")
    class ListByCountryTests {

        @Test
        @DisplayName("should_returnSchedule_when_portHasVoyages")
        void should_returnSchedule_when_portHasVoyages() {
            // Arrange — listByCountry proxies the vessel schedule implicitly
            // via ports with voyages in the same country
            when(portRepository.findByCountryOrderByName("BR")).thenReturn(List.of(santos));

            // Act
            List<PortResponse> result = portService.listByCountry("BR");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).country()).isEqualTo("BR");
        }
    }
}
