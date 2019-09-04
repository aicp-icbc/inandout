package com.aicp.icbc.inandout;


import com.aicp.icbc.inandout.domain.InputTreating;
import com.aicp.icbc.inandout.domain.ReadAndWriteFaqExcel;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching  //开启缓存
@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan("com.aicp.icbc.inandout.dao")
public class InandoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(InandoutApplication.class, args);
//        ApplicationContext context = SpringApplication.run(InandoutApplication.class);
//        System.out.println(context.getBean("FaqLibraryDao"));
//        if("F".equals(args[0].toUpperCase())){
//            System.out.println("开--------------FAQ测试---------------始");
//            ReadAndWriteFaqExcel.run(args);
//            System.out.println("完--------------FAQ测试---------------成");
//        }
//        if("T".equals(args[0].toUpperCase())){
//            System.out.println("开--------------机器人训练导入---------------始");
//            InputTreating.run(args);
//            System.out.println("完--------------机器人训练导入---------------成");
//        }
    }


}
