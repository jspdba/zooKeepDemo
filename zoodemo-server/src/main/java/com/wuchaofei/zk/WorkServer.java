package com.wuchaofei.zk;

import com.wuchaofei.zk.util.JacksonUtil;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

/**
 * 代表工作服务器
 * Created by cofco on 2017/4/27.
 */
public class WorkServer {
    private String serversPath;
    private String configPath;
    private ZkClient zkClient;
    private ServerConfig serverConfig;
    private ServerData serverData;

    //数据监听器
    private IZkDataListener dataListener;

    public WorkServer(String configPath,String serversPath,ServerData serverData,ZkClient zkClient, ServerConfig initconfig){
        this.configPath=configPath;
        this.serversPath=serversPath;
        this.serverData=serverData;
        this.zkClient=zkClient;
        this.serverConfig=initconfig;

        this.dataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                String retJson=new String((byte[])data);
                ServerConfig serverConfigLocal = JacksonUtil.toObject(retJson,ServerConfig.class);
                //更新配置
                updateConfig(serverConfigLocal);
                System.out.println("new workserver config is:"+serverConfigLocal.toString());
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {

            }
        };

    }
    /**
     * 当监听到zookeeper中config节点的配置信息改变时，要读取配置信息来更新自己的配置信息
     */
    private void updateConfig(ServerConfig serverConfig){
        this.serverConfig = serverConfig;
    }


    /**
     * 服务的启动
     */
    public void start(){
        System.out.println("work server start...");
        initRunning();
    }

    /**
     * 服务器的初始化
     */
    private void initRunning(){
        registMeToZookeeper();
        //订阅config节点的改变
        zkClient.subscribeDataChanges(configPath,dataListener);
    }

    private void registMeToZookeeper() {
        String path = serversPath.concat("/").concat(serverData.getAddress());
        try {
            zkClient.createEphemeral(path,JacksonUtil.toJson(serverConfig));
        }catch (ZkNoNodeException e){
             //父节点不存在
            zkClient.createPersistent(serversPath,true);
            registMeToZookeeper();
        }
    }

    /**
     * 服务的停止
     */
    public void stop(){
        System.out.println("work server stop...");
        //取消监听
        zkClient.unsubscribeDataChanges(configPath, dataListener);
    }
}
