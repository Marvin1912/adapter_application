package com.marvin.itnews.mapper;

import com.marvin.itnews.dto.ArticleDTO;
import com.marvin.itnews.dto.FeedSourceDTO;
import com.marvin.itnews.entity.Article;
import com.marvin.itnews.entity.FeedConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    ArticleDTO toArticleDTO(Article article);

    FeedSourceDTO toFeedSourceDTO(FeedConfig feedConfig);

}
