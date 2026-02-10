package io.github.springstudent.dubbo;

import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson.JSONObject;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

    @Override
    public void syso(List<String> list, Map<String, Object> map) {
        System.out.println("list=" + list);
        System.out.println("map=" + map);
    }

    @Override
    public JSONObject toJSon(Dict dict) {
        return (JSONObject) JSONObject.toJSON(dict);
    }
}
