package com.freightflow.modules.port;

import com.freightflow.modules.port.dto.CreatePortRequest;
import com.freightflow.modules.port.dto.PortResponse;
import com.freightflow.modules.port.dto.UpdatePortRequest;
import com.freightflow.shared.pagination.PageResponse;
import com.freightflow.shared.rbac.RequiresRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ports")
@Tag(name = "Ports", description = "Port directory — 44 seeded ports with UNLOCODE, timezone and coordinates")
@SecurityRequirement(name = "Bearer Authentication")
public class PortController {

    private final PortService portService;

    public PortController(PortService portService) {
        this.portService = portService;
    }

    @GetMapping
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "List all ports", description = "Authenticated master-data read used by operational and RFQ forms. Paginated list ordered by name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ports listed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient role to read ports")
    })
    public ResponseEntity<PageResponse<PortResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(portService.list(pageable));
    }

    @GetMapping("/{id}")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "Get port by ID", description = "Authenticated master-data read used by operational and RFQ forms.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Port found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient role to read ports"),
            @ApiResponse(responseCode = "404", description = "Port not found")
    })
    public ResponseEntity<PortResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(portService.getById(id));
    }

    @GetMapping("/unlocode/{unlocode}")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "Get port by UNLOCODE", description = "Authenticated master-data read used by operational and RFQ forms. Example: BRSSZ for Santos, NLRTM for Rotterdam.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Port found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient role to read ports"),
            @ApiResponse(responseCode = "404", description = "Port not found")
    })
    public ResponseEntity<PortResponse> getByUnlocode(@PathVariable String unlocode) {
        return ResponseEntity.ok(portService.getByUnlocode(unlocode));
    }

    @GetMapping("/country/{country}")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "List ports by country code", description = "Authenticated master-data read used by operational and RFQ forms. ISO 2-letter country code (e.g., BR, NL, CN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ports listed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient role to read ports")
    })
    public ResponseEntity<List<PortResponse>> listByCountry(@PathVariable String country) {
        return ResponseEntity.ok(portService.listByCountry(country.toUpperCase()));
    }

    @GetMapping("/search")
    @RequiresRole({"ADMIN", "OPERATOR", "VIEWER", "CLIENT"})
    @Operation(summary = "Search ports", description = "Authenticated master-data read used by operational and RFQ forms. Search by name or UNLOCODE (case insensitive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ports returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient role to read ports")
    })
    public ResponseEntity<List<PortResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(portService.search(q));
    }

    @PostMapping
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Create a new port")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Port created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient role to create ports")
    })
    public ResponseEntity<PortResponse> create(@Valid @RequestBody CreatePortRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portService.create(request));
    }

    @PutMapping("/{id}")
    @RequiresRole({"ADMIN", "OPERATOR"})
    @Operation(summary = "Update a port")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Port updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient role to update ports"),
            @ApiResponse(responseCode = "404", description = "Port not found")
    })
    public ResponseEntity<PortResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdatePortRequest request) {
        return ResponseEntity.ok(portService.update(id, request));
    }
}
