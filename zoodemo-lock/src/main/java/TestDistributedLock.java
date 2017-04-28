import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

/**
 *
 * 测试分布式锁
 * Created by cofco on 2017/4/28.
 */
public class TestDistributedLock {
    public static void main(String[] args) {
        ZkClient zkClient1=new ZkClient("localhost:2181",5000,5000,new BytesPushThroughSerializer());
        final SimpleDistributedLockMutex mutex1 = new SimpleDistributedLockMutex(zkClient1, "/Mutex");

        ZkClient zkClient2=new ZkClient("localhost:2181",5000,5000,new BytesPushThroughSerializer());
        final SimpleDistributedLockMutex mutex2 = new SimpleDistributedLockMutex(zkClient2, "/Mutex");

        try {
            mutex1.acquire();
            System.out.println("Client1 locked");
            Thread client2Thd = new Thread(new Runnable() {

                public void run() {
                    try {
                        mutex2.acquire();
                        System.out.println("Client2 locked");
                        mutex2.release();
                        System.out.println("Client2 released lock");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            client2Thd.start();
            Thread.sleep(5000);
            mutex1.release();
            System.out.println("Client1 released lock");

            client2Thd.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
