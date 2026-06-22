package com.bazaartracker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bazaartracker.service.BazaarCollector;

@SpringBootApplication
public class BazaarTrackerApplication implements CommandLineRunner {

    public static void main(String[] args) {

        SpringApplication.run(
                BazaarTrackerApplication.class,
                args
        );
    }

    @Override
    public void run(String... args) {

        BazaarCollector collector =
                new BazaarCollector();

        collector.start();

        System.out.println(
                "Bazaar Collector started. Collecting every 5 minutes..."
        );
    }
}