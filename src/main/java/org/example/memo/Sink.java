package org.example.memo;

import static org.example.util.Util.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class Sink {
    public static void main(String[] args) {
        Sink sink = new Sink();

        //sink.sinkOne_Mono();

        //sink.sinkUnicast_Flux();
        sink.sinkMulticast_Flux();

        //sink.sink_Flux_ThreadSafety();
    }

    void sinkOne_Mono(){
        // we'll use this to push an item
        Sinks.One<Object> sink = Sinks.one();

        // we'll use this to subscribe to receive the item
        Mono<Object> mono = sink.asMono();

        mono.subscribe(subscriber("mySubsriber"));

        //sink.tryEmitValue("abcd");
        sink.emitValue("hi", (signalType, emitResult) -> {
            return true;
        });

        //this'll cause an error as Sinks.one() not suppose to emit the second value
        sink.emitValue("hello", (signalType, emitResult) -> {
            println(signalType.name());
            println(emitResult.name());
            sleeper(1).run();
            return true; // this'll keep retrying
        });

        //sink.tryEmitEmpty();

        //sink.tryEmitError(new RuntimeException("myException"));
    }

    void sinkUnicast_Flux(){

        // we'll use this to push an item
        Sinks.Many<Object> sink = Sinks.many().unicast().onBackpressureBuffer();

        // we'll use this to subscribe to receive the item
        Flux<Object> flux = sink.asFlux();

        flux.subscribe(subscriber("mySubscriber"));
        flux.subscribe(subscriber("mySubscriber2"));// this will receive an error and this subscriber will stop immediately

        sink.tryEmitNext("aaa");
        sink.tryEmitNext("bbb");
        sink.tryEmitNext("ccc");
        sink.tryEmitComplete();
    }

    void sinkMulticast_Flux(){

        // we'll use this to push an item
        Sinks.Many<Object> sink = Sinks.many().multicast().onBackpressureBuffer();

        // we'll use this to subscribe to receive the item
        Flux<Object> flux = sink.asFlux();

        sink.tryEmitNext("AAA"); // mySubscriber will still get 'AAA'

        flux.subscribe(subscriber("mySubscriber"));
        flux.subscribe(subscriber("mySubscriber2"));

        sink.tryEmitNext("aaa");
        sink.tryEmitNext("bbb");
        sink.tryEmitNext("ccc");

        flux.subscribe(subscriber("mySubscriber3"));
        sink.tryEmitNext("zzz"); // mySubscriber3 receive only 'zzz'

        sink.tryEmitComplete();

        println("====================");

        sink = Sinks.many().multicast().directAllOrNothing();// Sink will not wait for any subscribers

        flux = sink.asFlux();

        sink.tryEmitNext("AAA"); // now, mySubscriber will not get AAA

        flux.subscribe(subscriber("mySubscriber"));

        sink.tryEmitComplete();

        println("====================");

        sink = Sinks.many().replay().all(); // unlike thie first example, this one will cache and replay the message to all subscribers

        flux = sink.asFlux();

        sink.tryEmitNext("AAA");

        flux.subscribe(subscriber("mySubscriber"));
        flux.subscribe(subscriber("mySubscriber2"));

        sink.tryEmitNext("aaa");
        sink.tryEmitNext("bbb");
        sink.tryEmitNext("ccc");

        flux.subscribe(subscriber("mySubscriber3"));
        sink.tryEmitNext("zzz");

        sink.tryEmitComplete();
    }

    void sink_Flux_ThreadSafety(){
        Sinks.Many<Object> sink = Sinks.many().unicast().onBackpressureBuffer();

        Flux<Object> flux = sink.asFlux();
        List<Object> list = new LinkedList<>();

        flux.doOnNext(o -> list.add(o))
            .doOnError(e -> {
                println(e.getCause() + ":" + e.getMessage());
            })
            .subscribe(subscriber("mySubscriber"));

        for (int i = 0; i < 1000; i++) {
            CompletableFuture.runAsync(() -> {
                //sink.tryEmitNext(""); // try this. The final list size will not be 1000
                sink.emitNext("", (signalType, emitResult) -> {
                    // some thread will receive 'FAIL_NON_SERIALIZED' EmitResult signal, which will not even trigger onError
                    if(emitResult.isFailure()){
                        println(signalType.name());
                        println(emitResult.name());
                    }
                    return true; // need to always retry to make it thread-safe
                });

            });
        }

        sleeper(3).run();
        println("size: " + list.size());
    }
}
