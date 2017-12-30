package athena;

import com.athena.idalloc.client.IdallocClient;
import org.junit.Test;

/**
 * Created by wangjialong on 12/7/17.
 */
public class testIdallocClient {

    private IdallocClient idallocClient;

    public void setUp() {
        idallocClient = new IdallocClient();
    }

    @Test
    public void test1() {
        try {
            Long result = idallocClient.requestIdAlloc();
            System.out.print("server response: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
