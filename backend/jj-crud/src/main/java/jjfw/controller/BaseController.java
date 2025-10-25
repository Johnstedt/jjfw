package jjfw.controller;

import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jjfw.service.BaseService;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseController<T, S extends BaseService<T, ?>> {

    protected final S service;
    protected final DSLContext dsl;

    public BaseController(S service, DSLContext dsl) {
        this.service = service;
        this.dsl = dsl;
    }

    @GetMapping
    @Operation(summary = "Get all entities", description = "Retrieve a list of all entities")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @Transactional(readOnly = true)
    public ResponseEntity<List<T>> getAll() {
        List<T> entities = service.findAll();
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter entities", description = "Filter by exact / range (Low/High suffix) / like (Like suffix) values. Supports sort, limit, offset.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered list")
    @Transactional(readOnly = true)
    public ResponseEntity<List<T>> filter(@RequestParam Map<String,String> params) {
        List<T> entities = service.filter(params);
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID", description = "Retrieve a single entity by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved entity"),
        @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<T> getById(
            @Parameter(description = "ID of the entity to retrieve")
            @PathVariable Integer id) {
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
    @Transactional
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
    @Transactional
    public ResponseEntity<T> update(
            @Parameter(description = "ID of the entity to update")
            @PathVariable Integer id,
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
    @Transactional
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the entity to delete")
            @PathVariable Integer id) {
        boolean deleted = service.delete(id);
        return deleted ? ResponseEntity.noContent().build()
                      : ResponseEntity.notFound().build();
    }
}
