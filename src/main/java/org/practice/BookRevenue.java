package org.practice;

import static org.example.util.Util.*;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javafaker.Faker;

import reactor.core.publisher.Flux;

public class BookRevenue {
    record Book(String category, int price){};
    public static void main(String[] args) {
        BookRevenue b = new BookRevenue();
        b.test();
    }

    void test(){
        Set<String> categories = Set.of("Science fiction", "Fantasy", "Suspense/Thriller");
        bookFlux()
            .buffer(5)
            .map( (books) -> {
                return books.stream().filter(book -> categories.contains(book.category())).map(book -> book.price()).collect(Collectors.summingInt(Integer::intValue));
            })
            .subscribe(subscriber("mySubscriber"));

        sleeper(5).run();    
    }

    Flux<Book> bookFlux(){
        return Flux.interval(Duration.ofMillis(100))
                   .map(l -> new Book(Faker.instance().book().genre(), Faker.instance().random().nextInt(1, 100)));
    }
}
