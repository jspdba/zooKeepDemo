package com.wuchaofei.zk;

import com.wuchaofei.zk.util.JacksonUtil;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import java.util.List;

/**
 * 管理服务器
 * Created by cofco on 2017/4/27.
 */
public class ManagerServer {
    private String serversPath;
    private String commandPath;
    private String configPath;
    private ZkClient zkClient;
    private ServerConfig config;

    //用于监听server子节点变化
    private IZkChildListener childListener;
    private IZkDataListener dataListener;

    //工作服务器列表
    private List<String> workServerList;

    public ManagerServer(String serversPath,String commandPath,String configPath,ZkClient zkClient,ServerConfig config){
        this.serversPath=serversPath;
        this.commandPath=commandPath;
        this.configPath=configPath;
        this.config=config;
        this.zkClient=zkClient;

        //用于监听zookeeper中servers节点的子节点列表变化
        this.childListener=new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                workServerList=currentChilds;
                System.out.println("work server list change,current list is :"+workServerList.toString());
            }
        };

        //用于监听zookeeper中command节点的数据变化
        this.dataListener=new IZkDataListener() {
            @Override
            public void handleDataChange(String datapath, Object data) throws Exception {
                String cmd = new String((byte[]) data);
                System.out.println("cmd:"+cmd);
                exeCmd(cmd);
            }


            @Override
            public void handleDataDeleted(String s) throws Exception {
            }
        };

    }

    private void exeCmd(String cmdType) {
        if ("list".equals(cmdType)) {
            System.out.println(workServerList.toString());
        } else if ("create".equals(cmdType)) {
            execCreate();
        } else if ("modify".equals(cmdType)) {
            execModify();
        } else {
            System.out.println("error command!" + cmdType);
        }
    }

    private void execModify() {
        config.setDbUser(config.getDbUser()+"_modified");
        try {
            zkClient.writeData(configPath,JacksonUtil.toJson(config).getBytes());
        }catch (ZkNoNodeException e){
            execCreate();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //创建configPath节点
    private void execCreate() {
        if(!zkClient.exists(configPath)){
            try {
                zkClient.createPersistent(configPath, JacksonUtil.toJson(config).getBytes());
            } catch (ZkNodeExistsException e) {
                //节点已经存在异常，直接写入数据
                zkClient.writeData(configPath,JacksonUtil.toJson(config).getBytes());
            } catch (ZkNoNodeException e){
                //表示其中的一个节点的父节点还没有被创建
                String parentDir = configPath.substring(0,configPath.lastIndexOf('/'));
                zkClient.createPersistent(parentDir, true);
                execCreate();
            }
        }
    }

    public void start(){
        initRunning();
    }

    private void initRunning() {
        //执行订阅command节点数据变化和servers节点的列表变化
        zkClient.subscribeDataChanges(commandPath,dataListener);
        zkClient.subscribeChildChanges(serversPath,childListener);
    }

    public void stop(){
        ///取消订阅command节点数据变化和servers节点的列表变化
        zkClient.unsubscribeDataChanges(commandPath,dataListener);
        zkClient.unsubscribeChildChanges(serversPath,childListener);
    }

}
