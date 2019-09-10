package com.aicp.icbc.inandout.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * @Description:
 * @Author: 吴开云
 * @Date: 2019/8/30 0030
 * @Version： 1.0
 */
@Data
@HeadRowHeight(20)
public class OutDto{

    @ColumnWidth(6)
    @ExcelProperty(value = "序号" , index = 0)
    private Integer serialNum;

    @ColumnWidth(25)
    @ExcelProperty(value = "测试问法" , index = 1)
    private String faqQuestion;

    @ColumnWidth(30)
    @ExcelProperty(value = "触发的标准问题或建议问" , index = 2)
    private String standardQuestion;

    @ColumnWidth(15)
    @ExcelProperty(value = "返回结果类型" , index = 3)
    private String standardType;

    @ColumnWidth(60)
    @ExcelProperty(value = "返回答案" , index = 4)
    private String faqAnswer;

    @ColumnWidth(60)
    @ExcelProperty(value = "业务分类级别" , index = 5)
    private String businessLevel;


}
