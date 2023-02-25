package org.practice;

import static org.example.util.Util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class FileReaderService {
    public static void main(String[] args) {
        FileReaderService f = new FileReaderService();
        f.test();
    }

    void test() {


        Flux.generate(() -> {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("test.txt");
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);

            return br;
        }, (br, syncSink) -> {
            try {
                println("read line...");
                String line = br.readLine();
                if (line == null){
                    syncSink.complete();
                } else {
                    syncSink.next(line);
                }
            } catch (IOException e) {
                syncSink.error(e);
            }
            return br;
        }, (br) -> {
            try {
                println("close resource...");
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        })
        //.take(3)
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe(subscriber("mySubscriber"));

        sleeper(10).run();
    }
}
