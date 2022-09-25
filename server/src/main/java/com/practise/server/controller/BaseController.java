package com.practise.server.controller;

import com.practise.api.enums.StatusCode;
import com.practise.api.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
// 写txt文件的，用作压测 内容为userId
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
/**
 * @author HzeLng
 * @version 1.0
 * @description BaseController
 * @date 2020/12/1 14:37
 */
@Controller
@RequestMapping("base")
public class BaseController {
    private static final Logger log = LoggerFactory.getLogger(BaseController.class);

    /**
     *
     * @return 跳转到资源 welcome.jsp
     */
    @RequestMapping(value = "/welcome",method = RequestMethod.GET)
    public String intoWelcome(String usrname, ModelMap modelMap){
        usrname = "this is test para啊啊啊m upgrade1.0 sS S s asdadas  s ";
        modelMap.put("usrname",usrname);
        return "welcome";
    }

    /**
     * @RequestBody 是返回非String类型的
     * @param name
     * @return
     */
    @RequestMapping(value = "/data",method = RequestMethod.GET)
    @ResponseBody
    public String intoData(String name){
        name = "this is name from data";
        return name;
    }

    /**
     *
     * @param name 应该是浏览器Url的参数
     * @return
     */
    @RequestMapping(value = "/response",method = RequestMethod.GET)
    @ResponseBody
    public BaseResponse response(String name){
        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
        baseResponse.setData(name);
        return baseResponse;
    }

    /**
     * description:
     *  页面发生错误时，跳转至error页面
     * @return
     */
    @RequestMapping(value = "/error",method = RequestMethod.GET)
    public String intoError(){
        return "error";
    }

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello(){
        System.out.println(System.getProperty("user.dir"));
        System.out.println("hehehehehehehehello");
        return "WEB-INF/views/hello";
    }

    public static void main(String[] args) throws IOException {
        String path = "E:\\p_tool_software\\apache-jmeter-5.4\\testFiles\\useId.txt";
        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
        // 一次写一行
        for(int i=1;i<=10000;i++){
            bw.write(String.valueOf(i));
            bw.newLine();
        }
        bw.close();
        System.out.println("wrote!");
    }
}
