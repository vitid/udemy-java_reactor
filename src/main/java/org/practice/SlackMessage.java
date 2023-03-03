package org.practice;

import static org.example.util.Util.*;

import java.util.HashMap;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SlackMessage {
    class SlackMember{

        String name;
        Map<String, SlackRoom> roomName2SlackRoom = new HashMap<>();

        SlackMember(String name){
            this.name = name;
        }

        void registerRoom(SlackRoom slackRoom){
            roomName2SlackRoom.put(slackRoom.name, slackRoom);
        }

        void postMessage(String roomName, String message){
            roomName2SlackRoom.get(roomName).publishMessage(message);
        }
    }

    class SlackRoom{
        
        Sinks.Many<String> sink;
        Flux<String> flux;
        String name;
        SlackRoom(String name){
            this.name = name;
            sink = Sinks.many().multicast().onBackpressureBuffer();
            flux = sink.asFlux();
        }

        void addMember(SlackMember member){
            flux.subscribe(subscriber(member.name));
            member.registerRoom(this);
        }

        void publishMessage(String message){
            sink.tryEmitNext(name + ": " + message);
        }
    }

    public static void main(String[] args) {
        SlackMessage sm = new SlackMessage();

        sm.test();
    }

    void test(){
        SlackRoom room0 = new SlackRoom("room0");
        SlackRoom room1 = new SlackRoom("room1");

        SlackMember memberA = new SlackMember("memberA");
        SlackMember memberB = new SlackMember("memberB");
        SlackMember memberC = new SlackMember("memberC");

        room0.addMember(memberA);
        room0.addMember(memberB);
        room0.addMember(memberC);

        room1.addMember(memberA);
        room1.addMember(memberC);

        memberA.postMessage("room0", "hello");
        memberB.postMessage("room0", "hi");

        memberC.postMessage("room1", "xxx");

        memberC.postMessage("room0", "yyy");
    }
}
