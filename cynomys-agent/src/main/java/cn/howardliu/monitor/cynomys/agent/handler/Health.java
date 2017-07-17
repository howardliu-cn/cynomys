/**
 * @Probject Name: netty-wfj-base-dev
 * @Path: com.wfj.netty.monitor.infcHeart.java
 * @Create By Jack
 * @Create In 2015年8月25日 上午11:48:34
 */
package cn.howardliu.monitor.cynomys.agent.handler;

/**
 * @Class Name Heart
 * @Author Jack
 * @Create In 2015年8月25日
 */
public interface Health {

    /**
     * 重新连接监控服务器
     *
     * @param status 系统状态
     * @Methods Name restartHealth
     * @Create In 2015年8月25日 By Jack
     */
    public void restartHealth(String status);

    /**
     * 启动服务监控，后续更新请使用用updateHealth
     *
     * @param status 被监控的系统信息，例如："Active"
     * @Methods Name startHealth
     * @Create In 2015年8月25日 By Jack
     */
    public void startHealth(String status);

    /**
     * 更新系统状态
     *
     * @param status 系统状态
     * @Methods Name shutdownHealth
     * @Create In 2015年8月25日 By Jack
     */
    public void shutdownHealth(String status);

    /**
     * 设置Tomcat的应用端口号
     *
     * @param port void
     * @Methods Name setListensePort
     * @Create In 2016年3月24日 By Jack
     */
    public void setListensePort(Integer port);

    public Thread getM();

    public void setIsMonitorStopBoolean(Boolean isMonitorStopBoolean);

    public Long getSessionId();

    public void setSessionId(Long sessionId);

    public byte[] getSessionPassword();

    public void setSessionPassword(byte[] sessionPassword);
}
