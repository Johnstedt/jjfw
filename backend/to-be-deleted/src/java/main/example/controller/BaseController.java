package Main.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import Main.example.service.BaseService;
import java.util.List;
import java.util.Optional;

public abstract class BaseController<T, S extends BaseService<T, ?>> {

    protected final S service;

    public BaseController(S service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all entities", description = "Retrieve a list of all entities")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<T>> getAll() {
        List<T> entities = service.findAll();
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID", description = "Retrieve a single entity by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved entity"),
        @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    public ResponseEntity<T> getById(
            @Parameter(description = "ID of the entity to retrieve")
            @PathVariable Long id) {
        Optional<T> entity = service.findById(id);
        return entity.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new entity", description = "Create a new entity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created entity"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<T> create(@RequestBody T entity) {
        T created = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update entity", description = "Update an existing entity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated entity"),
        @ApiResponse(responseCode = "404", description = "Entity not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<T> update(
            @Parameter(description = "ID of the entity to update")
            @PathVariable Long id,
            @RequestBody T entity) {
        Optional<T> updated = service.update(id, entity);
        return updated.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete entity", description = "Delete an entity by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted entity"),
        @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the entity to delete")
            @PathVariable Long id) {
        boolean deleted = service.delete(id);
        return deleted ? ResponseEntity.noContent().build()
                      : ResponseEntity.notFound().build();
    }
}
