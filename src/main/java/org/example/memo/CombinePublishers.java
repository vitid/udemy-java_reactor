package org.example.memo;
import static org.example.util.Util.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;

import reactor.core.publisher.Flux;

public class CombinePublishers {
    List<String> nameCaches = new ArrayList<>();
    public static void main(String[] args) {
        CombinePublishers c = new CombinePublishers();

        //c.startWith();

        //c.concatWith();

        //c.concatWithError();

        //c.merge();

        //c.zip();

        c.combineLatest();
    }

    void startWith(){
        generateNames()
            .startWith(nameCacher()) // drain from nameCacher first
            .take(3)
            .subscribe(subscriber("mySubscriberA"));

        generateNames()
            .startWith(nameCacher())
            .take(4) // only the 4th element is freshley generated. Also observe that the first 3 names is being processed very fast
            .subscribe(subscriber("mySubscriberB"));    

        generateNames()
            .startWith(nameCacher())
            .filter(n -> n.startsWith("A"))
            .take(3) // only the 4th element is freshley generated. Also observe that the first 3 names is being processed very fast
            .subscribe(subscriber("mySubscriberB"));        
    }

    void concatWith(){
        // this is the opposite of startWith. It'll drain from the first publisher first before going to the second publisher
        Flux.range(0,5)
            .concatWith(Flux.range(5,5))
            .subscribe(subscriber("mySubscriber"));
    }

    void concatWithError(){
        Flux.concat(Flux.range(0,5),
            Flux.error(new Exception("Some error...")), // this'll cause the flow to stop
            Flux.range(5,5)
        )
        .subscribe(subscriber("mySubscriber"));

        Flux.concatDelayError(Flux.range(0,5),
            Flux.error(new Exception("Some error...")), // this'll delay the error (downstream will stream will still get onError)
            Flux.range(5,5)
        )
        .subscribe(subscriber("mySubscriber"));
    }

    void merge(){
        // emit as each publisher emitting. Will not wait for a complete drainage
        Flux<Integer> f0 = Flux.range(0, 5)
                                .delayElements(Duration.ofMillis(100));

        Flux<Integer> f1 = Flux.range(5, 5)
                                .delayElements(Duration.ofMillis(10));   
                                
        Flux<Integer> f2 = Flux.range(10, 5)
                                .delayElements(Duration.ofMillis(1000));   
                                
        Flux.merge(f0, f1, f2)
            .subscribe(subscriber("mySubscriber"));  
            
        sleeper(5).run();    
    }

    void zip(){
        Flux<Integer> f0 = Flux.range(0, 5)
                                .delayElements(Duration.ofMillis(100));

        Flux<Integer> f1 = Flux.range(5, 5)
                                .delayElements(Duration.ofMillis(10));
                                // the below line will imediately terminate the pipeline. It'll terminate as soon as we got the error, not wait for any tuple(0,5,10) and (1,6,11) will not even printed
                                //.doOnNext(x -> {if(x == 7) throw new RuntimeException("some error...");});   
                                
        Flux<Integer> f2 = Flux.range(10, 4) // this'll cause the stream to terminate earlier
                                .delayElements(Duration.ofMillis(1000));   
                                
        Flux.zip(f0, f1, f2) // this will wait for values from all publishers
            .subscribe(subscriber("mySubscriber"));  
            
        sleeper(10).run();
    }

    void combineLatest(){
        Flux<Integer> f0 = Flux.range(0, 5)
                                .delayElements(Duration.ofMillis(5))
                                .doOnNext(x -> {println("emitting: " + x);});

        Flux<Integer> f1 = Flux.range(5, 5)
                                .delayElements(Duration.ofMillis(10))
                                .doOnNext(x -> {println("emitting: " + x);}); 

       Flux.combineLatest(f0, f1, (i0,i1) -> 
            {
                return i0 + "," + i1;
            })
            .doOnNext(x -> {println("emitting: " + x);}) // this'll run on a thread that emit the latest value
            .subscribe(subscriber("mySubscriber"));  // this'll also run on a thread that emit the latest value
            
        sleeper(5).run();    
    }

    Flux<String> generateNames(){
        return Flux.generate((syncSink) -> {
            sleeperMil(1000).run();
            String name = Faker.instance().name().fullName();
            println("generate fresh: " + name);
            nameCaches.add(name);
            syncSink.next(name);
        });
    }

    Flux<String> nameCacher(){
        return Flux.fromIterable(nameCaches);
    }
}
