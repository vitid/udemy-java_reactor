package org.practice;

import com.github.javafaker.Faker;

import reactor.core.publisher.Flux;
import static org.example.util.Util.*;

public class CarPricePublisher {
    public static void main(String[] args) {
        CarPricePublisher c = new CarPricePublisher();
        c.test();
    }

    void test(){
        carPriceFlux()
            .zipWith(demandFactorFlux())
            .map(tuple -> tuple.getT1() * tuple.getT2())
            .subscribe(subscriber("mySubscriber"));
    }
    Flux<Double> demandFactorFlux(){
        return Flux.range(0, 100)
                    .map(x -> Faker.instance().random().nextDouble() * 0.4 + 0.8);
    }

    Flux<Integer> carPriceFlux(){
        return Flux.generate(
            () -> 10000, 
            (price, syncSink) -> {
                syncSink.next(price);
                return price - 100;
        });
    }
}
