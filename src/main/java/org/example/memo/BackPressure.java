package org.example.memo;
import static org.example.util.Util.*;

import java.util.ArrayList;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class BackPressure {
    public static void main(String[] args) {
        BackPressure b = new BackPressure();

        b.backPressureDrop();
    }

    void backPressureDrop(){

        // this value come from Queues.SMALL_BUFFER_SIZE
        System.setProperty("reactor.bufferSize.small", "16");

        var drops = new ArrayList<Integer>();
        Flux.range(0, 100)
            .doOnNext(x -> sleeperMil(5).run())
            .doOnNext(x -> println("publish: " + x))
            .onBackpressureDrop((e) -> drops.add(e))
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(x -> {sleeperMil(10).run();})
            .subscribe(subscriber("mySubscriber"));


        sleeper(5).run();    

        println("drop: " + drops.size() + " elements");
    }
}
