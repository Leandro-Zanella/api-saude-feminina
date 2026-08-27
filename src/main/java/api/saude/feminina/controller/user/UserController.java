package api.saude.feminina.controller.user;

import api.saude.feminina.config.security.TokenService;
import api.saude.feminina.dto.user.LoginDto;
import api.saude.feminina.dto.user.LoginResponseDto;
import api.saude.feminina.dto.user.UserDto;
import api.saude.feminina.dto.user.UserResponseDto;
import api.saude.feminina.dto.user.UserUpdateDto;
import api.saude.feminina.message.CustomMessage;
import api.saude.feminina.model.user.UserModel;
import api.saude.feminina.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          TokenService tokenService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginDto loginDto) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var userModel = (UserModel) auth.getPrincipal();
        var token = tokenService.generateToken(userModel);
        return ResponseEntity.ok(new LoginResponseDto(UserResponseDto.from(userModel), token));
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody @Valid UserDto userDto) {
        if (userService.existsByEmail(userDto.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new CustomMessage("Este e-mail já está em uso!"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.from(userService.register(userDto)));
    }

    @PostMapping
    public ResponseEntity<Object> saveUser(@RequestBody @Valid UserDto userDto) {
        if (userService.existsByEmail(userDto.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new CustomMessage("Este e-mail já está em uso!"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.from(userService.save(userDto)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll().stream().map(UserResponseDto::from).toList());
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserResponseDto>> getAllAdmins() {
        return ResponseEntity.ok(userService.findAllAdmins().stream().map(UserResponseDto::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponseDto.from(userService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id,
                                             @RequestBody @Valid UserUpdateDto userUpdateDto) {
        if (userService.existsByEmailForOtherUser(userUpdateDto.email(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new CustomMessage("Este e-mail já está em uso!"));
        }
        return ResponseEntity.ok(UserResponseDto.from(userService.update(id, userUpdateDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
