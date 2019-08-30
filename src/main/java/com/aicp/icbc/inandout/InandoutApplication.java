package com.aicp.icbc.inandout;

import com.aicp.icbc.inandout.domain.InputTreating;
import com.aicp.icbc.inandout.domain.ReadAndWriteFaqExcel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@EnableCaching  //开启缓存
@SpringBootApplication
public class InandoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(InandoutApplication.class, args);
        if("F".equals(args[0].toUpperCase())){
            System.out.println("开始进行Faq测试");
            ReadAndWriteFaqExcel.run(args);
            System.out.println("Faq测试完成");
        }
        if("T".equals(args[0].toUpperCase())){
            System.out.println("开始导入机器人训练集合");
            InputTreating.run(args);
            System.out.println("机器人训练集合导入完成");
        }
    }


}
