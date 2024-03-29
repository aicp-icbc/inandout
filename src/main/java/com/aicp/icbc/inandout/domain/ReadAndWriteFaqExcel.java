package com.aicp.icbc.inandout.domain;

import com.aicp.icbc.inandout.dao.FaqLibraryDao;
import com.aicp.icbc.inandout.dto.FaqLibraryDto;
import com.aicp.icbc.inandout.dto.InDto;
import com.aicp.icbc.inandout.dto.OutDto;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author: liuxincheng01
 * @description: faq批量测试--  导入问题  ---  导出问题&答案
 * @date：Created in 2019-08-24 15:04
 * @modified By liuxincheng01
 */
@Component
public class ReadAndWriteFaqExcel {
//    @Autowired
//    private static FaqLibraryDao faqLibraryDao;


    public  void run(String[] args,FaqLibraryDao faqLibraryDao) {
        InputStream is;
        try {
            //输入文件路径-数据源
            String inFileName = "FAQ测试数据导入模板.xlsx";
            //获取Excel中的数据
            List<InDto> inList = getAllDtoList(inFileName);
            //写出到Excel中的数据
            List<OutDto> outList = new ArrayList<>();
            String outFileName = "FAQ测试结果.xlsx";
            //获取token
            String tokenFileName = "faqtoken.txt";
            FileReader fr = new FileReader(tokenFileName);
            BufferedReader buff = new BufferedReader(fr);
            List<String> list = buff.lines().collect(Collectors.toList());
            String host = list.get(0);
            String token = list.get(1);
            String missTalk = list.get(2);


            //循环请求
            for (int row = 0; row < inList.size(); row++) {
                //读取每一行
                try {
                    String cellValue = inList.get(row).getFaqQuestion();
                    // System.out.println(cellValue);
                    //对每一行进行请求
                    String resp = post(cellValue, token, host);
                    JSONObject json = JSON.parseObject(resp);
                    JSONObject data = (JSONObject) json.get("data");
                    String suggestAnswer = data.getString("suggest_answer");
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
                    String businessLevel = "";
                    if("未命中标准问题".equals(standardQuestion)){
                        businessLevel = "该问题无法匹配到具体业务级别";
                    }else if(!"".equals(standardQuestionId)){
                        List<FaqLibraryDto> faqLibraryDtoList = new ArrayList<>();
                        List<Integer> faqLibIds = new ArrayList<>();
                        //在faq表中  根据标准问ID获取dir_id
                        Integer id = faqLibraryDao.selectFaqDirIdByFaqId(standardQuestionId);
                        faqLibIds.add(id);
                        //由最下层dir_id 在 faq_library 表中 获取 parent_id，并保存每一层的id直到最顶层
                        while (faqLibraryDao.selectParentIdById(id) != 0){
                            id = faqLibraryDao.selectParentIdById(id);
                            faqLibIds.add(id);
                        }
                        //反序list 在 faq_library 表 根据 id 获取 name，并追加至 业务级别字段中
                        Collections.reverse(faqLibIds);
                        for (Integer i = 0; i < faqLibIds.size(); i ++){
                            if(i == 0){
                                businessLevel += faqLibraryDao.selectNameById(faqLibIds.get(i));
                            }else {
                                businessLevel += "|" + faqLibraryDao.selectNameById(faqLibIds.get(i));
                            }
                        }
//                        System.out.println("\n"+faqLibIds + "\t"+ standardQuestion +"\t"+ businessLevel);
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
                    outDto.setSerialNum(row + 1);
                    outDto.setStandardQuestion(standardQuestion);
                    outDto.setBusinessLevel(businessLevel);
                    outDto.setFaqAnswer(suggestAnswer+sqs);
                    //设置回复类型
                    if(!"未命中标准问题".equals(outDto.getStandardQuestion())){
                        standardType = "标准回复";
                    }else if("未命中标准问题".equals(outDto.getStandardQuestion()) && missTalk.equals(outDto.getStandardQuestion())){
                        standardType = "默认回复";
                    }else {
                        standardType = "澄清";
                    }

                    outList.add(outDto);

                    //打印进度条
                    String tu = "*";
                    Integer rowNum = row + 1;
                    Integer scheduleNum = (new Double(((outList.size()*1.0) / (inList.size())) * 100).intValue());
                    Integer j = 0;
                    for (; j < scheduleNum/10; j += 1) {
                        tu += "***";
                    }
                    for (; j < 10; j += 1){
                        tu += "---";
                    }

                    if(rowNum == inList.size()){
                        System.out.print("\r接口访问进度：" + 100  + "%\t" + tu + "\t" + inList.size() + "/" + inList.size());
                    }else {
                        System.out.print("\r接口访问进度：" + scheduleNum  + "%\t" + tu + "\t" + outList.size() + "/" + inList.size());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            //写出Excel
            System.out.println("\t开始导出Excel");
            List<OutDto> outPutList = appendDtoList(outList,inList.size(),missTalk);
            Integer insertNum = insertDtoList(outPutList, outFileName);
            outList.clear();
            inList.clear();
            outPutList.clear();
        }catch (IOException e){

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  String post(String queryText,String token,String host){
        String url = host + "/api/v1/core/query?version=20170407";
        OkHttpClient client = new OkHttpClient();
        HttpUrl httpUrl = HttpUrl.parse(url).newBuilder()
                .addQueryParameter("version", "20170407")
                .build();
        String json = "{\"query_text\":\"" + queryText + "\",\"session_id\":\"\"}";

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);

        Request request = new Request
                .Builder()
                .post(body)
                .url(httpUrl)
                .addHeader("Authorization", "AICP "+ token)
                .build();
        Response response = null;
        while (true){
            try {
                response = client.newCall(request).execute();
            } catch (Exception e) {
            }
            if (response != null && response.body() != null){
                break;
            }
        }
        String str = "";
        try {
            str = response.body().string();
//            System.out.println(str);
        } catch (Exception e) {
        }
        return str;
    }

    //根据标准问获取业务级别列表
    public static String getQas(String question, String token, String host) {
        String url = host + "api/v1/qas/standard_suggestion?version=20171010&question="+question+"&ps=5";
        Request request = new Request.Builder().url(url)
                .addHeader("Authorization", "AICP "+ token)
                .get().build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (Exception e) {
        }
        String str = "";
        try {
            str = response.body().string();
//            System.out.println(str);
        } catch (Exception e) {
        }
        return str;
    }
    
    public  String get(String question, String token, String host) {
        String url = host + "/api/v1/qas/standard_suggestion?version=20171010&question="+question+"&ps=5";
        Request request = new Request.Builder().url(url)
                .addHeader("Authorization", "AICP "+ token)
                .get().build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (Exception e) {
        }
        String str = "";
        try {
            str = response.body().string();
//            System.out.println(str);
        } catch (Exception e) {
        }
        return str;
    }

    /**
     * 获取所有Excel中所有的list
     * @return
     */
    public  List<InDto> getAllDtoList(String fileName) {
        List<InDto> infoDtoList = new ArrayList<>();
        //调用easyexcel 访问数据
        //初始化监听器
        AnalysisEventListener<InDto> listener = new AnalysisEventListener<InDto>() {
            //访问，每一行数据
            @Override
            public void invoke(InDto object, AnalysisContext context) {
                // System.err.println("Row:" + context.getCurrentRowNum() + "  Data:" + object);
                if(object!=null && object.getFaqQuestion() != null){
                    infoDtoList.add(object);
                }
            }
            //完成访问所有数据
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                //System.out.println(fileName + " 数据读取完毕，共读取： " + infoDtoList.size() + " 条数据");
            }
        };
        //生成ExcelReader
        ExcelReader excelReader = EasyExcel.read(fileName, InDto.class, listener).build();;
        // 第一个参数表示sheet页，从0开始，第二个设值表头行数，默认从1开始
        ReadSheet readSheet = EasyExcel.readSheet(0).headRowNumber(1).build();
        excelReader.read(readSheet);
        // 这里千万别忘记关闭，读的时候会创建临时文件，到时磁盘会崩的
        excelReader.finish();
        return infoDtoList;
    }


    /**
     * 计算召回率
     * @param dtoList
     * @return
     */
    public  List<OutDto> appendDtoList(List<OutDto> dtoList, Integer insize, String missTalk){
        ArrayList<OutDto> dtoArrayList = new ArrayList<>();
        Integer missingNum = 0;
        Integer clarifyNum = 0;
        Integer totalNum = dtoList.size();
        Integer normalNum = 0;
        for(OutDto perDto:dtoList){
            if("未命中标准问题".equals(perDto.getStandardQuestion())){
                if(missTalk.equals(perDto.getFaqAnswer())){
                    //未命中问题--非召回
                    missingNum ++;
                }else {
                    //未命中问题--澄清
                    clarifyNum ++;
                }
            }else {
                normalNum ++;
            }
        }
        OutDto outDto1 = new OutDto();
//        outDto1.setSerialNum(totalNum + 1);
        outDto1.setFaqQuestion("FAQ总数：");
        outDto1.setStandardQuestion(insize.toString());
        outDto1.setFaqAnswer("成功测试FAQ数：");
        outDto1.setBusinessLevel(totalNum.toString());

        OutDto outDto2 = new OutDto();
//        outDto2.setSerialNum(totalNum + 2);
        outDto2.setFaqQuestion("FAQ召回数：");
        outDto2.setStandardQuestion(String.valueOf(totalNum - missingNum));
        outDto2.setFaqAnswer("FAQ召回率：");
        outDto2.setBusinessLevel(String.valueOf((totalNum - missingNum)*1.0 / totalNum));

        OutDto outDto3 = new OutDto();
//        outDto3.setSerialNum(totalNum + 3);
        outDto3.setFaqQuestion( "FAQ澄清数：");
        outDto3.setStandardQuestion(String.valueOf(clarifyNum));
        outDto3.setFaqAnswer("FAQ澄清率：");
        outDto3.setBusinessLevel(String.valueOf((clarifyNum * 1.0) / totalNum));

        OutDto outDto4 = new OutDto();

        dtoArrayList.add(outDto1);
        dtoArrayList.add(outDto2);
        dtoArrayList.add(outDto3);
        dtoArrayList.add(outDto4);

        dtoArrayList.addAll(dtoList);
        return dtoArrayList;
    }

    /**
     * 写入DTOlist
     * @param dtoList
     * @return
     */
    public static Integer insertDtoList(List<OutDto> dtoList, String fileName){
        //调用easyexcel 写入数据
        ExcelWriter excelWriter = EasyExcel.write(fileName, OutDto.class).build();
        //设值名称
        WriteSheet writeSheet = EasyExcel.writerSheet("FAQ测试").build();
        //开始写入
        excelWriter.write(dtoList, writeSheet);
        /// 千万别忘记finish 会帮忙关闭流
        excelWriter.finish();
        return dtoList.size();
    }

}
