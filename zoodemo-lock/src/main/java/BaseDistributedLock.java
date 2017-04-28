import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * 分布式锁
 * Created by cofco on 2017/4/28.
 */
public class BaseDistributedLock {

    private final ZkClient zkClient;


    //zookeeper中locker节点的路径
    private final String path;
    private final String basePath;
    private final String lockname;

    private static final Integer MAX_RETRY_COUNT = 10;
    private List<String> sortedChildren;

    BaseDistributedLock(ZkClient zkClient, String path, String lockname){
        this.zkClient=zkClient;
        this.basePath=path;
        this.path="/".concat(lockname);
        this.lockname=lockname;
    }

    private void deleteOurPath(String ourPath) throws Exception{
        zkClient.delete(ourPath);
    }


    private String createLockNode(ZkClient client,String path){

        System.out.println("create path="+path);
        return client.createEphemeralSequential(path,null);
    }

    private boolean waitToLock(long startMillis, Long millisToWait, String ourPath) throws Exception {
        boolean haveTheLock = false;
        boolean doDelete = false;

        try {
            while (!haveTheLock) {

                //获取lock节点下的所有节点
                List<String> children = getSortedChildren();
                String sequenceNodeName = ourPath.substring(basePath.length()+1);

                //获取当前节点的在所有节点列表中的位置
                int  ourIndex = children.indexOf(sequenceNodeName);
                //节点位置小于0,说明没有找到节点
                if ( ourIndex<0 ){
                    throw new ZkNoNodeException("节点没有找到: " + sequenceNodeName);
                }

                //节点位置大于0说明还有其他节点在当前的节点前面，就需要等待其他的节点都释放
                boolean isGetTheLock = ourIndex == 0;
                String  pathToWatch = isGetTheLock ? null : children.get(ourIndex - 1);

                if ( isGetTheLock ){

                    haveTheLock = true;

                }else{
                     //获取当前节点的次小的节点，并监听节点的变化
                    String  previousSequencePath = basePath .concat( "/" ) .concat( pathToWatch );
                    final CountDownLatch latch = new CountDownLatch(1);
                    final IZkDataListener previousListener=new IZkDataListener() {
                        @Override
                        public void handleDataChange(String s, Object o) throws Exception {

                        }

                        @Override
                        public void handleDataDeleted(String s) throws Exception {
                            //删除触发
                            latch.countDown();
                        }
                    };

                    //监听数据变化
                    try {
                        zkClient.subscribeDataChanges(previousSequencePath,previousListener);

                        if(millisToWait!=null){
                            millisToWait -= (System.currentTimeMillis() - startMillis);
                            startMillis = System.currentTimeMillis();
                            if ( millisToWait <= 0 ){
                                doDelete = true;    // timed out - delete our node
                                break;
                            }
                            latch.await(millisToWait, TimeUnit.MICROSECONDS);
                        }else{
                            latch.await();
                        }

                    } catch (ZkNoNodeException e ) {
//                        e.printStackTrace();
                    } finally {
                        zkClient.unsubscribeDataChanges(previousSequencePath,previousListener);
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            //发生异常需要删除节点
            doDelete = true;
            throw e;
        } finally {
            //如果需要删除节点
            if (doDelete) {
                deleteOurPath(ourPath);
            }
        }

        return false;
    }

    public List<String> getSortedChildren() {
        List<String> children = zkClient.getChildren(basePath);
        Collections.sort(children, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return getLockNodeNumber(o1,lockname).compareTo(getLockNodeNumber(o2,lockname));
            }
        });
        return children;
    }

    private String getLockNodeNumber(String str, String lockname) {
        int index = str.lastIndexOf(lockname);
        if ( index >= 0 )
        {
            index += lockname.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }

    protected void releaseLock(String lockPath) throws Exception{
        deleteOurPath(lockPath);
    }


    protected String attemptLock(long time,TimeUnit unit) throws Exception {
        final long startMillis = System.currentTimeMillis();

//        long millisToWait=(unit==null?null:unit.toMillis(time));
//        long millisToWait = ((unit!=null)?unit.toMillis(time):null);


        long millisToWait=0;

        if(unit!=null){
            millisToWait=unit.toMillis(time);
        }

        String          ourPath = null;
        boolean         hasTheLock = false;
        boolean         isDone = false;
        int             retryCount = 0;

        while (!isDone){
            ourPath = createLockNode(zkClient, path);
            try {
                waitToLock(startMillis,millisToWait,ourPath);
            } catch (ZkNoNodeException  e) {
//                e.printStackTrace();
                if ( retryCount++ < MAX_RETRY_COUNT ){
                    isDone = false;
                }else{
                    throw e;
                }
            }

        }
        if (hasTheLock){
            return ourPath;
        }

        return null;
    }
}
