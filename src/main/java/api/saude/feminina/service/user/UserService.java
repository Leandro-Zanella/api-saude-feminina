package api.saude.feminina.service.user;

import api.saude.feminina.dto.user.UserDto;
import api.saude.feminina.model.user.RoleModel;
import api.saude.feminina.model.user.UserModel;
import api.saude.feminina.repository.user.RoleRepository;
import api.saude.feminina.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserModel register(UserDto userDto) {
        var userModel = new UserModel();
        userModel.setName(userDto.name());
        userModel.setEmail(userDto.email());
        userModel.setPassword(passwordEncoder.encode(userDto.password()));
        userModel.setRole(this.getOrCreateRole(userDto.userRole()));
        return userRepository.save(userModel);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<UserModel> findAll() {
        return userRepository.findAll();
    }

    public UserModel getByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /** Cria o papel na primeira vez que ele é usado, evitando carga inicial de dados. */
    private RoleModel getOrCreateRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new RoleModel(name)));
    }
}
