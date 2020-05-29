package io.tava.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-05-28 15:16
 */
@SpringBootApplication(scanBasePackages = {"io.tava"})
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

}
