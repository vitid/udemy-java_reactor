package org.example.memo;

import reactor.core.publisher.Flux;
import static org.example.util.Util.*;



public class FluxSink {
    public static void main(String[] args) {
        FluxSink f = new FluxSink();
        f.test();
    }

    FluxSink() {

    }

    void test(){
        Flux.create((sink) -> {
            for (int i = 0; i < 10; i++) {
                sink.next(i);
                System.out.println("...");
            }

            sink.complete();
        })
        .log()
        .subscribe(subscriber("mySubscriber"));

    }
}
