package com.aicp.icbc.inandout.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @Description:
 * @Author: 吴开云
 * @Date: 2019/8/30 0030
 * @Version： 1.0
 */
@Data
public class OutDto{
    @ExcelProperty(value = "序号" , index = 0)
    private Integer serialNum;

    @ExcelProperty(value = "问题" , index = 1)
    private String faqQuestion;

    @ExcelProperty(value = "命中标准问" , index = 2)
    private String standardQuestion;

    @ExcelProperty(value = "回复类型" , index = 3)
    private String standardType;

    @ExcelProperty(value = "答案" , index = 4)
    private String faqAnswer;

    @ExcelProperty(value = "业务分类级别" , index = 5)
    private String businessLevel;


}
