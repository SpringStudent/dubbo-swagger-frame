package io.github.springstudent.dubbo;

import io.github.springstudent.bean.Cheng;
import io.github.springstudent.bean.Complex;
import io.github.springstudent.bean.Generic;
import io.github.springstudent.bean.Zhou;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 周宁
 */
@DubboService
@Component
public class HelloApiServiceImpl implements HelloApiService {

    @Override
    public void complex(Cheng cheng, Zhou zhou) {
        System.out.println(cheng);
        System.out.println(zhou);
    }

    @Override
    public void complex2(Cheng cheng, String projectId, Zhou zhou) {
        System.out.println(projectId);
    }

    @Override
    public void print(String str) {
        System.out.println(str);
    }

    @Override
    public void complex3(Zhou zhou, String projectId, Integer i, Generic<Cheng> chengGeneric) {
        System.out.println(projectId);
        System.out.println(zhou);
        System.out.println(i);
        System.out.println(chengGeneric);
    }

    @Override
    public void complex5(Zhou zhou, String projectId, Cheng cheng, Integer i, Generic<Cheng> chengGeneric) {
        System.out.println(zhou);
        System.out.println(projectId);
        System.out.println(cheng);
        System.out.println(i);
        System.out.println(chengGeneric);
    }

    @Override
    public void list(List<String> l1, List<String> l2) {
        System.out.println(l1);
        System.out.println(l2);
    }

    @Override
    public void list(List<String> l1) {
        System.out.println(l1);
    }

    @Override
    public void list(List<String> ids, Map<String, Zhou> map) {
        System.out.println(ids);
        System.out.println(map);
    }

    @Override
    public void list(List<Zhou> l1, List<Cheng> l2, List<Generic<Zhou>> generics) {
        System.out.println(l1);
        System.out.println(l2);
        System.out.println(generics);
    }

    @Override
    public void complex4(String projectId, Integer i, Zhou zhou, Generic<Cheng> chengGeneric) {
        System.out.println(projectId);
        System.out.println(i);
        System.out.println(zhou);
        System.out.println(chengGeneric);
    }

    @Override
    public void multiGeneric(Generic<Cheng> chengGeneric, Generic<Zhou> zhouGeneric, List<Generic<Zhou>> generics) {
        System.out.println(chengGeneric);
        System.out.println(zhouGeneric);
        System.out.println(generics);
    }

    @Override
    public String maptest(Map<String, Generic<Cheng>> map) throws Exception {
        return map.toString();
    }

    @Override
    public void arr(Zhou[] zhou, Generic<Cheng>[] chengGeneric) {
        System.out.println(ArrayUtils.toString(zhou));
        System.out.println(chengGeneric);
    }

    @Override
    public void complex(Complex complex, List<String[]> gan) {
        System.out.println(complex);
        System.out.println(gan);
    }

    @Override
    public String[] split(List<String> message) {
        String[] result = {};
        for(String msg : message){
            result = ArrayUtils.addAll(result,msg.split(","));
        }
        return result;
    }
}
