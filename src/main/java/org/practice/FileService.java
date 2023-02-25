package org.practice;

import reactor.core.publisher.Mono;

import java.io.*;

public class FileService {

    public static void main(String[] args) {
        FileService fs = new FileService();
        fs.test();
    }

    public FileService(){

    }

    public void test(){
        readFileMono("test.txt").subscribe((s) -> System.out.println(s),
                             (err) -> System.out.println(err.getMessage()));

        createFileMono("/tmp/write2.txt").subscribe((x) -> {},
                                                       (err) -> System.out.println(err.getMessage()),
                                                       () -> System.out.println("completed"));
    }

    Mono<String> readFileMono(String fileName) {
        return Mono.fromSupplier(() -> readFile(fileName));
    }

    String readFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        try(
                InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);
        ){
            String line;

            while((line = br.readLine()) != null){
                sb.append(line).append("\n");
            }

            return sb.toString();
        } catch (Exception e){
            throw new RuntimeException("can't read file");
        }
    }

    Mono<Void> createFileMono(String filePath){
        return Mono.fromRunnable(() -> createFile(filePath));
    }

    void createFile(String filePath) {
        try {
            File myFile = new File(filePath);
            if(!myFile.createNewFile()) throw new RuntimeException("file already exists");
        } catch (IOException ex){
            throw new RuntimeException("can't create file");
        }
    }

}
