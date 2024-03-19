package io.github.springstudent.dubbo;

import io.github.springstudent.bean.Cheng;
import io.github.springstudent.bean.Complex;
import io.github.springstudent.bean.Generic;
import io.github.springstudent.bean.Zhou;

import java.util.List;
import java.util.Map;

/**
 * @author 周宁
 */
public interface HelloApiService {

    void complex(Cheng cheng, Zhou zhou);

    void complex2(Cheng cheng, String projectId, Zhou zhou);

    void complex3(Zhou zhou, String projectId, Integer i, Generic<Cheng> chengGeneric);

    void complex4(String projectId, Integer i, Zhou zhou, Generic<Cheng> chengGeneric);

    void complex5(Zhou zhou, String projectId, Cheng cheng, Integer i, Generic<Cheng> chengGeneric);

    void print(String str);

    void list(List<String> l1, List<String> l2);

    void list(List<String> l1);

    void list(List<String> ids, Map<String, Zhou> map);

    void list(List<Zhou> l1,List<Cheng> l2,List<Generic<Zhou>> generics);

    void multiGeneric(Generic<Cheng> chengGeneric,Generic<Zhou> zhouGeneric,List<Generic<Zhou>> generics);

    String maptest(Map<String,Generic<Cheng>> map)throws Exception;

    void arr(Zhou[] zhou,Generic<Cheng>[] chengGeneric);

    void complex(Complex complex,List<String[]> gan);

    String[] split(List<String> message);

}
