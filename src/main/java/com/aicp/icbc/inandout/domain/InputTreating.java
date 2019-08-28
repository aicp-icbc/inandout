package com.aicp.icbc.inandout.domain;

import okhttp3.*;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: liuxincheng01
 * @description: 聚类灌入会话中控接口-待标注-机器人训练
 * @date：Created in 2019-08-22 18:09
 * @modified By liuxincheng01
 */
public class InputTreating {
    public static void run(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("teating.txt")),
                    "UTF-8"));

            //获取token
            FileReader fr = new FileReader("treatingtoken.txt");
            BufferedReader buff = new BufferedReader(fr);
            List<String> list = buff.lines().collect(Collectors.toList());
            String host = list.get(0);
            String token = list.get(1);

            String lineTxt = null;
            while ((lineTxt = br.readLine()) != null) {
                if (StringUtils.startsWithIgnoreCase(lineTxt, "AGENT")) {
//                    System.out.println("agent:" + lineTxt.substring(6));
                    post(lineTxt.substring(6), token, host);
                } else {
//                    System.out.println("user:" + lineTxt.substring(5));
                    post(lineTxt.substring(5), token, host);
                }
            }
            br.close();
            buff.close();
            fr.close();
        } catch (Exception e) {
            System.out.println("找不到 teating.txt 文件 或者 treatingtoken.txt");
        }

    }

    public static String post(String queryText,String token,String host){
        String url = host + "/api/v1/core/query?version=20170407";
        OkHttpClient client = new OkHttpClient();

        HttpUrl httpUrl = HttpUrl.parse(url).newBuilder()
                .addQueryParameter("version", "20170407")
                .build();
        String json = "{\"query_text\":\"" + queryText + "\",\"session_id\":\"1234567890\"}";

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
}
