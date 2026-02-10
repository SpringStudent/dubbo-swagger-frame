package io.github.springstudent.dubbo;


import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author 周宁
 * @Date 2019-12-05 19:05
 */
public interface TestApiService {

    void test();

    void syso(List<String> list, Map<String,Object> map);

    JSONObject toJSon(Dict dict);
}
