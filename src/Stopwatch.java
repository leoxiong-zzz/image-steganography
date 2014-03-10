/**
 * Created by Leo on 3/9/14.
 */
public class Stopwatch {

    private long start = System.nanoTime();

    public Stopwatch() {
    }

    public long getTime() {
        return System.nanoTime() - start;
    }
}
