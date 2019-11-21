package org.ycz.service;

import org.ycz.annotation.Service;

@Service(value = "testService")
public class TestService {

    public void test(){
        System.out.println("邏輯代碼");
    }
}
