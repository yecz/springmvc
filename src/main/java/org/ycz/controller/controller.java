package org.ycz.controller;

import org.ycz.annotation.Autowired;
import org.ycz.annotation.Controller;
import org.ycz.annotation.RequestMapping;
import org.ycz.service.TestService;

@Controller
@RequestMapping("/test")
public class controller {
    @Autowired
    private TestService testService;
    @RequestMapping("/index")
    public String index(){
        System.out.println("執行代碼");
        testService.test();
        return "hello word";
    }


}
