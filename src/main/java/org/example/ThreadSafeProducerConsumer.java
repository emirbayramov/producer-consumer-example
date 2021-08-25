package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadSafeProducerConsumer {
    static final Logger logger = LogManager.getLogger(ThreadSafeProducerConsumer.class);

    private int maxWriteCycleLength=100;
    private int maxRandomNumber=100;

    public List<Integer> randoms = new LinkedList<>();


    public ThreadSafeProducerConsumer() {
        File propsFile = new File("app.conf");

        if (propsFile.exists()) {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(propsFile));
            } catch (IOException e) {
                props.put("maxWriteCycleLength", "100");
                props.put("maxRandomNumber", "100");
            }
            maxWriteCycleLength = Integer.parseInt(props.getProperty("maxWriteCycleLength"));
            maxRandomNumber = Integer.parseInt(props.getProperty("maxRandomNumber"));
        }

    }

    public void startThreads() {
        PRODUCER.start();
        CONSUMER.start();
    }

    public void join(){
        try {
            PRODUCER.join();
            CONSUMER.join();
        } catch (InterruptedException e) {
        }
    }

    private final Thread PRODUCER = new Thread(() ->
    {
        Random random = new Random(System.currentTimeMillis());
        while (true) {
            int length = random.nextInt(maxWriteCycleLength);
            synchronized (randoms) {
                for (int i = 0; i < length; i++) {
                    int n = random.nextInt(maxRandomNumber);
                    randoms.add(n);
                    logger.info("Added " + n + " to list");
                }
            }
            logger.info("Writing cycle ended");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    });

    // Поток для чтения данных. Читает данные из списка Randoms и выводит их консоль
    private final Thread CONSUMER = new Thread(() -> {
        while (true) {
            synchronized (randoms) {
                while(randoms.size()>0) {
                    logger.info("Read and removed " + randoms.remove(0) + " from list");
                }
                logger.info("Read cycle ended");
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    });

    public static void main(String[] args){

        ThreadSafeProducerConsumer threads = new ThreadSafeProducerConsumer();
        threads.startThreads();
        threads.join();
    }
}