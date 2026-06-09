package az.aztu.egrant.iam.web;

import az.aztu.egrant.iam.domain.AccountStatus;
import az.aztu.egrant.iam.internal.UserService;
import az.aztu.egrant.iam.web.dto.RoleUpdateRequest;
import az.aztu.egrant.iam.web.dto.UpdateProfileRequest;
import az.aztu.egrant.iam.web.dto.UserResponse;
import az.aztu.egrant.shared.security.AuthenticatedUser;
import az.aztu.egrant.shared.storage.FileStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Users", description = "User profiles, approval queue, blocking and roles")
public class UserController {

    private static final String ADMIN = "hasAnyRole('ADMIN','SUPER_ADMIN')";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    @PreAuthorize(ADMIN)
    @Operation(summary = "List users (optionally filtered by account status)")
    public List<UserResponse> list(@RequestParam(required = false) AccountStatus status) {
        return userService.list(status);
    }

    @GetMapping("/users/pending")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Approval queue (PENDING users)")
    public List<UserResponse> pending() {
        return userService.list(AccountStatus.PENDING);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get a user by id")
    public UserResponse get(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping("/me")
    @Operation(summary = "Current user's profile")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return userService.getById(principal.userId());
    }

    @PutMapping("/me")
    @Operation(summary = "Edit own profile (may complete the profile)")
    public UserResponse updateMe(@AuthenticationPrincipal AuthenticatedUser principal,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.userId(), request);
    }

    @PostMapping("/me/avatar")
    @Operation(summary = "Upload own avatar")
    public UserResponse uploadAvatar(@AuthenticationPrincipal AuthenticatedUser principal,
                                     @RequestParam("file") MultipartFile file) {
        return userService.setAvatar(principal.userId(), file.getOriginalFilename(),
                file.getContentType(), readBytes(file));
    }

    @GetMapping("/users/{id}/avatar")
    @Operation(summary = "Stream a user's avatar")
    public ResponseEntity<InputStreamResource> avatar(@PathVariable Long id) {
        FileStorage.StoredFile stored = userService.loadAvatar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(stored.contentType()))
                .contentLength(stored.size())
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(new InputStreamResource(stored.content()));
    }

    @PostMapping("/users/{id}/approval")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Approve a pending user")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        userService.approve(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Reject/delete a user")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        userService.reject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{id}/block")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Block a user")
    public ResponseEntity<Void> block(@PathVariable Long id) {
        userService.block(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{id}/block")
    @PreAuthorize(ADMIN)
    @Operation(summary = "Unblock a user")
    public ResponseEntity<Void> unblock(@PathVariable Long id) {
        userService.unblock(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Set a user's global role")
    public ResponseEntity<Void> setRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        userService.setRole(id, request.role());
        return ResponseEntity.noContent().build();
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
