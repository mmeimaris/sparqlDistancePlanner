package app.tests;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestWithTimeout {

	
	public static void main(String[] args) throws Exception{
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new TestWithTimeout().new Task());

        try {
            System.out.println("Started..");
            System.out.println(future.get(3600, TimeUnit.SECONDS));
            System.out.println("Finished!");
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Terminated!");
        }

        executor.shutdownNow();
    
	}
	
	class Task implements Callable<String> {
	    @Override
	    public String call() throws Exception {
	        
	        return "Ready!";
	    }
	}
		
	
}
