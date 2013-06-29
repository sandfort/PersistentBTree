import java.util.Scanner;
import java.io.File;
import java.io.IOException;

/**
 * John Sandfort
 * CSC 365
 * Fall 2012
 */ 

public class PBTTest3 {
    public static void main(String[] args) throws IOException {
        PersistentBTree tree = new PersistentBTree(8, "test.txt");
        Scanner sc = new Scanner(new File("words.txt"));
        String s;
        
        int i = 0;
        while ( sc.hasNext() ) {
            s = sc.nextLine().trim();
            System.out.print(i++ + ": ");
            System.out.print("inserting " + s + "... ");
            System.out.println(tree.insert(s) ? "done." : "already in tree.");
        }
        
        System.out.println();
        
        System.out.print("Removing snazzy...");
        if ( tree.remove("snazzy") ) {
            System.out.println("done.");
        } else {
            System.out.println("not in tree!");
        }
        
        System.out.print("Removing blivet...");
        if ( tree.remove("blivet") ) {
            System.out.println("done.");
        } else {
            System.out.println("not in tree!");
        }
        
        System.out.print("inserting blivet...");
        if ( tree.insert("blivet") ) {
            System.out.println("done.");
        } else {
            System.out.println("already in tree!");
        }
        
        System.out.print("inserting abc...");
        if ( tree.insert("abc") ) { 
            System.out.println("done.");
        } else {
            System.out.println("already in tree!");
        }
        
        System.out.print("inserting blivet...");
        if ( tree.insert("blivet") ) {
            System.out.println("done.");
        } else {
            System.out.println("already in tree!");
        }
        
        System.out.print("inserting abc...");
        if ( tree.insert("abc") ) {
            System.out.println("done.");
        } else {
            System.out.println("already in tree!");
        }
        
        System.out.print("inserting snazzy...");
        if ( tree.insert("snazzy") ) {
            System.out.println("done.");
        } else {
            System.out.println("already in tree!");
        }
        
        //tree.close("header.txt");
    }
}