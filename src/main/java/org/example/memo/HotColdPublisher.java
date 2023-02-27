package org.example.memo;
import static org.example.util.Util.*;

import java.time.Duration;
import java.util.stream.Stream;

import reactor.core.publisher.Flux;

public class HotColdPublisher {
    public static void main(String[] args) {
        HotColdPublisher p = new HotColdPublisher();

        //p.hotPublisherShare();

        //p.hotPublisherPublish();

        //p.hotPublisherPublishAutoConnect();

        p.hotPublisherCache();
    }

    void hotPublisherShare(){
        Flux<String> movies = Flux.fromStream(() -> getMovies())
                                    .delayElements(Duration.ofSeconds(1))
                                    .share();

        movies.subscribe(subscriber("userA"));

        sleeper(3).run();

        movies.subscribe(subscriber("userB"));

        sleeper(20).run();
    }

    void hotPublisherPublish(){
        Flux<String> movies = Flux.fromStream(() -> getMovies())
                                    .delayElements(Duration.ofSeconds(1))
                                    .publish()
                                    .refCount(1);

        movies.subscribe(subscriber("userA"));

        sleeper(3).run();

        movies.subscribe(subscriber("userB"));

        sleeper(10).run();

        movies.subscribe(subscriber("userC"));

        sleeper(10).run();
    }

    void hotPublisherPublishAutoConnect(){
        Flux<String> movies = Flux.fromStream(() -> getMovies())
                                    .delayElements(Duration.ofSeconds(1))
                                    .publish()
                                    .autoConnect(1);

        movies.subscribe(subscriber("userA"));

        sleeper(3).run();

        movies.subscribe(subscriber("userB"));

        sleeper(10).run();

        movies.subscribe(subscriber("userC"));

        sleeper(10).run();
    }

    void hotPublisherCache(){
        Flux<String> movies = Flux.fromStream(() -> getMovies())
                                    .delayElements(Duration.ofSeconds(1))
                                    .cache(3);

        movies.subscribe(subscriber("userA"));

        sleeper(3).run();

        movies.subscribe(subscriber("userB"));

        sleeper(10).run();

        movies.subscribe(subscriber("userC"));

        sleeper(10).run();
    }

    Stream<String> getMovies(){
        return Stream.of("1", "2", "3", "4", "5", "6");
    }
}
