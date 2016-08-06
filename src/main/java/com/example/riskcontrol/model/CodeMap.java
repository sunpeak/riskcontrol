package com.example.riskcontrol.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 异常码
 * Created by sunpeak on 2016/8/6.
 */
public class CodeMap {

    private static Map<String, String> codeMap = new HashMap<String, String>();

    public final static String OK = "200";

    public final static String FAIL = "300";

    public final static String PARAM_ERROR = "301";


    /*************************
     * 定义错误码及默认错误信息 需要详细错误信息可以在异常类构造中带入，例如： new
     * GavinRuntimeException(CodeMap.DELETE_ERROR,"消息不存在或者已经被删除了");
     */
    static {
        codeMap.put(CodeMap.OK, "成功");
        codeMap.put(CodeMap.FAIL, "失败");
        codeMap.put(CodeMap.PARAM_ERROR, "参数错误");

    }

    /**
     * 根据code获取相应的消息定义
     *
     * @param code 错误码
     * @return String
     */
    public static String getMsgbyCode(String code) {
        if (codeMap.containsKey(code)) {
            return codeMap.get(code);
        }
        return "";
    }
}
