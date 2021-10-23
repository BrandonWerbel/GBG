package games.EWN.config;

public class ConfigEWN {

    /**
     * Number of players
     * allowed: 2 3 4
     */
    public static int NUM_PLAYERS = 2;

    /**
     * Board size
     * allowed: 3, 4, 5, 6
     */
    public static int BOARD_SIZE = 3;

    public static int CEll_CODING = 0;

    public static String[] CELL_CODE_NAMING = {"[0]", "[0],[1],[2]"};

    public static String[] CELL_CODE_DIR_NAMING = {"G-0", "G-0-1-2"};
    // avoid "[" and "]" in dir name because Excel cannot save on such dirs

    public static boolean RANDOM_POSITION = false;

}
