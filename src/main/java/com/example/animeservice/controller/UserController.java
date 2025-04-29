package com.example.animeservice.controller;

import com.example.animeservice.dto.ErrorResponse;
import com.example.animeservice.dto.UserDto;
import com.example.animeservice.dto.UserWithCollectionsDto;
import com.example.animeservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "API for managing user entities")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get all users", description = "Retrieves a list of all users.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "List of users retrieved",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
                           @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a user by its ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User retrieved",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
                           @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Create a new user", description = "Creates a new user entity.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "User created",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
                           @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @Operation(summary = "Update a user", description = "Updates an existing user by its ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
                           @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                           @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto dto
    ) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @Operation(summary = "Delete a user", description = "Deletes a user by its ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "User deleted"),
                           @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user with collections",
            description = "Retrieves a user with their "
                    + "collections and associated anime by user ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "User with collections retrieved",
                    content = @Content(schema =
                    @Schema(implementation = UserWithCollectionsDto.class))),
                           @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/full")
    public ResponseEntity<UserWithCollectionsDto>
        getUserWithCollectionsAndAnime(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserWithCollectionsAndAnime(id));
    }

    @Operation(summary = "Get all users with collections",
            description = "Retrieves all users with their collections and associated anime.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "List of users with collections retrieved",
                    content = @Content(schema =
                    @Schema(implementation = UserWithCollectionsDto.class))),
                           @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/full")
    public ResponseEntity<List<UserWithCollectionsDto>> getAllUserWithCollectionsAndAnime() {
        return ResponseEntity.ok(userService.getAllUsersWithCollectionsAndAnimes());
    }

    @Operation(summary = "Search users",
            description = "Searches users by username or email.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User search results",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
                           @ApiResponse(responseCode = "404", description = "No users found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(
            @Parameter(description = "Username to search for (case-insensitive)")
            @RequestParam(required = false) String username,
            @Parameter(description = "Email to search for (case-insensitive)")
            @RequestParam(required = false) String email
    ) {
        List<UserDto> list = userService.searchUsers(username, email);
        if (list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(list);
    }
}