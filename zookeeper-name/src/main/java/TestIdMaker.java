/**
 * 测试节点ID生成
 * Created by cofco on 2017/5/2.
 */
public class TestIdMaker {
    public static void main(String[] args) throws Exception {
        IdMaker idMaker=new IdMaker("localhost:2181","/NameService/IdGen","ID");
        idMaker.start();


        try {
            for (int i = 0; i < 1; i++) {
                String id = idMaker.generateId(IdMaker.RemoveMethod.DELAY);
                System.out.println(id);
            }
        } finally {
            idMaker.stop();
        }
    }
}
