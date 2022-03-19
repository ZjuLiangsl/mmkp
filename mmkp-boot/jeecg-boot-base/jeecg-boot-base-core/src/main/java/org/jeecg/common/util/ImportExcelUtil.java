package org.jeecg.common.util;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 */
@Slf4j
public class ImportExcelUtil {

    public static Result<?> imporReturnRes(int errorLines,int successLines,List<String> errorMessage) throws IOException {
        if (errorLines == 0) {
            return Result.ok(" " + successLines + "         ！");
        } else {
            JSONObject result = new JSONObject(5);
            int totalCount = successLines + errorLines;
            result.put("totalCount", totalCount);
            result.put("errorCount", errorLines);
            result.put("successCount", successLines);
            result.put("msg", "     ：" + totalCount + "，     ：" + successLines + "，    ：" + errorLines);
            String fileUrl = PmsUtil.saveErrorTxtByList(errorMessage, "userImportExcelErrorLog");
            int lastIndex = fileUrl.lastIndexOf(File.separator);
            String fileName = fileUrl.substring(lastIndex + 1);
            result.put("fileUrl", "/sys/common/static/" + fileUrl);
            result.put("fileName", fileName);
            Result res = Result.ok(result);
            res.setCode(201);
            res.setMessage("      ，    。");
            return res;
        }
    }

    public static List<String> importDateSave(List<Object> list, Class serviceClass,List<String> errorMessage,String errorFlag)  {
        IService bean =(IService) SpringContextUtils.getBean(serviceClass);
        for (int i = 0; i < list.size(); i++) {
            try {
                boolean save = bean.save(list.get(i));
                if(!save){
                    throw new Exception(errorFlag);
                }
            } catch (Exception e) {
                String message = e.getMessage().toLowerCase();
                int lineNumber = i + 1;
                //
                if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_ROLE_CODE)) {
                    errorMessage.add("  " + lineNumber + "  ：        ，    。");
                } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_JOB_CLASS_NAME)) {
                    errorMessage.add("  " + lineNumber + "  ：        ，    。");
                }else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_CODE)) {
                    errorMessage.add("  " + lineNumber + "  ：        ，    。");
                }else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE)) {
                    errorMessage.add("  " + lineNumber + "  ：        ，    。");
                }else {
                    errorMessage.add("  " + lineNumber + "  ：    ，    ");
                    log.error(e.getMessage(), e);
                }
            }
        }
        return errorMessage;
    }

    public static List<String> importDateSaveOne(Object obj, Class serviceClass,List<String> errorMessage,int i,String errorFlag)  {
        IService bean =(IService) SpringContextUtils.getBean(serviceClass);
        try {
            boolean save = bean.save(obj);
            if(!save){
                throw new Exception(errorFlag);
            }
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            int lineNumber = i + 1;
            //
            if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_ROLE_CODE)) {
                errorMessage.add("  " + lineNumber + "  ：        ，    。");
            } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_JOB_CLASS_NAME)) {
                errorMessage.add("  " + lineNumber + "  ：        ，    。");
            }else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_CODE)) {
                errorMessage.add("  " + lineNumber + "  ：        ，    。");
            }else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE)) {
                errorMessage.add("  " + lineNumber + "  ：        ，    。");
            }else {
                errorMessage.add("  " + lineNumber + "  ：    ，    ");
                log.error(e.getMessage(), e);
            }
        }
        return errorMessage;
    }
}
