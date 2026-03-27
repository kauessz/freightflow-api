package com.freightflow.modules.csvimport;

import com.freightflow.modules.csvimport.dto.ImportResult;
import com.freightflow.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/import")
@Tag(name = "CSV Import", description = "Batch import of shipments via CSV file upload")
@SecurityRequirement(name = "Bearer Authentication")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping(value = "/shipments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Import shipments from CSV",
        description = "Upload a CSV file to create multiple shipments at once. "
                + "Maximum 500 rows, 5MB file size. Returns detailed results with successes and errors.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Import completed (may contain partial errors)",
                content = @Content(schema = @Schema(implementation = ImportResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file format or empty file"),
            @ApiResponse(responseCode = "409", description = "Business rule violation (e.g., exceeds row limit)")
        }
    )
    public ResponseEntity<ImportResult> importShipments(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal user) {

        ImportResult result = importService.importShipments(file, user.getTenantId());

        // Retorna 200 mesmo com erros parciais — o client analisa successCount/errorCount
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/template", produces = "text/csv")
    @Operation(
        summary = "Download CSV template",
        description = "Returns a CSV template file with header and 3 example rows"
    )
    public ResponseEntity<String> downloadTemplate() {
        String template = importService.generateTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=freightflow-shipments-template.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(template);
    }

    @GetMapping(value = "/formats", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "CSV format documentation",
        description = "Returns JSON documentation describing the expected CSV format, "
                + "column definitions, validation rules and examples"
    )
    public ResponseEntity<String> getFormats() {
        return ResponseEntity.ok(importService.getFormatDocumentation());
    }
}
