package api.saude.feminina.controller.article;

import api.saude.feminina.dto.article.ArticleDto;
import api.saude.feminina.dto.article.ArticleResponseDto;
import api.saude.feminina.model.user.UserModel;
import api.saude.feminina.service.article.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseEntity<List<ArticleResponseDto>> getAllArticles() {
        return ResponseEntity.ok(articleService.findAll().stream().map(ArticleResponseDto::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseDto> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(ArticleResponseDto.from(articleService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ArticleResponseDto> saveArticle(@RequestBody @Valid ArticleDto articleDto,
                                                          @AuthenticationPrincipal UserModel author) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ArticleResponseDto.from(articleService.save(articleDto, author)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponseDto> updateArticle(@PathVariable Long id,
                                                            @RequestBody @Valid ArticleDto articleDto) {
        return ResponseEntity.ok(ArticleResponseDto.from(articleService.update(id, articleDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
