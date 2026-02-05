package util;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class DataPool<T> {
    private final List<T> data = new ArrayList<>();

    public void add(T item){
        data.add(item);
    }
    public List<T> all(){
        return new ArrayList<>(data);
    }
    public List<T> filter(Predicate<T> predicate){
        List<T> res = new ArrayList<>();
        for (T x: data) if (predicate.test(x)) res.add(x);
        return res;
    }
    public void sort(Comparator<T> comparator){
        data.sort(comparator);
    }
}
