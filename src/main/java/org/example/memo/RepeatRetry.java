package org.example.memo;
import static org.example.util.Util.*;

import java.time.Duration;

import com.github.javafaker.Faker;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class RepeatRetry {
    public static void main(String[] args) {
        RepeatRetry r = new RepeatRetry();

        //r.repeat();
        
        //r.retry();

        //r.retryWhen();

        r.retryWhenAdvance();
    }

    void repeat(){
        intFlux()
            //.repeat(BooleanSupplier) // can also use this to specify terminate condition
            .repeat(3) // observe that 'inFlux complete' is printed multiple times but the main complete of mySubscription is executed only once
            .subscribe(subscriber("mySubscription"));
    }

    void retry(){
        intFluxError()
            .retry(3)
            .subscribe(subscriber("mySubscription"));
    }

    void retryWhen(){
        intFluxError()
            .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
            .subscribe(subscriber("mySubscription"));

        sleeper(5).run();
    }

    void retryWhenAdvance(){
        serverFlux()
            .doOnError(e -> println("error:" + e.getMessage()))
            .retryWhen(Retry.from(signalFlux -> { //Flux<RetrySignal>
                return signalFlux.handle((retrySignal, syncSink) -> {
                    if(retrySignal.failure().getMessage().equals("500")) {
                        syncSink.next(1); // emit any signal to let the retry continue
                    } else {
                        syncSink.error(retrySignal.failure()); // send failure to downstream if we received 404 response
                    }
                })
                .delayElements(Duration.ofSeconds(1)); // delay to not bombard the server
            }))
            .subscribe(subscriber("mySubscription"));

        sleeper(5).run();    
    }

    Flux<Integer> intFlux(){
        return Flux.range(0, 10)
                    .doOnComplete(() -> println("intFlux complete"));
    }

    Flux<Integer> intFluxError(){
        return Flux.range(0, 10)
                    .doOnNext((i) -> println("emitting: " + i))
                    .map(x -> x / 0)
                    .doOnError((e) -> println("error:" + e.getMessage()));
    }

    Mono<String> serverFlux(){
        return Mono.fromSupplier(() -> {
            int v = Faker.instance().random().nextInt(0, 10);

            if(v < 8) throw new RuntimeException("500");
            if(v < 10) throw new RuntimeException("404");

            return "success";
        });
    }
}
