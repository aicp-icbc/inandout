package com.aicp.icbc.inandout.domain;

import com.aicp.icbc.inandout.dao.FaqLibraryDao;
import com.aicp.icbc.inandout.dto.FaqLibraryDto;
import com.aicp.icbc.inandout.dto.InDto;
import com.aicp.icbc.inandout.dto.OutDto;
import com.aicp.icbc.inandout.service.FaqAsynService;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


/**
 * @author: liuxincheng01
 * @description: faq批量测试--  导入问题  ---  导出问题&答案
 * @date：Created in 2019-08-24 15:04
 * @modified By liuxincheng01
 */
@Component
public class CheckFaqWithExcel {

    @Autowired
    private FaqAsynService faqAsynService;

    @Autowired
    @Qualifier("faqTaskExecutor")
    private AsyncTaskExecutor asyncTaskExecutor;

    public  void run() {
        InputStream is;
        try {
            //输入文件路径-数据源
            String inFileName = "FAQ测试数据导入模板.xlsx";
            //获取Excel中的数据
            List<InDto> inList = getAllDtoList(inFileName);
            FaqAsynService.allSum = inList.size();
            //写出到Excel中的数据
            List<OutDto> outList = new ArrayList<>();
            String outFileName = "FAQ测试结果.xlsx";
            //获取token
            String tokenFileName = "faqtoken.txt";
            FileReader fr = new FileReader(tokenFileName);
            BufferedReader buff = new BufferedReader(fr);
            List<String> list = buff.lines().collect(Collectors.toList());
            String missTalk = list.get(2);
            Integer cycleSize = Integer.valueOf(list.get(3));
            Integer size = (inList.size() + cycleSize) / cycleSize;

            //接受 返回Future<List<OutDto>>
            List<Future<List<OutDto>>> listFuture = new ArrayList<>();

            //循环请求 -- 调用多线程
            for (Integer i = 0; i < cycleSize; i ++){
                Integer from = size * i < inList.size() ? size * i : inList.size() - 1;;
                Integer to = size * (i + 1) < inList.size() ? size * (i + 1) : inList.size();
//                System.out.println(from +"\t\t"+to);
                listFuture.add(faqAsynService.getOutDto(inList.subList(from,to)));
                if(to == inList.size()){
                    break;
                }
            }
            //判断每个线程是否全部终止 并 获取 返回值
            for (Integer j = 0; j < listFuture.size(); j ++){
                //判断是否执行完毕
                while (true){
                    if(listFuture.get(j).isDone()){
                        break;
                    }
                }
                //获取返回值
                outList.addAll(listFuture.get(j).get());
            }
            System.out.print("\r测试进度：" + 100  + "%\t" + "●●●●●●●●●●●●●●●●●●●●" + "  " + FaqAsynService.allSum+ "/" + (FaqAsynService.allSum));
            System.out.println("  导出Excel");
            System.out.println("\n" +
                    "\t\t○○○○○○○○○╭╭╮╮╮|||╭╭╭╮╮○○○○  \n" +
                    "\t\t○○○○○○○○○╰╰ ╮╮|||||╭╭ ╯╯○○○○○  \n" +
                    "\t\t○○○○○○○○○○○○○╰╮╭╯○○○○○○○○  \n" +
                    "\t\t○○◥█◣◢█◤○○○○○○╮╭○○○○○○○○○  \n" +
                    "\t\t○○○◥██◤○○○○◢█████◣○○○○○○○  \n" +
                    "\t\t○○○○◥█◣○○○◢███████◣○○○○○○  \n" +
                    "\t\t○○○○○██◣○◢███████●█◣○○○○○  \n" +
                    "\t\t○○○○○█████  I  C  B  C  ███○○○○○  \n" +
                    "\t\t○○○○○◥██████████████○○○○○  \n" +
                    "\t\t○ ﹏﹏﹏﹏~◥████████████◤﹏﹏﹏﹏○\n");
            Collections.sort(outList, new Comparator<OutDto>() {
                @Override
                public int compare(OutDto o1, OutDto o2) {
                    return o1.getSerialNum().compareTo(o2.getSerialNum());
                }
            });

            //写出Excel
//            List<OutDto> outPutList = appendDtoList(outList,inList.size(),missTalk);
            Integer insertNum = insertDtoList(outList, outFileName);
            outList.clear();
            inList.clear();
//            outPutList.clear();
        }catch (IOException e){

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //关闭线程池
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor)asyncTaskExecutor;
        executor.shutdown();
    }

    public  String post(String queryText,String token,String host) throws InterruptedException {
        String url = host + "/api/v1/core/query?version=20170407";
        OkHttpClient client = new OkHttpClient();
        Thread.sleep(100);
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
        /// 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 背景设置为红色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short)12);
        headWriteCellStyle.setWriteFont(headWriteFont);
        //只设置表头样式
        HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                new HorizontalCellStyleStrategy(headWriteCellStyle,new WriteCellStyle());

        //调用easyexcel 写入数据
        ExcelWriter excelWriter = EasyExcel.write(fileName, OutDto.class).build();
        //设值名称
        WriteSheet writeSheet = EasyExcel.writerSheet("FAQ测试").registerWriteHandler(horizontalCellStyleStrategy).build();
        //开始写入
        excelWriter.write(dtoList, writeSheet);
        /// 千万别忘记finish 会帮忙关闭流
        excelWriter.finish();
        return dtoList.size();
    }

}
