package com.medtrack.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medtrack.dto.MedicineUsageLogDto;
import com.medtrack.dto.MedicineUsageSummaryDto;
import com.medtrack.service.MedicineUsageLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/medicine-logs")
@Tag(name = "Medicine Usage Tracking", description = "Operations for logging medicine intake, tracking adherence, and generating usage reports")
@SecurityRequirement(name = "bearerAuth")
public class MedicineUsageLogController {

    private final MedicineUsageLogService medicineUsageLogService;

    @GetMapping("/{userId}/time/{days}")
    @Operation(summary = "Get usage logs for past days", description = """
            Retrieves medicine usage logs for a specified number of past days.

            **Returns aggregated data showing:**
            - Total doses taken per medicine
            - Total doses missed per medicine
            - Usage patterns over the specified period

            **Time Zone**: All calculations are done in Asia/Kolkata timezone.
            """, tags = { "Usage Reports" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usage logs retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MedicineUsageSummaryDto.class)), examples = @ExampleObject(value = """
                    [
                        {
                            "healthProductId": 1,
                            "healthProductName": "Paracetamol 500mg",
                            "takenCount": 14,
                            "missedCount": 4
                        },
                        {
                            "healthProductId": 2,
                            "healthProductName": "Vitamin D3",
                            "takenCount": 7,
                            "missedCount": 0
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "errors": {
                            "error": "User Not Found"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<MedicineUsageSummaryDto>> getLogsForPastDays(
            @Parameter(description = "User ID to get logs for", required = true, example = "1") @PathVariable("userId") Long userId,
            @Parameter(description = "Number of past days to retrieve logs for", required = true, example = "7") @PathVariable("days") Integer days) {
        return ResponseEntity.ok(medicineUsageLogService.getLogForTime(userId, days));
    }

    @PostMapping("/log")
    @Operation(summary = "Add medicine usage log", description = """
            Records whether a medicine dose was taken or missed.

            **Important Notes:**
            - Set `isTaken: true` when medicine was consumed
            - Set `isTaken: false` when medicine was missed/skipped
            - Automatically validates sufficient quantity is available
            - Timestamp is recorded automatically in Asia/Kolkata timezone

            **Validation:**
            - Checks if user exists
            - Validates health product exists and belongs to user
            - Ensures sufficient quantity available (if taken = true)
            """, tags = { "Usage Logging" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Medicine usage logged successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ""))),
            @ApiResponse(responseCode = "400", description = "Invalid input or insufficient quantity", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "errors": {
                            "error": "Insufficient dose quantity available"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "User or health product not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "errors": {
                            "error": "Product Not Found"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Void> addLog(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Medicine usage log details", required = true, content = @Content(schema = @Schema(implementation = MedicineUsageLogDto.class), examples = {
                    @ExampleObject(name = "Medicine Taken", summary = "Log when medicine was taken", value = """
                            {
                                "userId": 1,
                                "healthProductId": 1,
                                "isTaken": true
                            }
                            """),
                    @ExampleObject(name = "Medicine Missed", summary = "Log when medicine was missed/skipped", value = """
                            {
                                "userId": 1,
                                "healthProductId": 1,
                                "isTaken": false
                            }
                            """)
            })) @RequestBody MedicineUsageLogDto logDto) {
        System.out.println("Received log: " + logDto);
        medicineUsageLogService.add(logDto);
        System.out.println("Log added successfully");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{userId}/today")
    @Operation(summary = "Get today's usage summary", description = """
            Retrieves a comprehensive summary of today's medicine usage for a user.

            **Returns for each medicine:**
            - Number of doses taken today
            - Number of doses missed (based on scheduled reminders)
            - Medicine details (name, ID)

            **Smart Logic:**
            - If no logs exist for today, shows all user's medicines with 0 taken count
            - Missed count calculated from scheduled reminders vs actual taken
            - Only shows valid medicines (filters out null/invalid entries)

            **Time Zone**: All calculations use Asia/Kolkata timezone.
            """, tags = { "Usage Reports" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Today's usage summary retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MedicineUsageSummaryDto.class)), examples = @ExampleObject(value = """
                    [
                        {
                            "healthProductId": 1,
                            "healthProductName": "Paracetamol 500mg",
                            "takenCount": 2,
                            "missedCount": 0
                        },
                        {
                            "healthProductId": 2,
                            "healthProductName": "Vitamin D3",
                            "takenCount": 1,
                            "missedCount": 0
                        },
                        {
                            "healthProductId": 3,
                            "healthProductName": "Blood Pressure Medicine",
                            "takenCount": 0,
                            "missedCount": 2
                        }
                    ]
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "errors": {
                            "error": "User Not Found"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<MedicineUsageSummaryDto>> getTodayLogs(
            @Parameter(description = "User ID to get today's usage for", required = true, example = "1") @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(medicineUsageLogService.getOneDay(userId));
    }
}