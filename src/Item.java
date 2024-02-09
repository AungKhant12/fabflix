public class Item {
    private String title;
    private String id;
    private int count = 1;
    private double price = 5.00;

    public Item(String title, String id) {
        this.title = title;
        this.id = id;
//        count++;
    }

    public void incrementItemCount(){
        count ++;
    }

    public int decrementItemCount(){
        count --;
        return count;
    }

    public String getTitle(){
        return title;
    }

    public String getId(){
        return id;
    }

    public double getPrice(){
        return price;
    }

    public double getTotalPrice(){
        return price * count;
    }

    public int getCount(){
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        // If the object is compared with itself then return true
        if (obj == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(obj instanceof Item)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Item other = (Item) obj;

        // Compare the data members and return accordingly
        return id.equals(other.id);
    }
}
