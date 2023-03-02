package org.example.memo;

import static org.example.util.Util.*;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class Sink {
    public static void main(String[] args) {
        Sink sink = new Sink();

        sink.sinkOne();
    }

    void sinkOne(){
        Sinks.One<Object> sink = Sinks.one();

        Mono<Object> mono = sink.asMono();

        mono.subscribe(subscriber("mySubsriber"));

        sink.tryEmitValue("abcd");
    }
}
