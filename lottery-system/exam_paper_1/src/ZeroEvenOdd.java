import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntConsumer;

/**
 * 这次使用原子类
 * zero, odd, zero, even
 * ACAB ACAB
 */
public class ZeroEvenOdd {

//    private static final AtomicBoolean zero_step = new AtomicBoolean(true);
//    private static final AtomicBoolean odd_even_step = new AtomicBoolean(true);

//    private boolean isZero = true;
//    private static final Object lock = new Object();
//    private int num = 1;
//    private int current = 0;
//
//    public ZeroEvenOdd(int n) {
//        this.n = n;
//    }
//
//    // printNumber.accept(x) outputs "x", where x is an integer.
//    public void zero(IntConsumer printNumber) throws InterruptedException {
////        for (int i = 0; i < n; i++) {
////            while (!zero_step.get()) {//不是true等待
////                Thread.yield();
////            }
////            printNumber.accept(0);
////            zero_step.set(false);
////        }
//
//        for (int i = 0; i < n; i++) {
//            synchronized (lock) {
//                while (!isZero) {
//                    lock.wait();
//                }
//                printNumber.accept(0);
//                isZero = false;
//                lock.notifyAll();
//            }
//            while (i >= n) {
//                return;
//            }
//        }
//    }
//
//    public void even(IntConsumer printNumber) throws InterruptedException {
////        int m = n / 2;
////        for (int i = 0; i < m; i++) {
////            while (zero_step.get() && odd_even_step.get()) { //zero = false , odd_even = false 才执行
////                Thread.yield();
////            }
////            printNumber.accept(num++);
////            zero_step.set(true);
////            odd_even_step.set(true);
////        }
//
//        while (current < n) {
//            synchronized (lock) {
//                while (isZero || num % 2 != 0) {
//                    lock.wait();
//                }
//                printNumber.accept(num);
//                isZero = true;
//                num++;
//                current++;
//                lock.notifyAll();
//                while (current >= n) {
//                    return;
//                }
//            }
//        }
//    }
//
//    public void odd(IntConsumer printNumber) throws InterruptedException {
////        int m = (n % 2 == 0 ? n / 2 : n / 2 + 1);
////        for (int i = 0; i < m; i++) {
////            //zero = false , odd_even = true 才执行
////            while (zero_step.get() && !odd_even_step.get()) {
////                Thread.yield();
////            }
////            printNumber.accept(num++);
////            zero_step.set(true);
////            odd_even_step.set(false);
////        }
//
//        while (current < n) {
//            synchronized (lock) {
//                while (isZero || num % 2 == 0) {
//                    lock.wait();
//                }
//                printNumber.accept(num);
//                isZero = true;
//                num++;
//                current++;
//                lock.notifyAll();
//                while (current >= n) {
//                    return;
//                }
//            }
//        }
//    }

    /**
     * <p>
     * 核心逻辑缺陷：
     * 在 even/odd 方法中，当线程进入 synchronized 块后，
     * 如果条件不满足（如 isZero 为 true 或数字奇偶性不符），线程会调用 lock.wait() 进入等待状态。
     * 当最后一个非零数字打印后，current 增加到 n，
     * 但 可能仍有另一个线程在 lock.wait() 中永久等待（因为已没有线程会调用 lock.notifyAll() 来唤醒它）。
     * 并且阻塞线程不知道任务已完成
     * 示例场景（以 n=2 为例）：
     * 线程执行顺序：zero → odd → zero → even
     * even 线程打印数字 2 后，设置 isZero=true 并调用 lock.notifyAll()。
     * 此时 odd 线程可能在等待状态（因 num 已递增为 3，奇偶性不符， 但是isZero是true, 并且zero线程已经退出了， i >= n ）。
     * 所有线程执行完毕后，odd 线程仍阻塞在 lock.wait()，无法退出。没有线程会调用 lock.notifyAll()，并且isZero一直为true,
     * 无法变化，循环判断导致odd线程陷入阻塞，虚假唤醒也没有作用了
     * </p>
     * <p>
     * 在 even/odd 方法的 while (isZero || ...) 循环中增加 退出条件检查，
     * 确保当所有数字打印完成时，线程能主动退出等待。
     * 第一重检查（等待循环内）
     * 目的：防止永久阻塞， 退出等待循环
     * 当线程在 lock.wait() 时被唤醒（可能是虚假唤醒或最后一个数字打印后的通知,
     * 比如 n == 2, even线程打印后调用lock.notifyAll()）
     * 立即检查任务是否已完成（current >= n）
     * 如果已完成，跳出等待循环，避免重新进入等待
     * </p>
     * <p>
     * 第二重检查（等待循环后）
     * 目的：防止无效打印
     * 解决唤醒后状态不一致问题
     * 确保在获得锁后、执行打印前，任务确实未完成
     * 退出同步块
     * </p>
     * <p>
     * 为什么单次检查不够？
     * 竞态条件：
     * 在 lock.wait() 返回后到重新获取锁之前，状态可能已被其他线程改变
     * 批量唤醒问题：
     * notifyAll() 会唤醒所有等待线程,但只有一个线程能继续执行，其他线程获取锁时状态可能已过期
     * 执行间隙：
     * 从跳出等待循环到执行打印之间，可能有其他线程修改了状态
     * 特别是当 current 在临界点（current == n-1 → current == n）时
     * </P>
     */
    private int n;
    private static final Object lock = new Object();
    private boolean isZeroTurn = true;
    private int currentNumber = 1;

    public ZeroEvenOdd(int n) {
        this.n = n;
    }

    public void zero(IntConsumer printNumber) throws InterruptedException {
        for (int i = 0; i < n; i++) {
            synchronized (lock) {
                // 等待轮到zero线程
                while (!isZeroTurn) {
                    lock.wait();
                }
                printNumber.accept(0);
                isZeroTurn = false;
                lock.notifyAll();
            }
        }
    }

    public void even(IntConsumer printNumber) throws InterruptedException {
        while (true) {
            synchronized (lock) {
                // 等待轮到even线程且当前数为偶数，同时不超过n
                while (isZeroTurn || currentNumber % 2 != 0 || currentNumber > n) {
                    if (currentNumber > n) return; // 关键退出条件
                    lock.wait();
                }
                if (currentNumber > n) break; // 双重检查
                printNumber.accept(currentNumber);
                currentNumber++;
                isZeroTurn = true;
                lock.notifyAll();
            }
        }
    }

    public void odd(IntConsumer printNumber) throws InterruptedException {
        while (true) {
            synchronized (lock) {
                // 等待轮到odd线程且当前数为奇数，同时不超过n
                while (isZeroTurn || currentNumber % 2 == 0 || currentNumber > n) {
                    if (currentNumber > n) return; // 关键退出条件
                    lock.wait();
                }
                if (currentNumber > n) break; // 双重检查
                printNumber.accept(currentNumber);
                currentNumber++;
                isZeroTurn = true;
                lock.notifyAll();
            }
        }
    }

    public static void main(String[] args) {
        IntConsumer printNumber = new IntConsumer() {
            @Override
            public void accept(int value) {
                System.out.print(value);
            }
        };
        ExecutorService executor = Executors.newFixedThreadPool(3);
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        final ZeroEvenOdd zeroEvenOdd = new ZeroEvenOdd(n);
        //线程 zero, odd, even
        executor.submit(() -> {
            try {
                zeroEvenOdd.zero(printNumber);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executor.submit(() -> {
            try {
                zeroEvenOdd.even(printNumber);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executor.submit(() -> {
            try {
                zeroEvenOdd.odd(printNumber);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        executor.shutdown();
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("\nover");
    }
}