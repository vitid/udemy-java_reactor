package org.example.memo;
import static org.example.util.Util.*;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class ContextDemo {
    public static void main(String[] args) {
        ContextDemo c = new ContextDemo();

        //c.useContext();

        c.rateLimiter();
    }

    void useContext(){
        welcomeFlux()
            .contextWrite(context -> context.put("user", context.get("user").toString().toUpperCase())) // we can use this to modify the existing context value
            .contextWrite(Context.of("otherKey", "xxx")) // this'll add another key to the current context
            .contextWrite(Context.of("user", "userB")) // userA is overriden to userB
            .contextWrite(Context.of("user", "userA")) // set context from downstream for upstream publisher
            .subscribe(subscriber("mySubscriber"));
    }

    Mono<String> welcomeFlux(){
        return Mono.deferContextual(context -> { //expect context setup from downstream
            println("context size: " + context.size());
            if(context.hasKey("user")){
                return Mono.just("welcome: " + context.get("user"));
            }

            return Mono.error(new RuntimeException("unauthenthicated"));
        });
    }

}
