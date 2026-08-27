package api.saude.feminina.service.user;

import api.saude.feminina.config.exception.NotFoundException;
import api.saude.feminina.dto.user.UserDto;
import api.saude.feminina.dto.user.UserUpdateDto;
import api.saude.feminina.model.user.RoleModel;
import api.saude.feminina.model.user.UserModel;
import api.saude.feminina.repository.user.RoleRepository;
import api.saude.feminina.repository.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    /** Cadastro público do app: o papel é sempre USER, qualquer que seja o enviado. */
    @Transactional
    public UserModel register(UserDto userDto) {
        return this.create(userDto, RoleModel.USER);
    }

    /** Cadastro pela gestão web: grava o papel informado no corpo. */
    @Transactional
    public UserModel save(UserDto userDto) {
        return this.create(userDto, userDto.userRole());
    }

    /** Edição pela gestão web. A senha não é alterada por aqui. */
    @Transactional
    public UserModel update(Long id, UserUpdateDto userUpdateDto) {
        var userModel = this.getById(id);
        userModel.setName(userUpdateDto.name());
        userModel.setEmail(userUpdateDto.email());
        userModel.setRole(this.getOrCreateRole(userUpdateDto.userRole()));
        return userRepository.save(userModel);
    }

    public boolean existsByEmailForOtherUser(String email, Long id) {
        return userRepository.existsByEmailAndIdNot(email, id);
    }

    /** Exclusão lógica: o registro permanece e passa a ser ignorado nas consultas. */
    @Transactional
    public void delete(Long id) {
        var userModel = this.getById(id);
        userModel.setDeletedAt(LocalDateTime.now());
        userRepository.save(userModel);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<UserModel> findAll() {
        return userRepository.findAllByDeletedAtIsNull();
    }

    public List<UserModel> findAllAdmins() {
        return userRepository.findAllByRole_NameAndDeletedAtIsNull(RoleModel.ADMIN);
    }

    public UserModel getById(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + id));
    }

    public UserModel getByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);
    }

    private UserModel create(UserDto userDto, String roleName) {
        var userModel = new UserModel();
        userModel.setName(userDto.name());
        userModel.setEmail(userDto.email());
        userModel.setPassword(passwordEncoder.encode(userDto.password()));
        userModel.setRole(this.getOrCreateRole(roleName));
        return userRepository.save(userModel);
    }

    /** Cria o papel na primeira vez que ele é usado, evitando carga inicial de dados. */
    private RoleModel getOrCreateRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new RoleModel(name)));
    }
}
