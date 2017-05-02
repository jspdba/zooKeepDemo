import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by cofco on 2017/4/28.
 */
    public class SimpleDistributedLockMutex implements DistributedLock {
        private final ZkClient zkClient;
        private final String path;

        private final BaseDistributedLock baseDistributedLock;

        private static final String LOCK_NAME="LOCK";

        private String lockPath;

        public SimpleDistributedLockMutex(ZkClient zkClient,String path){
            this.zkClient=zkClient;
            this.path=path;
            this.baseDistributedLock = new BaseDistributedLock(zkClient,path,LOCK_NAME);
        }

    @Override
    public void acquire() throws Exception {
        System.out.println("acquire");
        try {
            this.lockPath = baseDistributedLock.attemptLock(0,null);
        } catch (ZkNoNodeException e) {
            //父节点不存在
            if(!zkClient.exists(path)) {
                zkClient.createPersistent(path, true);
            }else{
                throw e;
            }
        }
    }

    @Override
    public boolean acquire(long time, TimeUnit unit) throws Exception {
        System.out.println("acquire time");

        try {
            this.lockPath = baseDistributedLock.attemptLock(time,unit);
        } catch (ZkNoNodeException e) {
            //父节点不存在
            if(!zkClient.exists(path)) {
                zkClient.createPersistent(path, true);
            }else{
                e.printStackTrace();
                throw e;
            }
        }
        return true;
    }

    @Override
    public void release() throws Exception {
        System.out.println("release");
        try {
            if(!StringUtils.isBlank(this.lockPath)){
                baseDistributedLock.releaseLock(this.lockPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
