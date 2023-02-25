package org.example.util;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class DefaultSubscriber<T> implements Subscriber<T> {

    String name;
    Subscription subscription;

    DefaultSubscriber(String name){this.name = name;}

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        s.request(Integer.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        System.out.println("[" + name + "]" + t.toString());
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("[" + name + "]" + "error:" + t.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("[" + name + "]" + "completed");
    }
    
}
