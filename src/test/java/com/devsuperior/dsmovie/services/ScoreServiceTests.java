package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService scoreService;

	@Mock
	private UserService userService;

	@Mock
	private ScoreRepository scoreRepository;

	@Mock
	private MovieRepository movieRepository;

	private Long existingScoreId, nonExistingScoreId;
	private ScoreEntity score;
	private ScoreDTO scoreDTO;

	private Long existingMovieId, nonExistingMovieId;
	private MovieEntity movie;
	private MovieDTO movieDTO;

	private UserEntity user;

	@BeforeEach
	void setUp() throws Exception{

		existingMovieId = 1L;
		nonExistingMovieId = 2L;

		existingScoreId = 1L;
		nonExistingScoreId = 2L;

		user = UserFactory.createUserEntity();

		score = ScoreFactory.createScoreEntity();
		scoreDTO = new ScoreDTO(score);

		movie = MovieFactory.createMovieEntity();
		movieDTO = new MovieDTO(movie);

		Mockito.when(userService.authenticated()).thenReturn(user);

		Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movie));
		Mockito.when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());

		Mockito.when(scoreRepository.saveAndFlush(any())).thenReturn(score);

		Mockito.when(movieRepository.save(any())).thenReturn(movie);

	}
	
	@Test
	public void saveScoreShouldReturnMovieDTO() {

		Mockito.when(userService.authenticated()).thenReturn(user);

		ScoreEntity scoreEntity = ScoreFactory.createScoreEntity();
		movie.setId(existingMovieId);
		movie.getScores().add(scoreEntity);

		score.setMovie(movie);

		scoreDTO = new ScoreDTO(score);

		MovieDTO result = scoreService.saveScore(scoreDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), movie.getId());
		Assertions.assertEquals(result.getScore(), movie.getScore());
	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {

		Mockito.when(userService.authenticated()).thenReturn(user);

		ScoreEntity scoreEntity = ScoreFactory.createScoreEntity();
		movie.setId(nonExistingScoreId);
		movie.getScores().add(scoreEntity);

		score.setMovie(movie);

		scoreDTO = new ScoreDTO(score);

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			scoreService.saveScore(scoreDTO);
		});
	}
}
