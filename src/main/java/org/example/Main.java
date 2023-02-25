package org.example;

import com.github.javafaker.Faker;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import static org.example.util.Util.*;

public class Main {
    Faker faker = Faker.instance();
    public static void main(String[] args) {

        Main main = new Main();
        main.test();

    }

    Main(){

    }

    public void test(){
        // Flux.range(0, 10)
        // .map(x -> generateName())
        // .subscribe(printer);

        Flux.from(Mono.just(1));

        Mono<Integer> m = Flux.range(0, 10)
            .next();
    }

    public String generateName(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return faker.name().fullName();

    }
}