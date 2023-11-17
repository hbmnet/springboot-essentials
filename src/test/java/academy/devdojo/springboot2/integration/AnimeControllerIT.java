package academy.devdojo.springboot2.integration;


import academy.devdojo.springboot2.domain.Anime;
import academy.devdojo.springboot2.repository.AnimeRepository;
import academy.devdojo.springboot2.requests.AnimePostRequestBody;
import academy.devdojo.springboot2.util.AnimeCreator;
import academy.devdojo.springboot2.util.AnimePostRequestBodyCreator;
import academy.devdojo.springboot2.util.AnimePutRequestBodyCreator;
import academy.devdojo.springboot2.wrapper.PageableResponse;
import org.assertj.core.api.Assertions;
import org.hibernate.grammars.ordering.OrderingParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AnimeControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private AnimeRepository animeRepository;



    @Test
    @DisplayName("list returns list of anime inside page object when successful")
    void list_ReturnsListOfAnimesInsidePageObject_WhenSuccessful(){

        Anime saveAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        String expectedName = saveAnime.getName();

        PageableResponse<Anime> animePage = testRestTemplate.exchange("/animes", HttpMethod.GET, null,
                new ParameterizedTypeReference<PageableResponse<Anime>>() {
                }).getBody();


        Assertions.assertThat(animePage).isNotNull();
        Assertions.assertThat(animePage.toList())
                .isNotEmpty()
                .hasSize(1);


        Assertions.assertThat(animePage.toList().get(0).getName()).isEqualTo(expectedName);
    }


    @Test
    @DisplayName("listAll returns list of anime when successful")
    void listAll_ReturnsListOfAnimes_WhenSuccessful(){

        Anime saveAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        String expectedName = saveAnime.getName();

        List<Anime> animes = testRestTemplate.exchange("/animes/all", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();


        Assertions.assertThat(animes)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);


        Assertions.assertThat(animes.get(0).getName()).isEqualTo(expectedName);

    }


    @Test
    @DisplayName("findById returns anime when successful")
    void findById_ReturnsAnimes_WhenSuccessful(){

        Anime saveAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        Long expectedId = saveAnime.getId();

        Anime anime = testRestTemplate.getForObject("/animes/{id}", Anime.class, expectedId);


        Assertions.assertThat(anime)
                .isNotNull();

        Assertions.assertThat(anime.getId()).isNotNull().isEqualTo(expectedId);

    }


    @Test
    @DisplayName("findByName returns an empty list of anime when is not found")
    void findByName_ReturnsListOfAnime_WhenAnime(){

        Anime saveAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        String expectedName = saveAnime.getName();

        String url = String.format("/animes/find?name=%s", expectedName);

        List<Anime> animes = testRestTemplate.exchange(url , HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();


        Assertions.assertThat(animes)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);


        Assertions.assertThat(animes.get(0).getName()).isEqualTo(expectedName);


    }


    @Test
    @DisplayName("findByName returns an empty list of anime when is not found")
    void findByName_ReturnsEmptyListOfAnime_WhenAnimeIsNotFound(){


        List<Anime> animes = testRestTemplate.exchange("/animes/find?name=dbz" , HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();


        Assertions.assertThat(animes)
                .isNotNull()
                .isEmpty();


    }


    @Test
    @DisplayName("save returns anime when successful")
    void save_ReturnsAnimes_WhenSuccessful(){

        AnimePostRequestBody animePostRequestBody = AnimePostRequestBodyCreator.createAnimePostRequestBody();

        ResponseEntity<Anime> animeResponseEntity = testRestTemplate.postForEntity("/animes", animePostRequestBody, Anime.class);


        Assertions.assertThat(animeResponseEntity).isNotNull();
        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(animeResponseEntity.getBody()).isNotNull();
        Assertions.assertThat(animeResponseEntity.getBody().getId()).isNotNull();


    }

    @Test
    @DisplayName("replace updates anime when successful")
    void replace_UpdatesAnimes_WhenSuccessful(){

        Anime saveAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        saveAnime.setName("nem name");

        ResponseEntity<Void> animeResponseEntity = testRestTemplate.exchange("/animes",
                HttpMethod.PUT, new HttpEntity<>(saveAnime), Void.class);


        Assertions.assertThat(animeResponseEntity).isNotNull();
        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);


    }


    @Test
    @DisplayName("delete removes anime when successful")
    void delete_RemovesAnimes_WhenSuccessful(){


        Anime saveAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());


        ResponseEntity<Void> animeResponseEntity = testRestTemplate.exchange("/animes/{id}",
                HttpMethod.DELETE, null, Void.class, saveAnime.getId());


        Assertions.assertThat(animeResponseEntity).isNotNull();
        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }


}
