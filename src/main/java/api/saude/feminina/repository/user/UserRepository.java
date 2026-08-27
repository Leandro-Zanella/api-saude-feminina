package api.saude.feminina.repository.user;

import api.saude.feminina.model.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {

    /** Considera também os excluídos, para o e-mail continuar reservado. */
    boolean existsByEmail(String email);

    /** Usada na edição: ignora o próprio usuário, senão editar sem trocar o e-mail daria conflito. */
    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<UserModel> findByEmailAndDeletedAtIsNull(String email);

    Optional<UserModel> findByIdAndDeletedAtIsNull(Long id);

    List<UserModel> findAllByDeletedAtIsNull();

    List<UserModel> findAllByRole_NameAndDeletedAtIsNull(String roleName);
}
