package api.saude.feminina.controller.user;

import api.saude.feminina.config.security.TokenService;
import api.saude.feminina.dto.user.LoginDto;
import api.saude.feminina.dto.user.LoginResponseDto;
import api.saude.feminina.dto.user.UserDto;
import api.saude.feminina.dto.user.UserResponseDto;
import api.saude.feminina.message.CustomMessage;
import api.saude.feminina.model.user.UserModel;
import api.saude.feminina.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          TokenService tokenService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginDto loginDto) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenService.generateToken((UserModel) auth.getPrincipal());
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Object> saveUser(@RequestBody @Valid UserDto userDto) {
        if (userService.existsByEmail(userDto.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new CustomMessage("Este e-mail já está em uso!"));
        }
        var userModel = new UserModel();
        userModel.setName(userDto.name());
        userModel.setEmail(userDto.email());
        userModel.setPassword(passwordEncoder.encode(userDto.password()));
        userModel.setRole(userDto.userRole());
        userModel.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.from(userService.save(userModel)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll().stream().map(UserResponseDto::from).toList());
    }

}
