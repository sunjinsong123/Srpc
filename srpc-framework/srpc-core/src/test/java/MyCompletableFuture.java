import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MyCompletableFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        /*
        * 可以返回子线程中的值，在主线程成可以获取到。
        * */


        CompletableFuture<Integer> future = new CompletableFuture<>();
        new Thread(()->{

            int i=100;
            System.out.println("线程执行中"+i);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            future.complete(i);
        }).start();

        System.out.println(future.get());
    }
}
