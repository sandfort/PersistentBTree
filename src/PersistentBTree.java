import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * John Sandfort
 * CSC 365
 * Fall 2012
 */ 

/** A persistent B-Tree for holding Strings.
 * @author sandfort
 */ 
public class PersistentBTree {
    private PBTNode root;
    private int order;
    private PBTCache cache;
    private String filePath;

    /** Constructs a B-Tree of order m.
     * @param m The order of the B-Tree.  Each node contains m links and m-1 keys.
     */
    public PersistentBTree(int m, String file) throws IOException {
        order = m;
        cache = new PBTCache(m, file);
        root = new PBTNode(m, cache);
        root.index = cache.getNextIndex();
        root.index = new Integer(0);
        cache.write(root);
        filePath = file;
    }
    
    public PersistentBTree(String headerName) throws IOException {
        Scanner sc = new Scanner(new File(headerName));
        filePath = sc.nextLine().trim();
        order = Integer.parseInt(sc.nextLine().trim());
        int numberOfBlocks = Integer.parseInt(sc.nextLine().trim());
        cache = new PBTCache(order, filePath);
        cache.numberOfBlocks = numberOfBlocks;
        PBTNode newRoot = new PBTNode(order, cache);
        String[] keys = sc.nextLine().split(" ");
        for ( int i = 0; i < keys.length; ++i ) {
            if ( !keys[i].trim().isEmpty() ) {
                newRoot.keys.add(keys[i].trim());
            }
        }
        String[] links = sc.nextLine().split(" ");
        for ( int i = 0; i < links.length; ++i ) {
            if ( !links[i].trim().isEmpty() ) {
                newRoot.links.add(new Integer(Integer.parseInt(links[i].trim())));
            }
        }
        newRoot.index = new Integer(0);
        cache.write(newRoot);
        this.root = newRoot;
    }
    
    public void close(String name) throws IOException {
        cache.flush();
        PrintStream header = new PrintStream(new File(name));
        header.println(filePath);
        header.println(order);
        header.println(cache.numberOfBlocks);
        for ( String s : root.keys )
            header.print(s + " ");
        header.print("\n");
        for ( Integer i : root.links )
            header.print(i.intValue() + " ");
        header.print("\n");
    }
    
/*     public void restore(String name) throws IOException {
        Scanner sc = new Scanner(new File(name));
        filePath = sc.nextLine().trim();
        order = Integer.parseInt(sc.nextLine().trim());
        int numberOfBlocks = Integer.parseInt(sc.nextLine().trim());
        cache = new PBTCache(order, new File(filePath));
        cache.numberOfBlocks = numberOfBlocks;
        PBTNode newRoot = new PBTNode(order, cache);
        String[] keys = sc.nextLine().split(" ");
        for ( int i = 0; i < keys.length; ++i ) {
            if ( !keys[i].trim().isEmpty() ) {
                newRoot.keys.add(keys[i].trim());
            }
        }
        String[] links = sc.nextLine().split(" ");
        for ( int i = 0; i < links.length; ++i ) {
            if ( !links[i].trim().isEmpty() ) {
                newRoot.links.add(new Integer(Integer.parseInt(links[i].trim())));
            }
        }
        newRoot.index = new Integer(0);
        cache.write(newRoot);
        this.root = newRoot;
    } */

    /** Adds the given String to the B-Tree.
     * @param s The String to be added.
     * @return false if the String was already in the tree.
     */
    public boolean insert(String s) {
        if ( root.keys.size() >= (order-1) ) {
            splitRoot();
            cache.write(root);
        }
        boolean b = root.insert(s);
        cache.write(root);
        return b;
    } 

    public void print() {
        root.display(0);
        System.out.print("\n");
    }

    /** Removes the given String from the tree if found.
     * @param s The String to be removed.
     * @return true if the String was found and removed from the tree.
     */
    public boolean remove(String s) {
        boolean b = root.remove(s);
        cache.write(root);

        if ( root.keys.isEmpty() ) {
            if ( root.leaf() ) {
                root = null;
                cache.erase(new Integer(0));
            } else {
                PBTNode newRoot = cache.read(root.links.get(0));
                //root = cache.read(root.links.get(0));
                newRoot.index = new Integer(0);
                cache.write(newRoot);
                root = newRoot;
            }
        }
        //cache.write(root);
        return b;
    }

    private void splitRoot() {
        PBTNode left = new PBTNode(order, cache);
        left.index = cache.getNextIndex();
        PBTNode right = new PBTNode(order, cache);
        right.index = cache.getNextIndex();
        cache.write(left);
        cache.write(right);

        int mid = (order/2)-1;

        String s = root.keys.remove(mid);
        for ( int i = 0; i < mid; ++i )
            left.keys.add(root.keys.get(i));
        for ( int i = mid; i < root.keys.size(); ++i )
            right.keys.add(root.keys.get(i));

        if ( !root.leaf() ) {
            for ( int i = 0; i < mid+1; ++i )
                left.links.add(root.links.get(i));
            for ( int i = mid+1; i < root.links.size(); ++i )
                right.links.add(root.links.get(i));
        }

        root.keys.clear();
        root.links.clear();
        root.keys.add(s);
        root.links.add(left.index);
        root.links.add(right.index);

        cache.write(left);
        cache.write(right);
    }
}