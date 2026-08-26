package api.saude.feminina.dto.article;

import jakarta.validation.constraints.NotBlank;

public record ArticleDto(

        @NotBlank
        String title,

        String summary,

        @NotBlank
        String contentHtml,

        String coverImageUrl
) {
}
