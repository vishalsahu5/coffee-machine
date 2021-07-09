import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class CoffeeMachineTest {

    private CoffeeMachine coffeeMachine;
    private Map<String, Integer> inventoryMap;
    private Map<String, Map<String, Integer>> ordersMap;
    private int outlets;
    private Gson gson = new Gson();


    @AfterEach()
    public void afterEach() {
        coffeeMachine.shutdown();
    }

    private void setData(String jsonString) {
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

    @Test
    public void testCanPrepareAllBeveragesWithSufficientIngredients() throws Exception {
        URL resource = Main.class.getClassLoader().getResource("input2.json");
        String jsonString = new String(Files.readAllBytes(Paths.get(resource.toURI())));

        setData(jsonString);

        coffeeMachine = new CoffeeMachine(inventoryMap, outlets);
        for (String beverage : ordersMap.keySet()) {
            Map<String, Integer> ingredients = ordersMap.get(beverage);
            coffeeMachine.enqueue(beverage, ingredients);
        }

        Thread.sleep(1000);
        Assert.assertEquals(4, coffeeMachine.getSucceededCount());
    }

    @Test
    public void testCannotPrepareAllBeveragesWithInsufficientIngredients() throws Exception {
        URL resource = Main.class.getClassLoader().getResource("input1.json");
        String jsonString = new String(Files.readAllBytes(Paths.get(resource.toURI())));

        setData(jsonString);

        coffeeMachine = new CoffeeMachine(inventoryMap, outlets);
        for (String beverage : ordersMap.keySet()) {
            Map<String, Integer> ingredients = ordersMap.get(beverage);
            coffeeMachine.enqueue(beverage, ingredients);
        }

        Thread.sleep(1000);
        Assert.assertEquals(2, coffeeMachine.getSucceededCount());
    }
}