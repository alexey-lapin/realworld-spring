/*
 * MIT License
 *
 * Copyright (c) 2019 - present Alexey Lapin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.al.realworld.application.command;

import com.github.al.realworld.api.command.DeleteComment;
import com.github.al.realworld.api.command.DeleteCommentResult;
import com.github.al.realworld.bus.CommandHandler;
import com.github.al.realworld.domain.model.Article;
import com.github.al.realworld.domain.model.Comment;
import com.github.al.realworld.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.al.realworld.application.exception.NoAuthorizationException.forbidden;
import static com.github.al.realworld.application.exception.ResourceNotFoundException.notFound;

@RequiredArgsConstructor
@Service
public class DeleteCommentHandler implements CommandHandler<DeleteCommentResult, DeleteComment> {

    private final ArticleRepository articleRepository;

    @Transactional
    @Override
    public DeleteCommentResult handle(DeleteComment command) {
        Article article = articleRepository.findBySlug(command.getSlug())
                .orElseThrow(() -> notFound("article [slug=%s] does not exist", command.getSlug()));

        Comment comment = article.getComments().stream()
                .filter(c -> c.getId().equals(command.getId()))
                .findFirst()
                .orElseThrow(() -> notFound("comment [id=%s] does not exist", command.getId()));

        if (!comment.getAuthor().getUsername().equals(command.getCurrentUsername())) {
            throw forbidden();
        }

        Set<Comment> alteredComments = article.getComments().stream()
                .filter(comment1 -> Objects.equals(comment1, comment))
                .collect(Collectors.toSet());

        Article alteredArticle = article.toBuilder().comments(alteredComments).build();
        articleRepository.save(alteredArticle);

        return new DeleteCommentResult();
    }
}
