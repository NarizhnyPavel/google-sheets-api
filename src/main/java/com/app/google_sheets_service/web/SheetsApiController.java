package com.app.google_sheets_service.web;

import com.app.api.model.WriteResponse;
import com.app.exceptions.model.ErrorResponse;
import com.app.exceptions.model.ValidationErrorResponse;
import com.app.google_sheets_service.service.GoogleSheetsService;
import com.app.google_sheets_service.model.GoogleSheetsWriteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/sheets")
public class SheetsApiController {

    private final GoogleSheetsService googleSheetsService;

    @Operation(
            summary = "Write to google sheet",
            responses = {
                    @ApiResponse(responseCode = "200", description = "The values are recorded", content = @Content(schema = @Schema(implementation = WriteResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Requested data not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    WriteResponse writeToGoogleSheet(@RequestBody GoogleSheetsWriteRequest request){
        return googleSheetsService.writeToCompanyTable(request);
    }

}
