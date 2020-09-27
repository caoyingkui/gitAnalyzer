package git.util;

/**
 * Created by kvirus on 2019/8/8 22:53
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class TimeTool {
    public static long last;

    public static long reset() {
        last = System.currentTimeMillis();
        return last;
    }

    public static long elapse() {
        long temp = last;
        last = System.currentTimeMillis();
        return last - temp;
    }

    public static long elapse(long start) {
        return System.currentTimeMillis() - start;
    }
}
