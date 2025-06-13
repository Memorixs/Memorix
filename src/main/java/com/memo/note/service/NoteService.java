package com.memo.note.service;

import org.springframework.stereotype.Service;

import com.memo.category.entity.Category;
import com.memo.note.repository.NoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteService {
	private final NoteRepository noteRepository;

	public void deleteByCategory(Category category) {
		noteRepository.deleteByCategory(category);
	}
}
