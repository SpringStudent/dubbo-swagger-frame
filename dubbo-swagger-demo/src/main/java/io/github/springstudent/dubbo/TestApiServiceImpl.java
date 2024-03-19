package io.github.springstudent.dubbo;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

/**
 * @author 周宁
 * @Date 2019-12-05 19:05
 */
@DubboService
@Component
public class TestApiServiceImpl implements TestApiService {
    @Override
    public void test() {
        System.out.println("test()");
    }
}
