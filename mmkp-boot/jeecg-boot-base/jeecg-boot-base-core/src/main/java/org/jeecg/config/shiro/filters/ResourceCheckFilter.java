package org.jeecg.config.shiro.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Scott
 * @create 2019-02-01 15:56
 * @desc     URL       
 */
@Slf4j
public class ResourceCheckFilter extends AccessControlFilter {

    private String errorUrl;

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    /**
     *          ，        true，  false；
     *
     * @param servletRequest
     * @param servletResponse
     * @param o                                mappedValue    [urls]           
     * @return
     * @throws Exception
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        Subject subject = getSubject(servletRequest, servletResponse);
        String url = getPathWithinApplication(servletRequest);
        log.info("          url => " + url);
        return subject.isPermitted(url);
    }

    /**
     * onAccessDenied：               ；      true         ；      false
     *              ，       。
     *
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        log.info("  isAccessAllowed    false    ，     method onAccessDenied ");

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.sendRedirect(request.getContextPath() + this.errorUrl);

        //    false       ，        ，            （        ）
        return false;
    }

}