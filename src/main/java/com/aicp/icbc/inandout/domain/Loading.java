package com.aicp.icbc.inandout.domain;

import com.aicp.icbc.inandout.dao.FaqLibraryDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: 吴开云
 * @Date: 2019/8/21 0021
 * @Version： 1.0
 */
@Component
@Slf4j
@Order(value = 2)
public class Loading implements CommandLineRunner {

    @Autowired
    private FaqLibraryDao faqLibraryDao;

    @Override
    public void run(String... args) throws Exception {
//        System.out.println("程序启动模拟 ------ 准备读取接口数据");
//        System.out.println(faqLibraryDao.selectNameById(6) + "-------" + args[0]);
//        System.out.println("程序启动模拟 ------ 接口数据读取完毕");
//        if("F".equals(args[0].toUpperCase())){
//            System.out.println("开----------------------------------FAQ测试-----------------------------------始");
//            ReadAndWriteFaqExcel.run(args,faqLibraryDao);
//            System.out.println("完----------------------------------FAQ测试-----------------------------------成");
//        }
//        if("T".equals(args[0].toUpperCase())){
//            System.out.println("开----------------------------------机器人训练导入-----------------------------------始");
//            InputTreating.run(args);
//            System.out.println("完----------------------------------机器人训练导入-----------------------------------成");
//        }
            System.out.println("开----------------------------------FAQ测试-----------------------------------始");
            ReadAndWriteFaqExcel.run(args,faqLibraryDao);
            Thread.sleep(1500);
            System.out.println("完----------------------------------FAQ测试-----------------------------------成");
    }
}
