import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Assumptions:
 * 1. The input json is going to be valid. Hence no input checks implemented.
 * In a production system, we can have a wrapping layer or a factory which implements those checks for us.
 * 2. Since I assumed the input is going to be valid, it didn't make sense to have custom exceptions and throw them.
 * But in a production code, I would create different exceptions and throw them accordingly and handle them at suitable
 * places.
 * 3. For CoffeeMachine the executor is ThreadPoolExecutor which takes runnable. Using callables instead of runnable
 * can make life easier by returning 'future'. But for the problem scope, using runnable seemed ok.
 *
 * Scope for improvement (did not implement due to time constraints):
 * 1. Instead of passing around Map, we can parse JSON and have individual objects like (Inventory, Beverage, Worker etc)
 * 2. ExceptionHandling.
 */


public class Main {

    private static Map<String, Integer> inventoryMap;
    private static Map<String, Map<String, Integer>> ordersMap;
    private static int outlets;
    private static Gson gson = new Gson();

    private static void setData(String jsonString) {
        JsonObject jsonObj = gson.fromJson(jsonString, JsonObject.class);
        JsonObject machineObj = jsonObj.get("machine").getAsJsonObject();
        JsonObject outletsObj = machineObj.get("outlets").getAsJsonObject();
        JsonObject inventoryObj = machineObj.get("total_items_quantity").getAsJsonObject();
        JsonObject beveragesObj = machineObj.get("beverages").getAsJsonObject();

        inventoryMap = gson.fromJson(inventoryObj, new TypeToken<Map<String, Integer>>(){}.getType());
        ordersMap = gson.fromJson(beveragesObj,
                new TypeToken<Map<String, Map<String, Integer>>>(){}.getType());
        outlets = outletsObj.getAsJsonObject().get("count_n").getAsInt();
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {

        URL resource = Main.class.getClassLoader().getResource("input2.json");
        String jsonString = new String(Files.readAllBytes(Paths.get(resource.toURI())));

        setData(jsonString);

        CoffeeMachine cm = new CoffeeMachine(inventoryMap, outlets);
        for (String beverage : ordersMap.keySet()) {
            Map<String, Integer> ingredients = ordersMap.get(beverage);
            cm.enqueue(beverage, ingredients);
        }

        // Waiting. Can use notify, latch, futures etc. // But for take-home test it should be fine.
        while (cm.getSucceededCount() + cm.getFailedCount() < cm.getOrderCount()) {
            Thread.sleep(50);
        }

        cm.shutdown();

//        Uncomment the below to run another test.

//        System.out.println("\n***************************************\n");
//        // Another input.
//        resource = Main.class.getClassLoader().getResource("input2.json");
//        jsonString = new String(Files.readAllBytes(Paths.get(resource.toURI())));
//
//        setData(jsonString);
//
//        cm = new CoffeeMachine(inventoryMap, outlets);
//        for (String beverage : ordersMap.keySet()) {
//            Map<String, Integer> ingredients = ordersMap.get(beverage);
//            cm.enqueue(beverage, ingredients);
//        }
//
//        while (cm.getSucceededCount() + cm.getFailedCount() < cm.getOrderCount()) {
//            Thread.sleep(50);
//        }
//
//        cm.shutdown();

    }
}
