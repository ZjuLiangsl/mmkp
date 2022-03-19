package org.jeecg.common.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * HTML
 */
public class HTMLUtils {

    /**
     *   HTML    ，
     *
     * @param html HTML
     */
    public static String getInnerText(String html) {
        if (StringUtils.isNotBlank(html)) {
            //   html
            String content = html.replaceAll("</?[^>]+>", "");
            //
            content = content.replaceAll("(&nbsp;)+", "&nbsp;");
            //
            content = HtmlUtils.htmlUnescape(content);
            return content.trim();
        }
        return "";
    }

}
