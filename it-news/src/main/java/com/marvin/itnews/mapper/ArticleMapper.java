package com.marvin.itnews.mapper;

import com.marvin.itnews.dto.ArticleDTO;
import com.marvin.itnews.entity.Article;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    ArticleDTO toArticleDTO(Article article);

}
