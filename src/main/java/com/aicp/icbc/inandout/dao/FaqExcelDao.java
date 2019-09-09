package com.aicp.icbc.inandout.dao;

import com.aicp.icbc.inandout.dto.InDto;
import com.aicp.icbc.inandout.dto.OutDto;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: 吴开云
 * @Date: 2019/9/9 0009
 * @Version： 1.0
 */
@Component
public class FaqExcelDao {


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
