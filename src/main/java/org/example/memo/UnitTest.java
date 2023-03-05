package org.example.memo;
import static org.example.util.Util.*;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.util.context.Context;

public class UnitTest {
    public static void main(String[] args) {
        UnitTest t = new UnitTest();
        
        //t.testComplete();

        //t.testError();

        //t.testRange();

        //t.testDelay();

        //t.testVirtualTime0();

        //t.testVirtualTime1();

        //t.testScenarioName();

        t.testContext();
    }

    void testComplete(){
        Flux<Integer> flux = Flux.just(1,2,3);

        StepVerifier.create(flux) // StepVerifier will subscribe to this Flux
            .expectNext(1)
            .expectNext(2)
            .expectNext(3)
            .expectComplete()
            .verify();

            StepVerifier.create(flux)
                .expectNext(1, 2) // no need to do one-by-one
                .assertNext(i -> Assertions.assertEquals(3, i)) // can use assertion operation as well
                .verifyComplete(); // shorthand for expectComplete() and verify()

    }

    void testError(){
        Flux<Integer> flux = Flux.just(1,2,3);
        Flux<Integer> errorFlux = Flux.error(new RuntimeException("some error"));

        Flux<Integer> combined = Flux.concat(flux, errorFlux);
        
        StepVerifier.create(combined)
            .expectNext(1,2,3)
            //.verifyError(); // verify that there's an error
            //.verifyError(IllegalArgumentException.class); // verify that error matches exception class
            .verifyErrorMessage("some error"); // verify that error message match
    }

    void testRange(){
        Flux<Integer> flux = Flux.just(1,2,3,4);

        StepVerifier.create(flux)
            .expectNextCount(3) // not care about what's the actual value of emitted 3 elements
            .expectNext(4)
            .verifyComplete();

        StepVerifier.create(flux)
            .thenConsumeWhile(i -> i < 4) // can provide predicate as well
            .expectNext(4)
            .verifyComplete();    
    }

    void testDelay(){
        Mono<Integer> mono = Mono.just(1)
                                .delayElement(Duration.ofSeconds(3));

        // no need to sleep the thread. Stepverifier will handle it                        
        StepVerifier.create(mono)
            .expectNext(1)
            .verifyComplete();   
        
        // if you dont' want to wait    
        StepVerifier.create(mono)
            .expectNext(1)
            .expectComplete()
            .verify(Duration.ofSeconds(1));
    }

    void testVirtualTime0(){

        Flux<Integer> flux = Flux.just(1,2,3,4,5)
                                .delayElements(Duration.ofSeconds(10));

        // for some reason. This: StepVerifier.withVirtualTime(() -> flux) is still blocked... The flux need to be return by the function somehow...
        StepVerifier.withVirtualTime(() -> slowFlux())
            .thenAwait(Duration.ofSeconds(600))
            .expectNext(1,2,3,4,5)
            .verifyComplete();
    }

    void testVirtualTime1(){

        StepVerifier.withVirtualTime(() -> slowFlux())
            .expectSubscription() // need this because there'll be subscription event
            .expectNoEvent(Duration.ofSeconds(5)) // should be no onNext in the next 5 seconds
            .thenAwait(Duration.ofSeconds(600))
            .expectNext(1,2,3,4,5)
            .verifyComplete();
    }

    void testScenarioName(){
        Flux<Integer> flux = Flux.just(1,2,3,4);

        StepVerifier.create(flux, StepVerifierOptions.create().scenarioName("my scenario A")) // this'll give you the scenario name
            .expectNextCount(10)
            .verifyComplete();
        
        StepVerifier.create(flux)
            .expectNext(1)
            .as("1 test")
            .expectNext(5)   
            .as("5 test") // this'll give more info if it fail
            .expectNextCount(3)
            .expectNextCount(4)
            .verifyComplete();
    }

    void testContext(){
        StepVerifier.create(contextFlux())
            .verifyError(RuntimeException.class); // verify there's an error if we don't set context
            
        // use  StepVerifierOptions to give the initial context
        StepVerifier.create(contextFlux(), StepVerifierOptions.create().withInitialContext(Context.of("myContext", "...")))
            .expectNextCount(4)
            .verifyComplete();
    }

    Flux<Integer> slowFlux(){
        return Flux.just(1,2,3,4,5)
                .delayElements(Duration.ofSeconds(10));
    }

    Flux<Integer> contextFlux(){
        return Flux.deferContextual(context -> {
            if(!context.hasKey("myContext")) return Flux.error(new RuntimeException());

            return Flux.just(1,2,3,4);
        });
    }
}
