import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 分布式命名服务
 * Created by cofco on 2017/5/2.
 */
public class IdMaker {

    private ZkClient zkClient=null;
    //服务器地址
    private final String server;
    //父节点的路径
    private final String root;
    //节点的名称
    private final String nodeName;

    private volatile boolean running = false;
    private ExecutorService cleanExecutor = null;

    public enum RemoveMethod{
        NONE,IMMEDIATELY,DELAY
    }

    public IdMaker(String zkServer,String root,String nodeName){
        this.root = root;
        this.server = zkServer;
        this.nodeName = nodeName;
    }

    public void start() throws Exception{
        if(running){
            throw new Exception("sever is running...");
        }
        running = true;

        init();
    }

    private void init() {
        zkClient=new ZkClient(server,5000,5000,new BytesPushThroughSerializer());
        cleanExecutor= Executors.newFixedThreadPool(10);

        try {
            zkClient.createPersistent(root,true);
        } catch (ZkNodeExistsException e){
        }
    }

    public void stop() throws Exception {
        if (!running)
            throw new Exception("server has stopped...");
        running = false;

        freeResource();
    }

    private void freeResource() {
        cleanExecutor.shutdown();
        try {
            //等待线程池释放，释放完继续执行
            cleanExecutor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            cleanExecutor = null;
        }
        if (zkClient!=null){
            zkClient.close();
            zkClient=null;
        }
    }
    private void checkRunning() throws Exception {
        if (!running)
            throw new Exception("请先调用start");
    }
    private String ExtractId(String str){
        int index = str.lastIndexOf(nodeName);
        if (index >= 0){
            index+=nodeName.length();
            return index <= str.length()?str.substring(index):"";
        }
        return str;
    }

    /**
     * 产生id
     * @param removeMethod
     * @return 核心函数，产生的id
     * @throws Exception
     */
    public String generateId(RemoveMethod removeMethod) throws Exception{
        checkRunning();
        final String fullNodePath = root.concat("/").concat(nodeName);
        final String ourPath =zkClient.createPersistentSequential(fullNodePath,null);
        /**
         * 在创建完节点后为了不
         */
        if (removeMethod.equals(RemoveMethod.IMMEDIATELY)){
            zkClient.delete(ourPath);
        }else if (removeMethod.equals(RemoveMethod.DELAY)){
            cleanExecutor.execute(new Runnable() {
                public void run() {
                    zkClient.delete(ourPath);
                }
            });

        }
        //node-0000000000, node-0000000001，ExtractId提取ID
        return ExtractId(ourPath);
    }
}
