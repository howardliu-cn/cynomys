package cn.howardliu.monitor.cynomys.agent.handler;//package com.wfj.monitor.net.handler;
//
//import com.wfj.monitor.net.operator.AbstractMonitorDataOperator;
//import com.wfj.monitor.net.operator.SocketMonitorDataOperator;
//
//import java.util.Random;
//import java.util.concurrent.TimeUnit;
//
///**
// * <br>created at 17-3-31
// *
// * @author liuxh
// * @since 1.0.0
// */
//public class Client {
//    public static void main(String[] args) throws InterruptedException {
//        SocketMonitorDataOperator handler = new SocketMonitorDataOperator("127.0.0.1", 12345);
//        handler.start();
//        Random random = new Random(System.currentTimeMillis());
//        for (int i = 0; i < 1000; i++) {
//            if(handler.isActive()) {
//                handler.sendData(("Client Message: " + System.currentTimeMillis()).getBytes());
//                TimeUnit.SECONDS.sleep(random.nextInt(20));
//            }
//        }
//    }
//}
