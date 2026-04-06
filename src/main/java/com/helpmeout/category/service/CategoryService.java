package com.helpmeout.category.service;

import com.helpmeout.category.dto.CategoryResponse;
import com.helpmeout.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllActive() {
        return categoryRepository.findAllByIsActiveTrue()
                .stream()
                .map(c -> new CategoryResponse(c.getId(), c.getNameAr(), c.getSlug()))
                .toList();
    }
}