package org.example.util;

import java.util.function.Consumer;

public class Util {

    public static Consumer<String> printer = (x) -> System.out.println(x);

    public static Runnable sleeper(int second){
        return () -> {try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }};
    }

    public static Runnable sleeperMil(int mil){
        return () -> {try {
            Thread.sleep(mil);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }};
    }

    public static <T> DefaultSubscriber<T> subscriber(String name){
        return new DefaultSubscriber<T>(name);
    }

    public static void println(String s){
        System.out.println("[%s]%s".formatted(Thread.currentThread().getName(), s));
    }


}

