import java.util.ArrayList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
public class Cart {
    private ArrayList<Item> items;

    //---[ Constructor ]-------------------------------------------------------
    public Cart() {
        final String FUNC = "Constructor";
        logToServer(FUNC, "Entered Constructor");
        items = new ArrayList<Item>();
        logToServer(FUNC, "Created items cart");
    }
    //---[ Constructor ]-------------------------------------------------------

    public ArrayList<Item> getItemsAsString() {
        return items;
    }

    public String getCartItems() {
        JsonArray cartItemsJsonArray = new JsonArray();

        for (int counter = 0; counter < items.size(); counter++){
            JsonObject movie = new JsonObject();

            movie.addProperty("title", items.get(counter).getTitle());
            movie.addProperty("id", items.get(counter).getId());
            movie.addProperty("count", items.get(counter).getCount());
            movie.addProperty("price", items.get(counter).getPrice());

            cartItemsJsonArray.add(movie);
        }
        return cartItemsJsonArray.toString();
    }

    //---[ Cart Operations ]---------------------------------------------------
    public void increaseItemCount(String title, String id) {
        final String FUNC = "increaseItemCount";
        logToServer(FUNC, "Attempting to increase item count for: " + title + ", " + id);

        for (int counter = 0; counter < items.size(); counter++) {
            if (items.get(counter).getId().equals(id)) {
                items.get(counter).incrementItemCount();
                return;
            }
        }

        Item newMovie = new Item(title, id);
        items.add(newMovie);

        logToServer(FUNC, "items: " + items.toString());
        logToServer(FUNC, "items size: " + items.size());
    }

    public void decreaseItemCount(String id){
        for (int counter = 0; counter < items.size(); counter++){
            if (items.get(counter).getId().equals(id)){
                int newItemCount = items.get(counter).decrementItemCount();
                if (newItemCount == 0){
                    items.remove(counter);
                }
            }
        }
    }

    public void deleteItem(String id){
        for (int counter = 0; counter < items.size(); counter++){
            if (items.get(counter).getId().equals(id)){
                items.remove(counter);
            }
        }
    }

    public Item getItemAt(int index) {
        return this.items.get(index);
    }

    public double getTotal(){
        double total = 0.0;

        for (int counter = 0; counter < items.size(); counter++){
            total += items.get(counter).getTotalPrice();
        }

        return total;
    }

    public void clearCart() {
        this.items.clear();
    }
    //---[ Cart Operations ]---------------------------------------------------

    //---[ Helper Methods ]----------------------------------------------------
    public void logToServer(String funcName, String msg) {
        final String MODULE_NAME = this.getClass().getSimpleName();
        System.out.printf("%s - %s: %s%n", MODULE_NAME, funcName, msg);
    }
    //---[ Helper Methods ]----------------------------------------------------
}
