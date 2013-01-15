import java.io.*;

class Settings{
    private static final Sorting[] sorting = {new SortByPosted(), new SortByUpdated()};

    private static int reloadDelay = 0;
    private static File saveDirectory = new File("xplrss/");
    private static int sortIndex = 0;
    private static boolean readAllWarning = true;

    /**
     * Gets the delay of the reload timer, defaults to every 1 minute.
     */
    public static int getReloadDelay(){
        if(reloadDelay > 1) return reloadDelay;
        return 1;
    }

    /**
     * Get the directory in which the reader saves all the cachefiles and the
     * savefile.
     */
    public static File getSaveDirectory(){
        return saveDirectory;
    }

    /**
     * Get the order in which entries are to be sorted.
     */
    public static Sorting getSorting(){
        if(sortIndex > 0) return sorting[sortIndex];
        return sorting[0];
    }

    public static boolean warnForReadAll(){
        return readAllWarning;
    }

    public static void saveToFile(){
        
    }
}
