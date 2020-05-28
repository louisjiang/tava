package io.tava.web.controller;

import io.tava.util.HotThreads;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-05-28 15:05
 */
@RestController()
@RequestMapping("/tava")
public class ApiRestController {


    @RequestMapping(value = "/hotThreads", produces = "text/plain;charset=UTF-8")
    public String hotThreads(HotThreads hotThreads) throws Exception {
        return hotThreads.detect();
    }

}
