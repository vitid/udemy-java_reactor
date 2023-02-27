package org.example.memo;

import static org.example.util.Util.*;

import java.time.Duration;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ThreadingSchedule {
    public static void main(String[] args) {
        ThreadingSchedule t = new ThreadingSchedule();

        //t.publishOnSubscribeOn();

        //t.subscribeOnPublishOn();

        //t.multiSubscribeOn();

        //t.multiPublishOn();

        t.parallelOnNext();
    }

    void publishOnSubscribeOn(){

        Flux.range(0, 3)
            .doOnNext(x -> println("A emitting: " + x)) //A will be emitting in boundElastic thread
            .publishOn(Schedulers.parallel())
            .doOnNext(x -> println("B emitting:" + x)) //B will be emitting in parallel thread
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(subscriber("mySubscriber")); // onNext will be executed on parallel thread

        sleeper(3).run();    

    }

    void subscribeOnPublishOn(){

        Flux.range(0, 3)
            .doOnNext(x -> println("A emitting: " + x)) //A will be emitting in parallel thread
            .subscribeOn(Schedulers.parallel())
            .doOnNext(x -> println("B emitting:" + x)) //B will be emitting in parallel thread
            .publishOn(Schedulers.boundedElastic())
            .subscribe(subscriber("mySubscriber")); // onNext will be executed on boundElastic thread

        sleeper(3).run();    

    }

    void multiSubscribeOn(){
        // subscribeOn is for Publisher. The one that's closer to the source take priority
        Flux.range(0, 3)
            .doOnNext(x -> println("A emitting: " + x)) //A will be emitting in threadX
            .subscribeOn(Schedulers.newParallel("threadX"))
            .doOnNext(x -> println("B emitting:" + x)) //B will be emitting in threadX
            .subscribeOn(Schedulers.newParallel("threadY"))
            .doOnNext(x -> println("C emitting:" + x)) //C will be emitting in threadX
            .subscribeOn(Schedulers.newParallel("threadZ"))
            .subscribe(subscriber("mySubscriber")); // onNext will be executed on threadX

        sleeper(3).run(); 
    }

    void multiPublishOn(){
        // publishOn is for subscriber. Thread will be switch in downstream direction
        Flux.range(0, 3)
            .doOnNext(x -> println("A emitting: " + x)) //A will be emitting in main thread
            .publishOn(Schedulers.newParallel("threadX"))
            .doOnNext(x -> println("B emitting:" + x)) //B will be emitting in threadX
            .publishOn(Schedulers.newParallel("threadY"))
            .doOnNext(x -> println("C emitting:" + x)) //C will be emitting in threadY
            .publishOn(Schedulers.newParallel("threadZ"))
            .subscribe(subscriber("mySubscriber")); // onNext will be executed on threadZ

        sleeper(3).run(); 
    }

    void parallelOnNext(){
        Flux.range(0, 10)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .doOnNext(x -> println("emitting:" + x))
            .subscribe(subscriber("mySubscriber")); // you can see that the number printed is out of order

        sleeper(3).run();    
    }

}
