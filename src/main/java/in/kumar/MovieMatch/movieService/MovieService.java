package in.kumar.MovieMatch.movieService;

import in.kumar.MovieMatch.model.Movie;
import in.kumar.MovieMatch.model.MovieData;
import in.kumar.MovieMatch.model.MovieMatch;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class MovieService {

    private final EmbeddingModel embeddingModel;
    private final JsonMapper jsonMapper;

    private final List<Movie> embeddingMovies=new ArrayList<>();

    //constructor
    public MovieService(EmbeddingModel embeddingModel,
                        JsonMapper jsonMapper) {
        this.embeddingModel = embeddingModel;
        this.jsonMapper = jsonMapper;
    }

    public List<MovieMatch> search(String query){
        float[] userQueryEmbedding = embeddingModel.embed(query);

        List<MovieMatch> matches =new ArrayList<>();

        for (Movie movie : embeddingMovies){
            double similarity= cosineSimilarity(userQueryEmbedding , movie.getEmbedding());

            MovieMatch match =
                    new MovieMatch(movie.getTitle(),movie.getDescription(),similarity);

            matches.add(match);
        }
        sortBySimilarity(matches);

        return topKMatches(matches,3);

    }

    public List<MovieMatch> similarMovies(String title){
        Movie selectedMovies = findMovie(title);
        List<MovieMatch> matches = new ArrayList<>();

        for (Movie movie : embeddingMovies){
            if (movie.getTitle().equalsIgnoreCase(title)){
                continue;
            }
            double similariy =cosineSimilarity(
                    selectedMovies.getEmbedding(),
                    movie.getEmbedding()
            );
            MovieMatch match = new MovieMatch(
                    movie.getTitle(),
                    movie.getDescription(),
                    similariy);

            matches.add(match);
        }
        sortBySimilarity(matches);
        return topKMatches(matches,3);
    }

    private Movie findMovie(String title) {
        for (Movie movie : embeddingMovies){
            if (movie.getTitle().equalsIgnoreCase(title)){
                return movie;
            }
        }
        throw new IllegalArgumentException(
                "Movie not found: " + title
        );
    }


    private void sortBySimilarity(List<MovieMatch> matches) {
        Collections.sort(matches,
                (first,second)->Double.compare(
                        second.getMatch(),
                        first.getMatch()
                ));
    }

    private List<MovieMatch> topKMatches(List<MovieMatch> matches,int limit){
        List<MovieMatch> topMatches = new ArrayList<>();

        int numberOfMatches =Math.min(limit,matches.size());

        for (int i = 0; i < numberOfMatches; i++) {
            topMatches.add(matches.get(i));
        }
        return topMatches;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dotProdut = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProdut += (a[i] * b[i]);
            normA += (a[i] * a[i]);
            normB += (b[i] * b[i]);
        }
        if (normA ==0 || normB ==0){
            return 0.0;
        }
        return dotProdut/(Math.sqrt(normA)* Math.sqrt(normB));
    }

    @PostConstruct
    public void initializeMovies() throws IOException {
        ClassPathResource resource =
                new ClassPathResource("movies.json");

        InputStream inputStream =
                resource.getInputStream();

        List<MovieData> movieDataList= jsonMapper.readValue(
                inputStream,new TypeReference<>(){}
        );

        for (MovieData movieData : movieDataList){
            float[] embedding =embeddingModel.embed(
                    movieData.getDescription()
            );
            Movie movie = new Movie(
                    movieData.getTitle(),
                    movieData.getDescription(),
                    embedding
            );
            embeddingMovies.add(movie);
        }
        inputStream.close();
        System.out.println(
               embeddingMovies.size() + "movies loaded with embeddings."
        );

        for (Movie movie : embeddingMovies){
            System.out.println(Arrays.toString(movie.getEmbedding()));
        }

    }


}
