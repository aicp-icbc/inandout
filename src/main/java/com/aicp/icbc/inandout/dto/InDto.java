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
public class InDto {
    @ExcelProperty(value = "序号" , index = 0)
    private Integer serialNum;

    @ExcelProperty(value = "问题" , index = 1)
    private String faqQuestion;

    @ExcelProperty(value = "问题答案" , index = 2)
    private String questAnswer;

    @ExcelProperty(value = "时间" , index = 3)
    private String time;

    @ExcelProperty(value = "渠道" , index = 4)
    private String channel;
}
