package org.shangyu.szjsc.common.bean;

/**
 * @author zhouning
 * @date 2024/05/14 17:21
 */
public class Constants {
    /**
     * 统一的http超时时间
     */
    public static final int HTTP_TIMEOUT = 5000;

    /**
     * 登录类型
     */
    //驾驶舱
    public static final String LOGIN_FROM_SZJSC = "szjscLogin";
    //管理后台
    public static final String LOGIN_FROM_BACKEND = "backendLogin";

    /**
     * 顶级节点角色id
     */
    public static final String ROOT_ROLE_ID = "-1";
    /**
     * 建设平台视频对接参数
     * 参考文档地址:https://open.hikvision.com/docs/docId?productId=5c67f1e2f05948198c909700&version=%2Ff95e951cefc54578b523d1738f65f0a1
     */
    public static final String CAMERA_PREVIEW_APP_HOST = "101.69.216.133:444";
    public static final String CAMERA_PREVIEW_APP_KEY = "22803881";
    public static final String CAMERA_PREVIEW_APP_SECRET = "RyaxMKpZ8fOOSfGOLreg";
    /**
     * 环境数据
     */
    public static final String ENV_HOST = "http://api2.data.shangyu.gov.cn";
    public static final String ENV_APP_KEY = "A330604364597202207000005";
    public static final String ENV_APP_SECRET = "02c81e71fc2f4a63a6c3747ac3c71870";

}
