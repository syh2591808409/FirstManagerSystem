package com.first.project.system.controller;

import com.first.project.common.domain.FirstConstant;
import com.first.project.common.domain.FirstResponse;
import com.first.project.common.exception.FirstException;
import com.first.project.common.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@Slf4j
@Validated
@RestController
public class WeatherController {
    @GetMapping("weather")
    public FirstResponse queryWeather(@NotBlank(message = "{required}")String areaId ) throws FirstException {
        try {
            String data = HttpUtil.sendPost(FirstConstant.MEIZU_WEATHER_URL, "cityIds=" + areaId);
            return new FirstResponse().data(data);
        } catch (Exception e) {
            String message = "天气查询失败";
            log.error(message, e);
            throw new FirstException(message);
        }
    }
}
