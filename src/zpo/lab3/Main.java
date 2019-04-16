package zpo.lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.MAX_PRIORITY;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1 -> Wątki");
            System.out.println("2 -> Pula wątków");
            System.out.println("3 -> Stream API");
            System.out.println("4 -> Koniec programu");
            System.out.println("Wybierz opcje: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    threads();
                    break;
                case 2:
                    threadPool();
                    break;
                case 3:
                    streamAPI();
                    break;
                case 4:
                    System.exit(0);
            }
        }
    }

    public static List<Item> createItemList() {
        List<Item> itemList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            itemList.add(new Item());
        }

        return itemList;
    }

    public static void threads() {
        List<Item> itemList = createItemList();

        final int PRODUCING_THREADS_NUMBER = 4;
        final int CONSUMING_THREADS_NUMBER = 3;

        List<Thread> threadList = new ArrayList<>();

        AtomicInteger atomicInteger = new AtomicInteger(0);
        for (int i = 0; i < PRODUCING_THREADS_NUMBER; i++) {
            threadList.add(new Thread(() -> {
                for (int j = atomicInteger.getAndIncrement(); j < itemList.size(); j += PRODUCING_THREADS_NUMBER) {
                    itemList.get(j).produceMe();
                }
            }));
        }

        AtomicInteger atomicInteger2 = new AtomicInteger(0);
        for (int i = 0; i < CONSUMING_THREADS_NUMBER; i++) {
            threadList.add(new Thread(() -> {
                for (int j = atomicInteger2.getAndIncrement(); j < itemList.size(); j += CONSUMING_THREADS_NUMBER) {
                    itemList.get(j).consumeMe();
                }
            }));
        }

        for (Thread thread : threadList) {
            thread.start();
        }

        long startTime = System.currentTimeMillis();

        try {
            for (Thread thread : threadList) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time: " + estimatedTime + "ms");
    }

    public static void threadPool() {

        List<Item> itemList = createItemList();
        final int THREADS_NUMBER = 7;

        ExecutorService executorService = Executors.newFixedThreadPool(THREADS_NUMBER);
        //ExecutorService executorService = Executors.newCachedThreadPool();
        //ExecutorService executorService = Executors.newSingleThreadExecutor();

        long startTime = System.currentTimeMillis();
        itemList.stream().forEach(item -> {
            executorService.execute(() -> {
                item.produceMe();
                item.consumeMe();
            });
        });

        executorService.shutdown();
        try {
            executorService.awaitTermination(MAX_PRIORITY, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time: " + estimatedTime + "ms");
    }

    public static void streamAPI() {
        List<Item> itemList = createItemList();
        long startTime = System.currentTimeMillis();

        itemList.parallelStream().forEach(item -> {
            item.produceMe();
            item.consumeMe();
        });

        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time: " + estimatedTime + "ms");
    }

}
