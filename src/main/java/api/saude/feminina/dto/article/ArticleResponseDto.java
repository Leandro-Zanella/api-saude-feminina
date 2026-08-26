package api.saude.feminina.dto.article;

import api.saude.feminina.model.article.ArticleModel;

import java.time.LocalDateTime;

public record ArticleResponseDto(
        Long id,
        String title,
        String summary,
        String contentHtml,
        String coverImageUrl,
        String authorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ArticleResponseDto from(ArticleModel article) {
        return new ArticleResponseDto(article.getId(), article.getTitle(), article.getSummary(),
                article.getContentHtml(), article.getCoverImageUrl(), article.getAuthor().getName(),
                article.getCreatedAt(), article.getUpdatedAt());
    }
}
