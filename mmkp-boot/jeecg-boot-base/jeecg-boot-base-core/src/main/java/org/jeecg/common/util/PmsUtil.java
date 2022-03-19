package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class PmsUtil {


    private static String uploadPath;

    @Value("${jeecg.path.upload}")
    public void setUploadPath(String uploadPath) {
        PmsUtil.uploadPath = uploadPath;
    }

    public static String saveErrorTxtByList(List<String> msg, String name) {
        Date d = new Date();
        String saveDir = "logs" + File.separator + DateUtils.yyyyMMdd.get().format(d) + File.separator;
        String saveFullDir = uploadPath + File.separator + saveDir;

        File saveFile = new File(saveFullDir);
        if (!saveFile.exists()) {
            saveFile.mkdirs();
        }
        name += DateUtils.yyyymmddhhmmss.get().format(d) + Math.round(Math.random() * 10000);
        String saveFilePath = saveFullDir + name + ".txt";

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(saveFilePath));
            for (String s : msg) {
                if (s.indexOf("_") > 0) {
                    String arr[] = s.split("_");
                    bw.write("the" + arr[0] + "row:" + arr[1]);
                } else {
                    bw.write(s);
                }
                //bw.newLine();
                bw.write("\r\n");
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            log.info("excel import Exception:" + e.getMessage());
        }
        return saveDir + name + ".txt";
    }

}
