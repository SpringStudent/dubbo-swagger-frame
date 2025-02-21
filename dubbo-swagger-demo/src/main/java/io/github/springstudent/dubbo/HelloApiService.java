package io.github.springstudent.dubbo;

import cn.hutool.core.bean.BeanDesc;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.json.JSONObject;
import io.github.springstudent.bean.*;
import io.github.springstudent.third.bean.Tuple2;

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

    void list(List<Zhou> l1, List<Cheng> l2, List<Generic<Zhou>> generics);

    void multiGeneric(Generic<Cheng> chengGeneric, Generic<Zhou> zhouGeneric, List<Generic<Zhou>> generics);

    String maptest(Map<String, Generic<Cheng>> map) throws Exception;

    void arr(Zhou[] zhou, Generic<Cheng>[] chengGeneric);

    void complex(Complex complex, List<String[]> gan);

    String[] split(List<String> message);

    Map<String, Object> map(Generic<Generic<Generic<Zhou>>> me);

    Pair<String, Complex> tuple(Pair<Map<String, String>, Generic<String>> pair, List<String> strs);

    MyBoy myBoy(MyBoy myBoy,Generic<MyBoy> generic);

    JSONObject json(Tree<String> treeNode, MyBoy myBoy);
}
