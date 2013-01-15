import java.util.*;

public class Sorting implements Comparator<Entry>{
    public int compare(Entry self, Entry other){ return 0; }
}

class SortByPosted extends Sorting{
    public int compare(Entry self, Entry other){
        if(self == null) return -1;
        if(other == null) return 1;
        return self.getPosted().compareTo(other.getPosted());
    }
}

class SortByUpdated extends Sorting{
    public int compare(Entry self, Entry other){
        if(self == null) return -1;
        if(other == null) return 1;
        return self.getUpdated().compareTo(other.getUpdated());
    }
}
