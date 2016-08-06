package com.example.riskcontrol.controller;

import com.example.riskcontrol.model.CodeMap;
import com.example.riskcontrol.model.Event;
import com.example.riskcontrol.model.RCRuntimeException;
import com.example.riskcontrol.model.Result;
import com.example.riskcontrol.service.ConfigService;
import com.example.riskcontrol.service.KieService;
import com.example.riskcontrol.util.EventFactory;
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
@RequestMapping("/riskcontrol")
public class RiskController {

    private static Logger logger = LoggerFactory.getLogger(RiskController.class);

    @Autowired
    private KieService kieService;

    @Autowired
    private ConfigService configService;


    @RequestMapping(value = "/req", method = RequestMethod.GET)
    public Result<String> req(String json) {
        Result r = Result.success();
        try {
            if (StringUtils.isEmpty(json)) {
                throw new RCRuntimeException(CodeMap.PARAM_ERROR);
            }

            Event event = EventFactory.build(json);
            if ("ON".equals(configService.query("SWITCH_RC"))) {
                kieService.execute(event);
            }
            r.setData(event);
        } catch (RCRuntimeException e) {
            r = Result.fail();
            r.setRetCode(e.getId());
        } catch (Exception e) {
            logger.error("业务风控分析失败", e);
            r = Result.fail();
        }
        return r;
    }
}
