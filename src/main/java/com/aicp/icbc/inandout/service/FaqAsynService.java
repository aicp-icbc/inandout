package com.aicp.icbc.inandout.service;

import com.aicp.icbc.inandout.dao.FaqLibraryDao;
import com.aicp.icbc.inandout.domain.CheckFaqWithExcel;
import com.aicp.icbc.inandout.dto.FaqLibraryDto;
import com.aicp.icbc.inandout.dto.InDto;
import com.aicp.icbc.inandout.dto.OutDto;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: 吴开云
 * @Date: 2019/9/9 0009
 * @Version： 1.0
 */
@Service
public class FaqAsynService {
    private static List<InDto> inDtoList = new ArrayList<InDto>();

    private static List<OutDto> outDtoList = new ArrayList<OutDto>();
    private static String host = "";
    private static String token = "";
    private static String missTalk = "";
    private static Integer cycleSize = 0;

    private static Integer takeSum = 0;
    public static Integer allSum = 0;

    static {
        //获取token
        String tokenFileName = "faqtoken.txt";
        FileReader fr = null;
        try {
            fr = new FileReader(tokenFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader buff = new BufferedReader(fr);
        List<String> list = buff.lines().collect(Collectors.toList());
        host = list.get(0);
        token = list.get(1);
        missTalk = list.get(2);
        cycleSize = Integer.valueOf(list.get(3));
        try {
            buff.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private FaqLibraryDao faqLibraryDao;

    @Autowired
    CheckFaqWithExcel checkFaqWithExcel;

    /**
     * 根据InList 循环请求 接口，并将返回值填充至 outList 中
     * @return
     */
    @Async("faqTaskExecutor")
    public Future<List<OutDto>> getOutDto(List<InDto> inList){
        List<OutDto> outList = new ArrayList<OutDto>();
        //循环请求
        for (int row = 0; row < inList.size(); row++) {
            //读取每一行
            try {
                String excelQuestion = inList.get(row).getFaqQuestion();
                String questAnswerBank = inList.get(row).getQuestAnswerBank();
                String staderQuestionBank = inList.get(row).getStaderQuestionBank();
                String time = inList.get(row).getTime();
                String channel = inList.get(row).getChannel();

                // System.out.println(cellValue);
                //对每一行进行请求
                String resp = "";
                JSONObject json = null;
                JSONObject data = null;
                String suggestAnswer = "";
                JSONArray confirm_questions = null;
                //如果报错则重新请求
                try {
                    resp = checkFaqWithExcel.post(excelQuestion, token, host);
                    json = JSONObject.parseObject(resp);
                    data = (JSONObject) json.get("data");
                    if(data.containsKey("suggest_answer")){
                        suggestAnswer = data.getString("suggest_answer");
                    }
                    if(data.containsKey("confirm_questions")){
                        confirm_questions = data.getJSONArray("confirm_questions");
                    }
                }catch (Exception e){
                    try {
                        resp = checkFaqWithExcel.post(excelQuestion, token, host);
                        data = (JSONObject) json.get("data");
                        if(data.containsKey("suggest_answer")){
                            suggestAnswer = data.getString("suggest_answer");
                        }
                        if(data.containsKey("confirm_questions")){
                            confirm_questions = data.getJSONArray("confirm_questions");
                        }
                    }catch (Exception e1){
                        try {
                            resp = checkFaqWithExcel.post(excelQuestion, token, host);
                            json = JSONObject.parseObject(resp);
                            data = (JSONObject) json.get("data");
                            if(data.containsKey("suggest_answer")){
                                suggestAnswer = data.getString("suggest_answer");
                            }
                            if(data.containsKey("confirm_questions")){
                                confirm_questions = data.getJSONArray("confirm_questions");
                            }
                        }catch (Exception e2){
                            try {
                                resp = checkFaqWithExcel.post(excelQuestion, token, host);
                                json = JSONObject.parseObject(resp);
                                data = (JSONObject) json.get("data");
                                if(data.containsKey("suggest_answer")){
                                    suggestAnswer = data.getString("suggest_answer");
                                }
                                if(data.containsKey("confirm_questions")){
                                    confirm_questions = data.getJSONArray("confirm_questions");
                                }
                            }catch (Exception e3){
                                try {
                                    resp = checkFaqWithExcel.post(excelQuestion, token, host);
                                    json = JSONObject.parseObject(resp);
                                    data = (JSONObject) json.get("data");
                                    if(data.containsKey("suggest_answer")){
                                        suggestAnswer = data.getString("suggest_answer");
                                    }
                                    if(data.containsKey("confirm_questions")){
                                        confirm_questions = data.getJSONArray("confirm_questions");
                                    }
                                }catch (Exception e4){
                                    resp = checkFaqWithExcel.post(excelQuestion, token, host);
                                    json = JSONObject.parseObject(resp);
                                    data = (JSONObject) json.get("data");
                                    if(data.containsKey("suggest_answer")){
                                        suggestAnswer = data.getString("suggest_answer");
                                    }
                                    if(data.containsKey("confirm_questions")){
                                        confirm_questions = data.getJSONArray("confirm_questions");
                                    }
                                }
                            }
                        }
                    }
                }

                //回复类型
                String standardType = "";

                //判断是否命中了标准问
                String standardQuestion = "未命中标准问题";
                String standardQuestionId = "";
                if(data.containsKey("answer") && data.get("answer") != null){
                    JSONObject answer = (JSONObject) data.get("answer");
                    if(answer.containsKey("standardQuestion") && answer.get("standardQuestion") != null){
                        standardQuestion = (String) answer.get("standardQuestion");
                        standardQuestionId = (String)answer.get("id");
                    }
                }

                //根据命中的标准问 生成业务级别
                String businessLevel = "-1";
                if("未命中标准问题".equals(standardQuestion)){
                    businessLevel = "-1";
                }else if(!"".equals(standardQuestionId)){
                    List<FaqLibraryDto> faqLibraryDtoList = new ArrayList<>();
                    List<Integer> faqLibIds = new ArrayList<>();
                    //在faq表中  根据标准问ID获取dir_id
                    Integer id = faqLibraryDao.selectFaqDirIdByFaqId(standardQuestionId);
                    businessLevel = id.toString();
                }

                //判断是否匹配到了推荐问题
                String sqs = "";
                if (data.containsKey("suggest_questions")) {
                    sqs = " \n推荐问题： ";
                    JSONArray questions = data.getJSONArray("suggest_questions");
                    int serial = 0;
                    if (!questions.isEmpty()) {
                        for (Object o : questions) {
                            serial++;
                            sqs += serial + "："+((JSONObject)o).getString("question")+"；";
                        }
                    }
                }
                //判断是否为澄清--修改suggestAnswer返回值
                String confirmQuestion = "";
                if(confirm_questions != null){
                    for (int i = 0; i < confirm_questions.size(); i++) {
                        JSONObject perJson = confirm_questions.getJSONObject(i);
                        if(i == 0){
                            confirmQuestion += perJson.getString("question");
                        }else {
                            confirmQuestion += "、" + perJson.getString("question");
                        }
                    }
                }

                //判断是否命中--修改suggestAnswer返回值
                if ("".equals(suggestAnswer) || suggestAnswer == null) {
                    JSONObject clarifyQuestions = (JSONObject) data.get("clarify_questions");
                    JSONObject voice = (JSONObject) clarifyQuestions.get("voice");
                    String content = voice.getString("content");
                    suggestAnswer = content;
                }

                //对对象进行赋值
                OutDto outDto = new OutDto();
                BeanUtils.copyProperties(inList.get(row), outDto);
                outDto.setSerialNum(inList.get(row).getSerialNum());
                outDto.setStandardQuestion(standardQuestion);
                outDto.setBusinessLevel(businessLevel);
                outDto.setFaqAnswer(suggestAnswer+sqs);
                //设置随机时间
//                LocalDateTime localDateTime = LocalDateTime.now();
                //设置行方给定的结果
                if(!StringUtils.isEmpty(time)){
                    LocalDateTime localDateTime = LocalDateTime.parse(time,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    outDto.setTime(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    outDto.setHour(String.valueOf(localDateTime.getHour()));
                    outDto.setMin(String.valueOf(localDateTime.getMinute()));
                }
                if(!StringUtils.isEmpty(questAnswerBank)){
                    outDto.setQuestAnswerBank(questAnswerBank);
                }
                if (!StringUtils.isEmpty(staderQuestionBank)){
                    outDto.setStaderQuestionBank(staderQuestionBank);
                }
                if(!StringUtils.isEmpty(channel)){
                    outDto.setChannel(channel);
                }
                //设置回复类型
                if(!"未命中标准问题".equals(outDto.getStandardQuestion()) && "".equals(confirmQuestion)){
                    standardType = "标准回复";
//                }else if("未命中标准问题".equals(outDto.getStandardQuestion()) && missTalk.equals(outDto.getFaqAnswer())){
                }else if("未命中标准问题".equals(outDto.getStandardQuestion()) && "".equals(confirmQuestion)){
                    standardType = "默认回复";
                }else {
                    standardType = "建议问";
                }
                outDto.setStandardType(standardType);
                outList.add(outDto);
                FaqAsynService.takeSum ++;
                //打印进度条
                String tu = "";
                Integer rowNum = row + 1;
                Integer scheduleNum = (new Double(((FaqAsynService.takeSum*1.0) / (FaqAsynService.allSum)) * 100).intValue());
                Integer j = 0;
                for (; j < scheduleNum/5; j += 1) {
                    tu += "●";
                }

                for (; j < 20; j += 1){
                    tu += "○";
                }
                System.out.print("\r整理进度：" + scheduleNum  + "%\t" + tu + "  " + FaqAsynService.takeSum+ "/" + (FaqAsynService.allSum));
//                System.out.print("\t" + Thread.currentThread().getName() + "\t" + Thread.currentThread().getId());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Future<List<OutDto>> future = new AsyncResult<List<OutDto>>(outList);
        return future;
    }
}
