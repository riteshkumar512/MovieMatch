package in.kumar.MovieMatch.movieController;

import in.kumar.MovieMatch.model.MovieMatch;
import in.kumar.MovieMatch.movieService.MovieService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public List<MovieMatch> search(@RequestParam String query){
        return service.search(query);
    }

    @GetMapping("/{title}/similar")
    public List<MovieMatch> similarMovies(@PathVariable String title){
        return service.similarMovies(title);
    }
}
