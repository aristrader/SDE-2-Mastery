package org.example.scratch;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        final Map<String, String> countryCodeMap;
        final Map<String,String> countryMap = new HashMap<>();

        countryMap.put("a","b");

        countryCodeMap = Collections.unmodifiableMap(countryMap);

        System.out.println(countryCodeMap.getOrDefault(null,"THIS IS THE DEFAULT"));

        Thread t1 = new Thread(() -> {
            try {
                System.out.println("Thread 1 is running");
                Thread.sleep(2000); // Simulate some work
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Finally block of Thread 1");
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                System.out.println("Thread 2 is running");
                Thread.sleep(3000); // Simulate some work
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Finally block of Thread 2");
            }
        });



        try {
            // Wait for threads to complete
            t1.start();
            t2.start();

        } finally {
            System.out.println("Main thread moved forward.");
        }

        System.out.println("Main thread finished");

        try {
            t1.join();
            t2.join();
        } catch (Exception e){
            System.out.println("Found a bug");
        }

//        optionalExperimenting();
//
//        System.out.println(Resulting.obj.getA());
//        System.out.println(Resulting.obj.getB());
//
//        Resulting.obj.setA("ASDD");
//        Resulting.obj.setB("ASDE");
//
//        System.out.println(Resulting.obj.getA());
//        System.out.println(Resulting.obj.getB());
//
//        Resulting.obj.setA("xcz");
//        Resulting.obj.setB("xzc");
//
//        System.out.println(Resulting.obj.getA());
//        System.out.println(Resulting.obj.getB());

//        Resulting.obj = new Resulting();


//        optionalExperimenting();
//        stringBuilderExperiment();

//        final ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
//        map.remove(null);
//        map.put("THIS IS KEY",null);
//        Object xyz = map.getValue("THIS IS THE KEY");
//        System.out.println((String) xyz);

//        System.out.println(extractValue(null));
//        System.out.println(extractValue("abc"));
//        System.out.println(extractValue("abc_def"));
//        System.out.println(extractValue("abc_def.ghi_jkl"));

//        System.out.println(StringUtils.isEmpty(null));
    }

    private static String extractValue(String input) {
        if (input == null || input.isEmpty()) {
            return ""; // or return null, etc. based on the requirements
        }

        String[] parts = input.split("_");
        if (parts.length >= 2) {
            return String.join("_", Arrays.copyOfRange(parts, 1, parts.length));
        } else {
            return ""; // or return null, etc. based on the requirements
        }
    }

    private static void optionalExperimenting(){
        //        Optional<Resulting> obj = Optional.empty();
        Optional<Resulting> obj = Optional.of(new Resulting());
        if(obj.isPresent()){
            Resulting obj1 = obj.get();
            Resulting obj2 = obj.get();
            System.out.println(obj1.getA());
            System.out.println(obj1.getB());
            System.out.println(obj2.getA());
            System.out.println(obj2.getB());
        }
//        Resulting obj1 = obj.getValue();
//        if(obj1 != null){
//            System.out.println(obj1.getA());
//            System.out.println(obj1.getB());
//        }
        if(obj.isPresent()){
            System.out.println("TRUEEE");
        } else {
            System.out.println("False");
        }
    }

    private static void stringBuilderExperiment(){

        String transactionId = "TRANSACTIONID";
        String folder = "FOLDER";
        String tag = "TAG";

        LocalDateTime now = LocalDateTime.now();

        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("api/")
                .append(folder)
                .append("/")
                .append(now.getYear())
                .append("/")
                .append(String.format("%02d", now.getMonth().getValue()))
                .append("/")
                .append(String.format("%02d", now.getDayOfMonth()))
                .append("/")
                .append(String.format("%02d", now.getHour()))
                .append("/")
                .append(tag)
                .append("_")
                .append(transactionId)
                .append(".json");

        String key1 = keyBuilder.toString();

        String key2 = "api/" + folder + "/" +now.getYear() + "/" + String.format("%02d", now.getMonth().getValue()) + "/"
                + String.format("%02d", now.getDayOfMonth()) + "/" + String.format("%02d", now.getHour()) + "/" +
                tag + "_" + transactionId + ".json";

        System.out.println(key1.equals(key2));
        System.out.println(key1);
        System.out.println(key2);
    }
}

