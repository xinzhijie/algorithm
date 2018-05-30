package com.zsy.alg.controller;

import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static knn.KNN2.knn;

@RestController
@RequestMapping(value = "/run")
public class RunController {

    @Value("${anaconda.address}")
    private String pythonAddress;

    @PostMapping(value = "/knn")
    public List getUser(@RequestBody String trainingData, @RequestBody String testData) throws UnsupportedEncodingException {
        trainingData = URLDecoder.decode(trainingData,"UTF-8");
        trainingData = trainingData.substring(trainingData.indexOf("["),trainingData.indexOf("testData")-2);

        testData = URLDecoder.decode(testData,"UTF-8");
        testData = testData.substring(testData.lastIndexOf("["),testData.lastIndexOf("]")+1);

        String result = "";
        try {
            result = knn(pythonAddress, trainingData, testData );
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONArray resultArray = JSONArray.fromObject(result);
        List list = new ArrayList();
        if(resultArray.size()>0) {
            for (int i = 0; i < resultArray.size(); i++) {

                for (String str:((String)resultArray.get(i)).split(" ")) {
                    list.add(str);
                }
            }
        }
        return list;
    }
}
