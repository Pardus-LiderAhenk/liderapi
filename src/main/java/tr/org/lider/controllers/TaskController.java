package tr.org.lider.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.constant.RoleConstants;
import tr.org.lider.entities.PluginTask;
import tr.org.lider.repositories.PluginTaskRepository;
import tr.org.lider.services.TaskService;
import tr.org.lider.utils.IRestResponse;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


@Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_COMPUTERS})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lider/task/execute")
@Tag(name = "Task", description = "Task Rest Service")
public class TaskController {

    private final TaskService taskService;
    private final PluginTaskRepository pluginTaskRepository;
    Logger logger = LoggerFactory.getLogger(TaskController.class);

    private ResponseEntity<IRestResponse> validateAndExecute(
            PluginTask requestBody, String expectedRole, String endpoint) {

        endpoint = "/api/lider/task/execute/" + endpoint;
        List<PluginTask> task = pluginTaskRepository.findByCommandId(requestBody.getCommandId());

        if (task.isEmpty()) {
            logger.warn("Unknown commandId '{}' on endpoint '{}'", requestBody.getCommandId(), endpoint);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String requiredRole = task.getFirst().getRole();

        if (requiredRole == null) {
            logger.warn("CommandId '{}' has no assigned role. Rejected on endpoint '{}'",
                    requestBody.getCommandId(), endpoint);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!expectedRole.equals(requiredRole)) {
            logger.warn("CommandId '{}' role mismatch on '{}'. Expected: '{}', Found: '{}'",
                    requestBody.getCommandId(), endpoint, expectedRole, requiredRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        IRestResponse restResponse = taskService.execute(requestBody);
        return ResponseEntity.status(HttpStatus.OK).body(restResponse);
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_RESOURCE_USAGE })
    @Operation(summary = "Execute Resource Usage task ", description = "Executes a Resource Usage task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource Usage task executed successfully"),
            @ApiResponse(responseCode = "417", description = "Resource Usage task execution failed", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/resource-usage")
    public ResponseEntity<IRestResponse> executeResourceUsageTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/resource-usage' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_RESOURCE_USAGE, "resource-usage");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_NETWORK_MANAGER })
    @Operation(summary = "Execute Network Management task ", description = "Executes a Network Management task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Network Management task executed successfully"),
            @ApiResponse(responseCode = "417", description = "Network Management task execution failed", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/network-management")
    public ResponseEntity<IRestResponse> executeNetworkManagementTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/network-management' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_NETWORK_MANAGER, "network-management");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_SESSION_POWER })
    @Operation(summary = "Execute Session Power task ", description = "Executes a Session Power task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session Power task executed successfully"),
            @ApiResponse(responseCode = "417", description = "Session Power task execution failed", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/session-power")
    public ResponseEntity<IRestResponse> executeSessionPowerTask(@RequestBody PluginTask requestBody) {

        logger.info("Request received. URL: '/lider/task/execute/session-power' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_SESSION_POWER, "session-power");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_MANAGE_ROOT })
    @Operation(summary = "Execute Manage Root task ", description = "Executes a Manage Root task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Manage Root task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Manage Root task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/manage-root")
    public ResponseEntity<IRestResponse> executeManageRootTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/manage-root' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_MANAGE_ROOT, "manage-root");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_FILE_MANAGEMENT })
    @Operation(summary = "Execute File Management task ", description = "Executes a File Management task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File Management task executed successfully."),
            @ApiResponse(responseCode = "417", description = "File Management task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/file-management")
    public ResponseEntity<IRestResponse> executeFileManagementTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/file-management' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_FILE_MANAGEMENT, "file-management");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_SCRIPT })
    @Operation(summary = "Execute Script task ", description = "Executes a Script task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Script task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Script task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/script")
    public ResponseEntity<IRestResponse> executeScriptTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/script' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_SCRIPT, "script");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_PACKAGE_INSTALL_REMOVE })
    @Operation(summary = "Execute Package Install/Remove task ", description = "Executes a Package Install/Remove task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Package Install/Remove task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Package Install/Remove task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/package-install-remove")
    public ResponseEntity<IRestResponse> executePackageInstallRemoveTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/package-install-remove' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_PACKAGE_INSTALL_REMOVE, "package-install-remove");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_PACKAGE_LIST })
    @Operation(summary = "Execute Installed Packages task ", description = "Executes an Installed Packages task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Installed Packages task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Installed Packages task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/installed-packages")
    public ResponseEntity<IRestResponse> executeInstallPackagesTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/installed-packages' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_PACKAGE_LIST, "installed-packages");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_PACKAGE_CONTROL })
    @Operation(summary = "Execute Check Package task ", description = "Executes a Check Package task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check Package task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Check Package task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/check-package")
    public ResponseEntity<IRestResponse> executeCheckPackageTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/check-package' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_PACKAGE_CONTROL, "check-package");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_LOCAL_USER })
    @Operation(summary = "Execute Local User task ", description = "Executes a Local User task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Local User task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Local User task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/local-user")
    public ResponseEntity<IRestResponse> executeLocalUserTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/local-user' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_LOCAL_USER, "local-user");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_PACKAGE_REPO })
    @Operation(summary = "Execute Repository Management task ", description = "Executes a Repository Management task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repository Management task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Repository Management task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/repositories")
    public ResponseEntity<IRestResponse> executeRepositoryManagementTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/repositories' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_PACKAGE_REPO, "repositories");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_SERVICE_MANAGEMENT })
    @Operation(summary = "Execute Service Management task ", description = "Executes a Service Management task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service Management task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Service Management task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/service-management")
    public ResponseEntity<IRestResponse> executeServiceManagementTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/service-management' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_SERVICE_MANAGEMENT, "service-management");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_SEND_MESSAGE })
    @Operation(summary = "Execute Instant Message task ", description = "Executes an Instant Message task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Instant Message task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Instant Message task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/instant-message")
    public ResponseEntity<IRestResponse> executeInstantMessageTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/instant-message' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_SEND_MESSAGE, "instant-message");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_DEVICE_MANAGEMENT })
    @Operation(summary = "Execute Device Management task ", description = "Executes a Device Management task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device Management task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Device Management task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/device-management")
    public ResponseEntity<IRestResponse> executeDeviceManagementTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/device-management' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_DEVICE_MANAGEMENT, "device-management");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_USB_RULE })
    @Operation(summary = "Execute USB Rule Management task ", description = "Executes a USB Rule Management task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "USB Rule Management task executed successfully."),
            @ApiResponse(responseCode = "417", description = "USB Rule Management task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/usb-rule-management")
    public ResponseEntity<IRestResponse> executeUsbRuleManagementTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/usb-rule-management' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_USB_RULE, "usb-rule-management");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_CONKY })
    @Operation(summary = "Execute Conky task", description = "Executes a Conky task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conky task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Conky task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/conky")
    public ResponseEntity<IRestResponse> executeConkyTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/conky' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_CONKY, "conky");
    }

    @Secured({ RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_REMOTE_ACCESS })
    @Operation(summary = "Execute Remote Access Management task ", description = "Executes a Remote Access Management task on selected targets", tags = {
            "task" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Remote Access Management task executed successfully."),
            @ApiResponse(responseCode = "417", description = "Remote Access Management task execution failed.", content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/remote-access")
    public ResponseEntity<IRestResponse> executeRemoteAccessTask(@RequestBody PluginTask requestBody) {
        logger.info("Request received. URL: '/lider/task/execute/remote-access' Body: {}", requestBody);
        return validateAndExecute(requestBody, RoleConstants.ROLE_REMOTE_ACCESS, "remote-access");
    }
}
