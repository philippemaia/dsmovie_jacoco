package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService movieService;

	@Mock
	private MovieRepository movieRepository;

	private Long existingMovieId, nonExistingMovieId, dependentMovieId;
	private String movieTitle;
	private MovieEntity movie;
	private MovieDTO movieDTO;
	private PageImpl<MovieEntity> page;

	@BeforeEach
	void setUp() throws Exception{
		existingMovieId = 1L;
		nonExistingMovieId = 2L;
		dependentMovieId = 3L;

		movieTitle = "Test Movie";

		movie = MovieFactory.createMovieEntity();
		movieDTO = new MovieDTO(movie);
		page = new PageImpl<>(List.of(movie));

		Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movie));
		Mockito.when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());

		Mockito.when(movieRepository.searchByTitle(any(), (Pageable) any())).thenReturn(page);

		Mockito.when(movieRepository.save(any())).thenReturn(movie);

		Mockito.when(movieRepository.getReferenceById(existingMovieId)).thenReturn(movie);
		Mockito.when(movieRepository.getReferenceById(nonExistingMovieId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(movieRepository.existsById(existingMovieId)).thenReturn(true);
		Mockito.when(movieRepository.existsById(dependentMovieId)).thenReturn(true);
		Mockito.when(movieRepository.existsById(nonExistingMovieId)).thenReturn(false);

		Mockito.doNothing().when(movieRepository).deleteById(existingMovieId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(movieRepository).deleteById(dependentMovieId);
	}

	@Test
	public void findAllShouldReturnPagedMovieDTO() {

		Pageable pageable = PageRequest.of(0,12);

		Page<MovieDTO> result = movieService.findAll(movieTitle, pageable);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(1, result.getSize());
		Assertions.assertEquals(movieTitle, result.iterator().next().getTitle());

	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = movieService.findById(existingMovieId);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingMovieId);
		Assertions.assertEquals(result.getTitle(), movie.getTitle());
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			movieService.findById(nonExistingMovieId);
		});
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {
		MovieDTO result = movieService.insert(movieDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), movie.getId());
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = movieService.update(existingMovieId, movieDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingMovieId);
		Assertions.assertEquals(result.getTitle(), movieDTO.getTitle());
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			movieService.update(nonExistingMovieId, movieDTO);
		});
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			movieService.delete(existingMovieId);
		});
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			movieService.delete(nonExistingMovieId);
		});
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			movieService.delete(dependentMovieId);
		});
	}
}
