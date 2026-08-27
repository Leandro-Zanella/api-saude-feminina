package api.saude.feminina.repository.article;

import api.saude.feminina.model.article.ArticleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<ArticleModel, Long> {

    List<ArticleModel> findAllByDeletedAtIsNullOrderByUpdatedAtDesc();

    Optional<ArticleModel> findByIdAndDeletedAtIsNull(Long id);
}
