import java.util.Scanner;
import java.io.File;
import java.io.IOException;

/**
 * John Sandfort
 * CSC 365
 * Fall 2012
 */ 

public class PersistentBTreeFileTest {
    public static void main(String[] args) throws IOException {
        PersistentBTree tree = new PersistentBTree(8, "test.txt");
        Scanner sc = new Scanner(new File("words.txt"));
        Scanner sc2;
        String s;
        int i = 0;
        while ( sc.hasNext() ) {
            s = sc.nextLine().trim();
            System.out.print(i++ + ": ");
            System.out.print("Adding " + s + "... ");
            System.out.println(tree.insert(s) ? "done." : "already in tree.");
        }
        
        

        i = 0;
        sc = new Scanner(new File("words.txt"));
        while ( sc.hasNext() ) {
            s = sc.nextLine().trim();
            System.out.print(i++ + ": ");
            System.out.print("Adding " + s + "... ");
            if ( tree.insert(s) ) {
                //sc2 = new Scanner(System.in);
                //System.out.println("not in tree, press enter...");
                //sc2.nextLine();
                System.out.println("not in tree!");
                System.exit(-1);
            } else {
                System.out.println("already in tree.");
            }
        }

        /*
        tree.print();
        sc = new Scanner(System.in);
        System.out.println("Press enter...");
        sc.nextLine();
        */
        
        /*
        i = 0;
        sc = new Scanner(new File("words.txt"));
        while ( sc.hasNext() ) {
            s = sc.nextLine().trim();
            System.out.print(i++ + ": ");
            System.out.print("Removing " + s + "... ");
            if ( tree.remove(s) )
                System.out.println("done.");
            else {
                System.out.println("not in tree!");
                System.exit(-1);
            }
        }

        /*
        tree.print();
        sc = new Scanner(System.in);
        System.out.println("Press enter...");
        sc.nextLine();
        
        i = 0;
        sc = new Scanner(System.in);
        s = "";
        while ( !s.equals("!q") ) {
            System.out.print("Add what word? ");
            s = sc.nextLine().trim();
            System.out.print(i++ + ": ");
            System.out.print("Adding " + s + "... ");
            System.out.println(tree.insert(s) ? "done." : "already in tree.");
        }

        i = 0;
        sc = new Scanner(new File("words.txt"));
        while ( sc.hasNext() ) {
            s = sc.nextLine().trim();
            System.out.print(i++ + ": ");
            System.out.print("Adding " + s + "... ");
            System.out.println(tree.insert(s) ? "done." : "already in tree.");
        }
        */
    }
}
