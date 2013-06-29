import java.util.Scanner;
import java.io.File;
import java.io.IOException;

/**
 * John Sandfort
 * CSC 365
 * Fall 2012
 */ 

public class PBTTest2 {
    public static void main(String[] args) throws IOException {
        PersistentBTree tree = new PersistentBTree("header.txt");
        //tree.restore("header.txt");
        
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
    }
}