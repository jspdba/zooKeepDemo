package com.wuchaofei.zk;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 订阅者客户端
 * Created by cofco on 2017/4/27.
 */
public class SubscribeZkClient {

    //限制客户端数量
    private static final int CLIENT_LIMIT=3;
    private static final String ZK_SERVERS="localhost:2181,localhost:2182,localhost:2183";

    //节点的路径
    private static final String  CONFIG_PATH = "/config";//配置节点
    private static final String  COMMAND_PATH = "/command";//命令节点
    private static final String  SERVERS_PATH = "/servers";//服务器列表节点

    public static void main(String[] args) {
        //clientt列表
        List<ZkClient> zkCLients = new ArrayList<ZkClient>();

        //用来存储所有的workservers（代表工作服务器）
        List<WorkServer>  workServers = new ArrayList<WorkServer>();

        ManagerServer manageServer = null;

        try {

            ServerConfig initConfig = new ServerConfig();
            initConfig.setDbUser("root");
            initConfig.setDbPwd("wuchaofei");
            initConfig.setDbUrl("jdbc:mysql://localhost:3306/mydb");

            //client管理服务器
//        ZkClient clientManager=new ZkClient(ZK_SERVERS,5000,5000,new BytesPushThroughSerializer());

//        manageServer = new ManagerServer(SERVERS_PATH, COMMAND_PATH,CONFIG_PATH,clientManage,initConfig);
//        manageServer.start();

            for (int i = 0; i < CLIENT_LIMIT; i++) {
                ZkClient zkClient=new ZkClient(ZK_SERVERS,5000,5000,new BytesPushThroughSerializer());
                zkCLients.add(zkClient);

                ServerData serverData = new ServerData();
                serverData.setId(i);
                serverData.setName("#workserver#"+i);
                serverData.setAddress("localhost:208"+(i+1));

                WorkServer  workServer = new WorkServer(CONFIG_PATH, SERVERS_PATH, serverData, zkClient, initConfig);
                workServers.add(workServer);
                workServer.start();
            }

            System.out.println("敲回车键退出！\n");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
