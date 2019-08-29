package com.aicp.icbc.inandout.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.Collections;
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
            System.out.println("导入faq测试集");
            is = new FileInputStream("faqin.xls");
            HSSFWorkbook workbook = new HSSFWorkbook(is);
            HSSFSheet sheet = workbook.getSheetAt(0);
            sheet.setColumnWidth(2, 200 * 256);
            Row firstRow = sheet.getRow(0);
            Cell firstCell = firstRow.createCell(2);
            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            firstCell.setCellStyle(style);
            firstCell.setCellValue("答案");

            //获取token
            FileReader fr = new FileReader("faqtoken.txt");
            BufferedReader buff = new BufferedReader(fr);
            List<String> list = buff.lines().collect(Collectors.toList());
            String host = list.get(0);
            String token = list.get(1);

            //循环请求
            for (int row = 1; row < sheet.getLastRowNum()+1; row++) {
                Row currentRow = sheet.getRow(row);
                Cell cell = currentRow.getCell(1);
                cell.setCellType(CellType.STRING);
                String cellValue = cell.getStringCellValue();
//                System.out.println(cellValue);
                String resp = post(cellValue, token, host);
                JSONObject json = JSON.parseObject(resp);
                JSONObject data = (JSONObject) json.get("data");
                String suggestAnswer = data.getString("suggest_answer");

                 // 获取推荐问
                String resp2 = get(cellValue, token,host);
                JSONObject data2 = (JSONObject) JSON.parseObject(resp2).get("data");
                JSONArray question = new JSONArray();
                question = data2.getJSONArray("question");

                int serial = 0;
                String sqs = "";
                // if (!question.isEmpty()) {
                if (question != null && question.size() > 0) {
                    for (Object o : question) {
                        try {
                            serial++;
                            sqs += serial + "："+((JSONObject)o).getString("question")+"；";
                        }catch (Exception e){
                            e.printStackTrace();
                            System.out.println("question --------->" + question);
                        }
                    }
                }
                Cell newCell = currentRow.createCell(2);
                newCell.setCellValue(suggestAnswer+" && "+sqs);
                if ("".equals(suggestAnswer) || suggestAnswer == null) {
                    JSONObject clarifyQuestions = (JSONObject) data.get("clarify_questions");
                    JSONObject voice = (JSONObject) clarifyQuestions.get("voice");
                    String content = voice.getString("content");
                    newCell.setCellValue(content+" && "+sqs);
                }
            }
            System.out.println("导出faq测试结果");
            //输入文件路径
            FileOutputStream fos = new FileOutputStream("faqout.xls");
            workbook.write(fos);
            fos.close();
            fr.close();
            buff.close();
        } catch (Exception e) {
            System.out.println("找不到 faqin.xls 或 faqout.xls 文件 或者 faqtoken.txt文件");
            e.printStackTrace();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = "";
        try {
            str = response.body().string();
//            System.out.println(str);
        } catch (IOException e) {
            e.printStackTrace();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = "";
        try {
            str = response.body().string();
//            System.out.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

}
