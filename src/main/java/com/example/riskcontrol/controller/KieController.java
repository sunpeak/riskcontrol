package com.example.riskcontrol.controller;

import com.example.riskcontrol.model.CodeMap;
import com.example.riskcontrol.model.RCRuntimeException;
import com.example.riskcontrol.model.Result;
import com.example.riskcontrol.service.KieService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sunpeak on 2016/8/6.
 */
@RestController
@RequestMapping("/kie")
public class KieController {

    private static Logger logger = LoggerFactory.getLogger(KieController.class);

    @Autowired
    private KieService kieService;


    @RequestMapping(value = "/addRule", method = RequestMethod.GET)
    public Result<String> addRule(String rule) {
        Result r = Result.success();
        try {
            if (StringUtils.isEmpty(rule)) {
                throw new RCRuntimeException(CodeMap.PARAM_ERROR);
            }
            kieService.addRule("rules", rule);
        } catch (RCRuntimeException e) {
            r = Result.fail();
            r.setRetCode(e.getId());
        } catch (Exception e) {
            logger.error("添加规则失败", e);
            r = Result.fail();
        }
        return r;
    }

    @RequestMapping(value = "/removeRule", method = RequestMethod.GET)
    public Result<String> removeRule(String rule) {
        Result r = Result.success();
        try {
            if (StringUtils.isEmpty(rule)) {
                throw new RCRuntimeException(CodeMap.PARAM_ERROR);
            }
            kieService.removeRule("rules", rule);
        } catch (RCRuntimeException e) {
            r = Result.fail();
            r.setRetCode(e.getId());
        } catch (Exception e) {
            logger.error("删除规则失败", e);
            r = Result.fail();
        }
        return r;
    }
}
