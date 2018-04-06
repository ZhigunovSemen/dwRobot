package ru.zhigunov.dwar.robot;

import java.util.*;
import java.util.stream.Stream;

public class Main {

    private static void findMaxLCString(List<String> list) {
        Optional<String> s = list.stream()
                .max((o1, o2) -> {
                    return (int) o1.chars().filter(intValue -> (intValue >= 97) && (intValue <= 122))
                            .count()
                            -
                            (int) o2.chars().filter(intValue -> (intValue >= 97) && (intValue <= 122))
                                    .count();
                });
        System.out.println(s);
    }


    public static void main(String[] args) throws Exception {
        GUI gui = new GUI("MyGUI");

//        int[] s = new int[10000000];
//        int[] ss = new int[10000000];
//        Arrays.setAll(ss, i -> i);
//        long t1 = System.nanoTime();
//        IntStream.of(s).parallel().map(i -> i*i).sum();
//        long t2 = System.nanoTime();
//        IntStream.of(s).map(i -> i*i).sum();
//        long t3 = System.nanoTime();
//        System.out.println(t2 - t1);
//        System.out.println(t3 - t2);
        Stream<String> names = Stream.of("John", "Paul", "George", "John",
                "Paul", "John");

    }

    private static void putToMapAndObserve(Map<Integer, Integer> map, Integer int1, Integer int2) {
        map.put(int1, int2);
        System.out.println(map.toString());
    }


    private static List<String> createList() {
        List<String> list = new ArrayList<>();
        String alphabet = "abcdefghijklmnopqrstuvwxyz" + "abcdefghijklmnopqrstuvwxyz".toUpperCase();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            StringBuilder s = new StringBuilder();
            int randomLen = 25 + random.nextInt(25);
            for (int j = 0; j < randomLen; j++) {
                char c = alphabet.charAt(random.nextInt(alphabet.length()));
                s.append(c);
            }
            list.add(s.toString());
        }

        return list;
    }
}
