import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Foo {
    public static final Foo foo;

    static {
        try {
            foo = new Foo();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 记录当前执行阶段（1→2→3）
     */
    private int step = 1;
    /**
     * 用于同步的对象锁，所有任务必须获取同一把锁
     */
    private static final Object lock = new Object();

    public Foo() throws InterruptedException {

    }

    public void first(Runnable printFirst) throws InterruptedException {
        //通过synchronized(lock)获取锁
//        synchronized (lock) {
//            //使用while(step != 1)而非if，防止虚假唤醒
//            while (step != 1) {
//                //条件不满足时调用wait()释放锁并进入等待状态
//                lock.wait();
//            }
//            // printFirst.run() outputs "first". Do not change or remove this line.
//            // 执行任务
//            printFirst.run();
//            //任务完成后将step设为 2，表示轮到任务 B 执行
//            step++;
//            //调用notifyAll()唤醒所有等待的线程
//            lock.notifyAll();
//        }
        printFirst.run();
    }

    public void second(Runnable printSecond) throws InterruptedException {
//        synchronized (lock) {
//            while (step != 2) {
//                lock.wait();
//            }
//            // printSecond.run() outputs "second". Do not change or remove this line.
//            printSecond.run();
//            step++;
//            lock.notifyAll();
//        }
        printSecond.run();
    }

    public void third(Runnable printThird) throws InterruptedException {
//        synchronized (lock) {
//            while (step != 3) {
//                lock.wait();
//            }
//            // printThird.run() outputs "third". Do not change or remove this line.
//            printThird.run();
//            step++;
//            lock.notifyAll();
//        }
        printThird.run();

    }

    public static void main(String[] args) throws InterruptedException {
        //ExecutorService executor = Executors.newFixedThreadPool(3);
        Runnable printFirst = () -> System.out.println("first");
        Runnable printSecond = () -> System.out.println("second");
        Runnable printThird = () -> System.out.println("third");
        Thread ta = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    foo.first(printFirst);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Thread tb = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    foo.second(printSecond);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread tc = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    foo.third(printThird);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        tc.start();
        tb.start();
        ta.start();

    }
}