package org.example.memo;

import static org.example.util.Util.*;

import java.time.Duration;

import com.github.javafaker.Faker;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
public class Batching {
    public static void main(String[] args) {
        Batching b = new Batching();

        //b.batchBuffer();

        //b.batchBufferSkip();

        b.batchWindown();

        //b.batchGroupBy();
    }

    void batchBuffer(){
        eventFlux()
            .buffer(5) // note that even though we define 5, if the source complete emitting the signal, it'll also terminate and yield the collected values so far
            //.buffer(Duration.ofSeconds(10)) // instead of waiting for 5 elements, it'll wait for 10 secons and return all elements in those 10 seconds
            //.bufferTimeout(5, Duration.ofSeconds(10)) // hybrid approach of the above 2. It'll emit all items collected when either one of the above conditions met
            .subscribe(subscriber("mySubscriber"));

        sleeper(5).run();
    }

    void batchBufferSkip(){
        eventFlux()
            .buffer(5, 1) // this'll drop the first n element. Useful for seeing consecutive pattern
            .subscribe(subscriber("mySubscriber"));

        sleeper(5).run();

        eventFlux()
            .buffer(5, 10) // this basically mean give me 5 items for every 10 items. Useful for sampling data
            .subscribe(subscriber("mySubscriber"));

        sleeper(5).run();
    }

    void batchWindown(){
        eventFlux()
            .window(5)
            .flatMap(flux -> fluxProcessor(flux))
            .subscribe(subscriber("mySubscription"));
        
        sleeper(5).run();
    }

    Mono<String> fluxProcessor(Flux<Long> flux){
        println("...starting a new batch...");
        return flux
                .doOnNext(l -> println(l + ""))
                .then(Mono.just("success processing"));
    }

    void batchGroupBy(){
        Flux.interval(Duration.ofMillis(100))
            .map(l -> Faker.instance().random().nextInt(0, 100))
            .groupBy(x -> x % 3)
            .subscribe( gf -> processGroup(gf, gf.key()));

        sleeper(10).run();
    }

    void processGroup(Flux<Integer> flux, int key){
        flux.buffer(5)
            .subscribe(subscriber("subscriber_group_" + key));
    }

    Flux<Long> eventFlux(){
        return Flux.interval(Duration.ofMillis(100))
                   .handle((l, syncSink) -> {
                        syncSink.next(l);
                        if(l == 27) syncSink.complete();
                   });
    }
}
