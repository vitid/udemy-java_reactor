package org.practice;

import static org.example.util.Util.*;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.publisher.Flux;

public class StockObserver {
    public static void main(String[] args) {
        StockObserver stockObserver = new StockObserver();
        stockObserver.test();
    }

    StockObserver(){

    }

    void test(){
        AtomicInteger price = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);
        
        price.set(100);
        Flux.interval(Duration.ofMillis(100))
            .map(s -> {
                Random r = new Random();
                if(r.nextBoolean()) {price.set(price.get() - 5);}
                else{ price.set(price.get() + 5);}
                
                return price.get();
            })
            .log()
            .subscribeWith(new Subscriber<Integer>() {

                Subscription subscription;
                @Override
                public void onSubscribe(Subscription s) {
                    subscription = s;
                    s.request(Integer.MAX_VALUE);
                }

                @Override
                public void onNext(Integer p) {
                    System.out.println(Thread.currentThread().getName() + ":" + p);
                    if(p < 80 || p > 120) {
                        subscription.cancel();
                        latch.countDown();
                    };
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("Error:" + t.getMessage());
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    System.out.println("Completed!!!");
                    latch.countDown();
                }
                
            });

        try {
            latch.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
