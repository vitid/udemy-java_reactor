package org.example.memo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import static org.example.util.Util.*;

import java.util.function.Consumer;

import com.github.javafaker.Faker;




public class FluxSinkCreateGeneratePush {
    public static void main(String[] args) {
        FluxSinkCreateGeneratePush f = new FluxSinkCreateGeneratePush();
        //f.testFluxCreate();

        //f.testFluxCreateSeparateThread();

        //f.testFluxPush();

        //f.testFluxGenerate();

        f.testFluxGenerateWithStatus();
    }

    void testFluxCreate(){
        Flux.create((sink) -> {
            for (int i = 0; i < 20; i++) {
                // check for cancel from downstream so the publisher not keep doing the work
                if(sink.isCancelled())break;
                println("emitting:" + i);
                sink.next(i);
            }

            sink.complete();
        })
        .take(5)
        .subscribe(subscriber("mySubscriber"));

    }

    void testFluxCreateSeparateThread(){
        // extract consumer integerGenerator as its own instance, which can be used to emit value in another place
        IntegerGenerator integerGenerator = new IntegerGenerator();

        Flux.create(integerGenerator)
            .subscribe(subscriber("mySubscriber"));

        // show that calling integerGenerator to produce value is Thread-safe     
        // (always produce 10 value) 
        Runnable r = () -> integerGenerator.produce(Faker.instance().random().nextInt(0, 10));    
        for (int i = 0; i < 10; i++) {
            new Thread(r).start();
        }    

    }

    void testFluxPush(){
        // extract consumer integerGenerator as its own instance, which can be used to emit value in another place
        IntegerGenerator integerGenerator = new IntegerGenerator();

        Flux.push(integerGenerator)
            .subscribe(subscriber("mySubscriber"));

        // show that calling integerGenerator to produce value is not Thread-safe      
        // sometime produce less than 10 values
        Runnable r = () -> integerGenerator.produce(Faker.instance().random().nextInt(0, 10));    
        for (int i = 0; i < 10; i++) {
            new Thread(r).start();
        }    

    }

    void testFluxGenerate(){
        Flux.generate((syncSink) -> {
            // can execute only 1 .next(...)
            syncSink.next(Faker.instance().random().nextInt(1, 10));

            // no need to call .complete() or check for cancellation like in Flux.create(...)
        })
        .take(5)
        .subscribe(subscriber("mySubscriber"));
    }

    void testFluxGenerateWithStatus() {
        Flux.generate(() -> 0, 
            (counter, syncSink) -> {
                syncSink.next(counter); 
                if(counter > 5) syncSink.complete();
                return counter + 1;
            }
        )
        .subscribe(subscriber("mySubscriber"));
    }

    class IntegerGenerator implements Consumer<FluxSink<Integer>>{

        FluxSink<Integer> sink;

        @Override
        public void accept(FluxSink<Integer> sink) {
            this.sink = sink;
        }

        public void produce(int i){
            println("emitting:" + i);
            sink.next(i);
        }
        
    }
}
