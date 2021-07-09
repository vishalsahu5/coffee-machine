import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class CoffeeMachine {
    private Map<String, Integer> inventory;
    private ExecutorService executor;
    private int outlets;
    private int orderCount;
    private int succeededCount;
    private int failedCount;

    public CoffeeMachine(final Map<String, Integer> inventory, int outlets) {
        this.orderCount = 0;
        this.succeededCount = 0;
        this.failedCount = 0;
        this.inventory = new HashMap<>(inventory);
        this.outlets = outlets;
        this.executor = new ThreadPoolExecutor(outlets, outlets, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));
    }

    public int getOrderCount() {
        return orderCount;
    }

    public int getSucceededCount() {
        return succeededCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void shutdown() {
        orderCount = 0;
        failedCount = 0;
        succeededCount = 0;
        executor.shutdown();
    }

    public void enqueue(String beverage, Map<String, Integer> ingredients) {
        orderCount++;
        executor.execute(new BeverageTask(beverage, ingredients));
    }

    private synchronized boolean updateInventory(String beverage, Map<String, Integer> ingredients) {

        boolean canMake = true;
        for (String ingredient : ingredients.keySet()) {
            int requiredQuantity = ingredients.get(ingredient);
            if (requiredQuantity > inventory.getOrDefault(ingredient, 0)) {
                canMake = false;
                failedCount++;
                System.out.println(beverage + " cannot be prepared because " + ingredient + " is not sufficient");
                break;
            }
        }

        if (!canMake) return false;

        for (String ingredient : ingredients.keySet()) {
            int requiredQuantity = ingredients.get(ingredient);
            inventory.put(ingredient, inventory.get(ingredient) - requiredQuantity);
        }

        succeededCount++;
        return true;
    }

    private class BeverageTask implements Runnable {
        public String beverage;
        public Map<String, Integer> ingredients;

        public BeverageTask(String beverage, Map<String, Integer> ingredients) {
            this.beverage = beverage;
            this.ingredients = ingredients;
        }

        @Override
        public void run() {
            if (updateInventory(beverage, ingredients)) {
                // Uncomment below to add delay.
//                try {
//                    Thread.sleep(1500 + (int) (Math.random() * 5000));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                System.out.println(beverage + " is prepared");
            }
        }
    }
}
