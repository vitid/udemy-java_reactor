package org.example.memo;
import static org.example.util.Util.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.reactivestreams.Publisher;

import com.github.javafaker.Faker;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

public class Operator {
    record User(int userId, String name){};
    record PurchaseOrder(String item, int price, int userId){};
    public static void main(String[] args) {
        Operator o = new Operator();  
        
        //o.handleOperator();

        //o.callbackOperator();

        //o.limitRateOperator();

        //o.delayElementsOperator();

        //o.transformOperator();

        o.switchOnFirstOperator();

        //o.flatMapOperator();

        //o.flatMapOperatorMultiThread();

        //o.concatMapOperator();

        //o.onErrorReturnOperator();

        //o.onErrorResumeOperator();

        //o.onErrorContinueOperator();

        //o.timeoutOperator();

        //o.defaultIfEmptyOperator();

        //o.switchIfEmptyOperator();
    }

    void handleOperator(){
        // use .handle(...) to handle until usecase and complete the stream
        Flux.generate((SynchronousSink<String> syncSink) -> syncSink.next(Faker.instance().country().name()))
            .handle((country, syncSink) -> {
                syncSink.next(country);
                if("canada".equalsIgnoreCase(country))syncSink.complete();
            })
            .subscribe(subscriber("mySubscriber"));
            
    }

    void callbackOperator(){
        Flux.range(0, 100)
            .log()
            .doFirst(() -> println("first1")) //first2 is printing before first1
            .doFirst(() -> println("first2"))
            .doOnSubscribe((subscription) -> {println("subscribe1");}) //however, subscribe1 is executing before subscribe2 in this case
            .doOnSubscribe((subscription) -> {println("subscribe2");})
            .doOnDiscard(Integer.class, (hook) -> {println("discard:" + hook);})
            .doOnComplete(() -> println("completed"))
            .take(3)
            .doOnDiscard(Integer.class, (hook) -> {println("discard:" + hook);})
            .subscribe(subscriber("mySubscriber"));
    }

    void limitRateOperator(){
        Flux.range(0, 100)
            .log()
            .limitRate(10, 5)
            .subscribe(subscriber("mySubscriber"));
    }

    void delayElementsOperator(){
        Flux.range(0, 100)
            .log()
            .delayElements(Duration.ofMillis(100)) //this has limitRate built in as it initially request 32 elements from the upstream
            .subscribe(subscriber("mySubscriber"));

        sleeper(5).run();    
    }

    void onErrorReturnOperator(){
        Flux.range(0, 10)
            .map(i -> i / (i - 5))
            .onErrorReturn(Exception.class, 100) //after emitting 100, this'll end immediately
            .subscribe(subscriber("mySubscriber"));
    }

    void onErrorResumeOperator(){
        Flux.range(0, 10)
            .map(i -> i / (i - 5))
            .onErrorResume(Exception.class, (i) -> {
                return Mono.just(20);
            })
            .subscribe(subscriber("mySubscriber"));
    }

    void onErrorContinueOperator(){
        Flux.range(0, 10)
            .map(i -> i / (i - 5))
            .onErrorContinue(Exception.class, (e,i) -> println("element: " + i + " cause an error"))
            .subscribe(subscriber("mySubscriber"));
    }

    void timeoutOperator(){
        Flux.range(0, 10)
            .handle((i,syncSink) -> {
                if(i == 5)sleeper(5).run();
                syncSink.next(i);
            })
            .timeout(Duration.ofSeconds(1), fallback()) // fallback to the 2nd publisher
            .subscribe(subscriber("mySubscriber"));
    }

    Flux<Integer> fallback(){
        return Flux.range(10,20);
    }

    void defaultIfEmptyOperator(){
        Flux.empty()
            .defaultIfEmpty(5)
            .subscribe(subscriber("mySubscriber"));
    }

    void switchIfEmptyOperator(){
        Flux.empty()
            .switchIfEmpty(fallback())
            .subscribe(subscriber("mySubscriber"));
    }

    void transformOperator(){
        Flux.range(0, 10)
            .transform(myCustomOperator()) // make a reuseable myCustomOperator
            .subscribe(subscriber("mySubscriber"));
    }

    Function<Flux<Integer>, Publisher<Integer>> myCustomOperator(){
        return (flux) -> {
                return flux.filter(i -> i > 10)
                            .map(i -> i + 1);
            };

    }

    void switchOnFirstOperator(){
        Flux.range(0, 10)
            .switchOnFirst((signal, flux) -> {
                return signal.isOnNext() && signal.get() == 0 ? Flux.range(100, 5) : flux;
            })
            .subscribe(subscriber("mySubscriber"));
    }
    

    void flatMapOperator(){

        Flux<User> users = getUsers();
        users.map(user -> getOrder(user.userId)) // this return a Flux. This's not correct
                .subscribe(subscriber("mySubscriber"));

        users.flatMap(user -> getOrder(user.userId)) // need to flatten the Flux from the above
                .subscribe(subscriber("mySubscriber2"));
    }

    void flatMapOperatorMultiThread(){
        Flux<User> users = getUsers();
        users.flatMap(user -> getOrderWithDelay(user.userId)) // notice that the return is out of order because Publisher is now working on a different thread
                .subscribe(subscriber("mySubscriber"));

        sleeper(10).run();
    }

    void concatMapOperator(){
        Flux<User> users = getUsers();
        users.concatMap(user -> getOrderWithDelay(user.userId)) // compare to the flatMapOperatorMultiThread, this one work correctly. It'll completely drain the first publisher before moving on to subsequent publisher
                .subscribe(subscriber("mySubscriber"));

        sleeper(10).run();
    
    }
    
    Map<Integer, List<PurchaseOrder>> userId2PurchaseOrders = Map.of(
        1, List.of(new PurchaseOrder("aaa",100,1), new PurchaseOrder("bbb", 200, 1)),
        2, List.of(new PurchaseOrder("ccc",300,2), new PurchaseOrder("ddd", 400, 2))
    );

    // simulate retrieve getOrder from another service
    Flux<PurchaseOrder> getOrder(int userId){
        return Flux.<PurchaseOrder>create((sink) -> {
            userId2PurchaseOrders.getOrDefault(userId, List.of()).forEach(o -> sink.next(o));
            sink.complete();
        });
    }

    // simulate above with the real world with delay. This will cause publisher to work on a different thread
    Flux<PurchaseOrder> getOrderWithDelay(int userId){
        return Flux.<PurchaseOrder>create((sink) -> {
            userId2PurchaseOrders.getOrDefault(userId, List.of()).forEach(o -> sink.next(o));
            sink.complete();
        }).delayElements(Duration.ofSeconds(1));
    }

    // simulate retrieve getUsers from another service
    Flux<User> getUsers(){
        return Flux.range(1, 2)
                .map(i -> new User(i, "user-" + i));
    }
    
}
