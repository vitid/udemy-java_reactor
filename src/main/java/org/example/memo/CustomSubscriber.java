package org.example.memo;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;


public class CustomSubscriber {
    public static void main(String[] args) {

        AtomicReference<Subscription> mySubscription = new AtomicReference<>();

        Flux.range(1, 20)
                .log()
                .subscribeWith(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        mySubscription.set(subscription);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println("received: " + integer);
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("completed");
                    }
                });

        mySubscription.get().request(3);
        mySubscription.get().request(2);

        mySubscription.get().cancel();

        mySubscription.get().request(3);

    }
}