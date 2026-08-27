package api.saude.feminina.service.article;

import api.saude.feminina.config.exception.NotFoundException;
import api.saude.feminina.dto.article.ArticleDto;
import api.saude.feminina.model.article.ArticleModel;
import api.saude.feminina.model.user.UserModel;
import api.saude.feminina.repository.article.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    /** Mais recentes primeiro, para o app já refletir as edições no topo da lista. */
    public List<ArticleModel> findAll() {
        return articleRepository.findAllByDeletedAtIsNullOrderByUpdatedAtDesc();
    }

    public ArticleModel getById(Long id) {
        return articleRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Artigo não encontrado: " + id));
    }

    @Transactional
    public ArticleModel save(ArticleDto articleDto, UserModel author) {
        var articleModel = new ArticleModel();
        articleModel.setAuthor(author);
        this.copyToModel(articleDto, articleModel);
        return articleRepository.save(articleModel);
    }

    @Transactional
    public ArticleModel update(Long id, ArticleDto articleDto) {
        var articleModel = this.getById(id);
        this.copyToModel(articleDto, articleModel);
        return articleRepository.save(articleModel);
    }

    /** Exclusão lógica: o registro permanece e passa a ser ignorado nas consultas. */
    @Transactional
    public void delete(Long id) {
        var articleModel = this.getById(id);
        articleModel.setDeletedAt(LocalDateTime.now());
        articleRepository.save(articleModel);
    }

    private void copyToModel(ArticleDto articleDto, ArticleModel articleModel) {
        articleModel.setTitle(articleDto.title());
        articleModel.setSummary(articleDto.summary());
        articleModel.setContentHtml(articleDto.contentHtml());
        articleModel.setCoverImageUrl(articleDto.coverImageUrl());
    }
}
