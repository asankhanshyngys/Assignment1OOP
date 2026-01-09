public class FoodItem extends MenuItem{
    public FoodItem(String name, double price){
        super(name,price,"Food");
    }
    @Override
    public void printInfo(){
        System.out.println("Food: " + super.toString());
    }
}
