import org.I0Itec.zkclient.ExceptionUtil;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 简单的分布式队列
 * 队列保存数据大小有限制(缺点)
 * Created by cofco on 2017/4/28.
 */
public class DistributedSimpleQueue<T> {
    protected final ZkClient zkClient;//用于操作zookeeper集群
    protected final String root;//代表根节点

    protected static final String Node_NAME = "n_";//顺序节点的名称

    public DistributedSimpleQueue(ZkClient zkClient, String root) {
        this.zkClient = zkClient;
        this.root = root;
    }

    public int size(){
        return zkClient.getChildren(root).size();
    }
    //判断队列是否为空
    public boolean isEmpty() {
        return zkClient.getChildren(root).size() == 0;
    }

    /**
     * 向队列提供数据
     * @param element
     * @return
     * @throws Exception
     */
    public boolean offer(T element) throws Exception{
        //构建数据节点的完整路径
        String nodeFullPath = root .concat( "/" ).concat( Node_NAME );

        //创建持久的节点，写入数据
        try {
            zkClient.createPersistentSequential(nodeFullPath , element);
        } catch (ZkNoNodeException e) {
//            e.printStackTrace();

            zkClient.createPersistent(root);
            offer(element);
        } catch (Exception  e) {
            throw ExceptionUtil.convertToRuntimeException(e);
        }

        return true;
    }

    //从队列取数据
    public T poll() throws Exception {

        try {
            List<String> list = zkClient.getChildren(root);

            if (list.size() == 0) {
                return null;
            }

            //将队列安装由小到大的顺序排序
            Collections.sort(list, new Comparator<String>() {
                public int compare(String lhs, String rhs) {
                    return getNodeNumber(lhs, Node_NAME).compareTo(getNodeNumber(rhs, Node_NAME));
                }
            });

//        将队列中的元素做循环，然后构建完整的路径，在通过这个路径去读取数据
            for ( String nodeName : list ){
                String nodeFullPath = root.concat("/").concat(nodeName);
                try {
                    T node = (T) zkClient.readData(nodeFullPath);
                    zkClient.delete(nodeFullPath);
                    return node;
                } catch (ZkNoNodeException e) {
                    // ignore
                }
            }
            return null;

        } catch (Exception e) {
            throw ExceptionUtil.convertToRuntimeException(e);
        }

    }

    private String getNodeNumber(String str, String nodeName) {
        int index = str.lastIndexOf(nodeName);
        if (index >= 0) {
            index += Node_NAME.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;

    }
}
