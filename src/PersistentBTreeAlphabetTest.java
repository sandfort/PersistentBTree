import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

/**
 * John Sandfort
 * CSC 365
 * Fall 2012
 */ 

public class PersistentBTreeAlphabetTest {
    public static void main(String[] args) throws IOException {
        int order = 4;
        PersistentBTree tree = new PersistentBTree(order, "test.txt");

        String[] adds = {
            "Q", "W", "E", "R", "T", "Y", "U", "I", "O" ,"P",
            "A", "S", "D", "F", "G", "H", "J", "K", "L",
            "Z", "X", "C", "V", "B", "N", "M",
            "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z"
        };

        for ( String s : Arrays.asList(adds).subList(0, adds.length) )
            add(tree, s);
       
        /*
        Scanner sc = new Scanner(System.in);
        String s = "";
        while ( !s.equals("!q") ) {
            System.out.print("Remove what word? ");
            s = sc.nextLine().trim();
            System.out.print("Removing " + s + "... ");
            if ( tree.remove(s) )
                System.out.print("done.\n");
            else System.out.print("not found!\n");
            tree.print();
        }
        */
    }

    public static void add(PersistentBTree tree, String s) {
        System.out.print("Adding " + s + "... ");
        System.out.print(tree.insert(s) ? "done.\n" : " already in tree!\n");
        tree.print();
    }
}
