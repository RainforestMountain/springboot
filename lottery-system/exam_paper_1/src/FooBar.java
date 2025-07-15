import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FooBar {
    private int n;

    private int count = 0;
    private static final Object lock = new Object();

    public FooBar(int n) {
        this.n = n;
    }

    public void foo(Runnable printFoo) throws InterruptedException {

        for (int i = 0; i < n; i++) {
            synchronized (lock) {
                while (count % 2 != 0) {
                    lock.wait();
                }
                // printFoo.run() outputs "foo". Do not change or remove this line.
                printFoo.run();
                count++;
                lock.notifyAll();
            }
        }

    }

    public void bar(Runnable printBar) throws InterruptedException {

        for (int i = 0; i < n; i++) {
            synchronized (lock) {
                while (count % 2 == 0) {
                    lock.wait();
                }
                // printBar.run() outputs "bar". Do not change or remove this line.
                printBar.run();
                count++;
                lock.notifyAll();
            }
        }
    }


    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        final FooBar fooBar = new FooBar(n);
        Runnable printFoo = () -> {
            System.out.print("foo");
        };
        Runnable printBar = () -> {
            System.out.print("bar");
        };
        executor.submit(() -> {
            try {
                fooBar.foo(printFoo);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executor.submit(() -> {
            try {
                fooBar.bar(printBar);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();
    }
}