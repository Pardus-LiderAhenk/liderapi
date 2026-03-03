package tr.org.lider.operation.logs;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import tr.org.lider.constant.RoleConstants;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "OperationLogType", description = "Operation Log Type Rest Service")
@Secured({RoleConstants.ROLE_ADMIN,  RoleConstants.ROLE_USER})
@RequestMapping("/api/lider/operation-log-type")
public class OperationLogTypeController {

    private final OperationLogTypeService operationLogTypeService;

    public OperationLogTypeController(OperationLogTypeService operationLogTypeService) {
        this.operationLogTypeService = operationLogTypeService;
    }

    @Operation(summary = "Get operation log type list", description = "", tags = { "operation-log-type" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns operation log type list. Successful"),
            @ApiResponse(responseCode = "417", description = "Could not get operation log type list. Unexpected error occurred",
                    content = @Content(schema = @Schema(implementation = String.class))) })
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OperationLogTypeImpl>> operationLogTypeListAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(operationLogTypeService.listAll());
    }
}
