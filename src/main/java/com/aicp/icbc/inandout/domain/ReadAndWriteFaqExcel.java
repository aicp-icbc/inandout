package com.aicp.icbc.inandout.domain;

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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author: liuxincheng01
 * @description: faq批量测试--  导入问题  ---  导出问题&答案
 * @date：Created in 2019-08-24 15:04
 * @modified By liuxincheng01
 */
public class ReadAndWriteFaqExcel {
    public static void run(String[] args) {
        InputStream is;
        try {
            //输入文件路径-数据源
            String inFileName = "faqin.xlsx";
            //获取Excel中的数据
            List<InDto> inList = getAllDtoList(inFileName);
            //写出到Excel中的数据
            List<OutDto> outList = new ArrayList<>();
            String outFileName = "faqout.xlsx";
            //获取token
            String tokenFileName = "faqtoken.txt";
            FileReader fr = new FileReader(tokenFileName);
            BufferedReader buff = new BufferedReader(fr);
            List<String> list = buff.lines().collect(Collectors.toList());
            String host = list.get(0);
            String token = list.get(1);



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
                    OutDto outDto = new OutDto();
                    BeanUtils.copyProperties(inList.get(row), outDto);

                    //判断是否命中--修改suggestAnswer返回值
                    if ("".equals(suggestAnswer) || suggestAnswer == null) {
                        JSONObject clarifyQuestions = (JSONObject) data.get("clarify_questions");
                        JSONObject voice = (JSONObject) clarifyQuestions.get("voice");
                        String content = voice.getString("content");
                        suggestAnswer = content;
                    }
                    outDto.setFaqAnswer(suggestAnswer+sqs);
                    outList.add(outDto);

                    //打印进度条
                    String tu = "*";
                    Integer rowNum = row + 1;
                    Integer scheduleNum = (new Double(((outList.size()*1.0) / (inList.size())) * 100).intValue());
                    for (Integer j = 0 ; j < scheduleNum/10; j += 1) {
                        tu += "*";
                    }
                    if(rowNum == inList.size()){
                        System.out.print("\r接口访问进度：" + 100  + "%\t" + tu + "\t" + inList.size() + "/" + inList.size());
                    }else {
                        System.out.print("\r接口访问进度：" + scheduleNum  + "%\t" + tu + "\t" + outList.size() + "/" + inList.size());
                    }
                }catch (Exception e){

                }
            }
            inList.clear();
            //写出Excel
            System.out.println("开始导出Excel");
            Integer insertNum = insertDtoList(outList, outFileName);

        } catch (Exception e) {
            System.out.println("找不到所需文件");
        }
    }

    public static String post(String queryText,String token,String host){
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
        try {
            response = client.newCall(request).execute();
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
    
    public static String get(String question, String token, String host) {
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
    public static List<InDto> getAllDtoList(String fileName) {
        List<InDto> infoDtoList = new ArrayList<>();
        //调用easyexcel 访问数据
        //初始化监听器
        AnalysisEventListener<InDto> listener = new AnalysisEventListener<InDto>() {
            //访问，每一行数据
            @Override
            public void invoke(InDto object, AnalysisContext context) {
                // System.err.println("Row:" + context.getCurrentRowNum() + "  Data:" + object);
                if(object!=null && object.getSerialNum()!=null && object.getFaqQuestion() != null){
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
