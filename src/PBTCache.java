import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.nio.ByteBuffer;

/**
 * John Sandfort
 * CSC 365
 * Fall 2012
 */

public class PBTCache {
    private int order;
    public int numberOfBlocks;
    LinkedBlockingQueue<Integer> freeBlocks;
    private FLRAF f;

    PBTNode[] nodes;
    private boolean[] dirty;

    String emptyString;
    byte[] emptyStringBytes;
    Integer negOne;
    byte[] negOneBytes;

    PBTCache(int order, String file) throws IOException {
        this.order = order;
        int blockSize = ((order-1)*32)+(order*4);
        this.numberOfBlocks = 0;
        this.freeBlocks = new LinkedBlockingQueue<Integer>();
        this.f = new FLRAF(blockSize, file);

        emptyString = "                                ";
        emptyStringBytes = emptyString.getBytes();
        negOne = new Integer(-1);
        negOneBytes = integerToBytes(negOne);

        nodes = new PBTNode[4];
        dirty = new boolean[4];
        dirty[0] = false;
        dirty[1] = false;
        dirty[2] = false;
        dirty[3] = false;
    }

    /** Write a node to the cache.
     * @param n the node to write to the cache.
     * @return the index of the node as an Integer object.
     */
    public Integer write(PBTNode n) {
        //System.out.println("Writing node with index " + n.index.intValue() + " in cache.");
        //for ( String s : n.keys )
        //    System.out.println(s);
        // find a "clean" node to overwrite
        for ( int i = 0; i < 4; ++i ) {
            if ( !dirty[i] ) {
                nodes[i] = n;
                dirty[i] = true;
                return n.index;
            }
        }

        // overwrite the node with the lowest index
        int j = 0;
        for ( int i = 0; i < 4; ++i )
            if ( nodes[i].index.intValue() < nodes[j].index.intValue() )
                j = i;

        //System.out.println("Writing node with index " + nodes[j].index.intValue() + " to file.");
        writeToFile(nodes[j]);
        nodes[j] = n;
        dirty[j] = true;
        return n.index;
    }

    /** Read a node from the cache.
     * @param i the index of the node.
     * @return a new node object.
     */
    public PBTNode read(Integer i) {
        // search for this node in the cache
        for ( int j = 0; j < 4; ++j )
            if ( nodes[j] != null && nodes[j].index.equals(i) )
                return nodes[j];

        // find a "clean" node to overwrite
        PBTNode n = readFromFile(i);
        n.index = i;
        for ( int j = 0; j < 4; ++j )
            if ( !dirty[j] )
                return nodes[j] = n;

        // write the node with the lowest index to file, then overwrite it in the cache
        int k = 0;
        for ( int j = 0; j < 4; ++j )
            if ( nodes[j].index.intValue() < nodes[k].index.intValue() )
                k = j;

        writeToFile(nodes[k]);
        nodes[k] = n;
        dirty[k] = false;
        return n;
    }

    /** Write all nodes in the cache to file and empty the cache.
     */
    public void flush() {
        for ( int i = 0; i < 4; ++i ) {
            if ( dirty[i] )
                writeToFile(nodes[i]);
            nodes[i] = null;
            dirty[i] = false;
        }
    }

    public void erase(PBTNode n) {
        eraseFromFile(n.index);
        freeBlocks.add(n.index);
    }

    public void erase(Integer i) {
        eraseFromFile(i);
        freeBlocks.add(i);
    }

    /** Select the index of an empty block in the FLRAF.
     * @return the index of the empty block.
     */
    public Integer getNextIndex() {
        if ( freeBlocks.peek() == null ) {
            ++numberOfBlocks;
            //System.out.println("Assigning index: " + (numberOfBlocks-1));
            return new Integer(numberOfBlocks-1);
        }
        int i = freeBlocks.remove();
        //System.out.println("Reassigning index: " + i);
        return i;
        //return freeBlocks.remove();
    }

    // writes a Persistent B-Tree node to the FLRAF at its index
    private void writeToFile(PBTNode n) {
        try {
            byte[] bytes = nodeToBytes(n);
            f.write(nodeToBytes(n), n.index.intValue());
        } catch ( IOException ioe ) {
            System.err.println("Something went wrong in writeToFile(PBTNode).");
            ioe.printStackTrace();
            System.exit(-1);
        }
    }

    // reads a Persistent B-Tree Node from the FLRAF
    private PBTNode readFromFile(Integer i) {
        //System.out.println("Reading from file block " + i);
        try {
            return bytesToNode(f.read(i.intValue()));
        } catch ( IOException ioe ) {
            System.err.println("Something went wrong in readFromFile(Integer).");
            System.err.println("The index given is: " + i.intValue() + ".");
            ioe.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    // writes zeroes over a block in the FLRAF and frees the node's index for use
    private void eraseFromFile(Integer i) {
        if ( i.intValue() >= numberOfBlocks ) {
            byte[] bytes = new byte[(32*(order-1))+(4*order)];
            for ( int j = 0; j < bytes.length; ++j )
                bytes[j] = (byte) 0x00;
            try {
                f.write(bytes, i.intValue());
            } catch ( IOException ioe ) {
                System.err.println("Something went wrong in eraseFromFile(Integer).");
                ioe.printStackTrace();
                System.exit(-1);
            }
            //System.out.println("Freeing index: " + i.intValue());
            if ( i.intValue() == numberOfBlocks-1 )
                --numberOfBlocks;
            else
                freeBlocks.add(i);
        }
    }

    // helper method to convert a byte array to a node
    private PBTNode bytesToNode(byte[] bytes) {
        ArrayList<String> keys = new ArrayList<String>(order-1);
        ArrayList<Integer> links = new ArrayList<Integer>(order);
        String s;
        Integer i;
        byte[] stringBytes = new byte[32];
        byte[] intBytes = new byte[4];

        // parsing bytes to Strings
        // ignores 32-space Strings
        for ( int j = 0; j < order-1; ++j ) {
            for ( int k = 0; k < 32; ++k )
                stringBytes[k] = bytes[(j*32)+k];
            s = new String(stringBytes);
            if ( !s.equals(emptyString) )
                keys.add(s.trim());
        }

        // parsing bytes to Integers
        // ignores -1
        for ( int j = 0; j < order; ++j ) {
            for ( int k = 0; k < 4; ++k )
                intBytes[k] = bytes[((order-1)*32)+(j*4)+k];
            i = bytesToInteger(intBytes);
            if ( !i.equals(negOne) )
                links.add(i);
        }

        PBTNode n = new PBTNode(order, this);
        n.keys = keys;
        n.links = links;
        return n;
    }

    // helper method to convert a node to a byte array
    private byte[] nodeToBytes(PBTNode node) {
        byte[] stringBytes = new byte[32];
        byte[] intBytes = new byte[4];
        byte[] bytes = new byte[(32*(order-1))+(4*order)];

        // parsing Strings to bytes
        for ( int i = 0; i < node.keys.size(); ++i ) {
            stringBytes = fixLength(node.keys.get(i)).getBytes();
            for ( int j = 0; j < 32; ++j )
                bytes[(32*i)+j] = stringBytes[j];
        }

        // filling the rest of the block with 32-space Strings
        for ( int i = node.keys.size(); i < order-1; ++i )
            for ( int j = 0; j < 32; ++j )
                bytes[(32*i)+j] = emptyStringBytes[j];

        // parsing Integers to bytes
        for ( int i = 0; i < node.links.size(); ++i ) {
            intBytes = integerToBytes(node.links.get(i));
            for ( int j = 0; j < 4; ++j )
                bytes[(32*(order-1))+(4*i)+j] = intBytes[j];
        }

        // filling the rest of the block with -1
        for ( int i = node.links.size(); i < order; ++i )
            for ( int j = 0; j < 4; ++j )
                bytes[(32*(order-1))+(4*i)+j] = negOneBytes[j];

        return bytes;
    }

    // helper method to convert an array of bytes to an Integer
    private Integer bytesToInteger(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    // helper method to convert an Integer to an array of bytes
    private byte[] integerToBytes(Integer i) {
        return ByteBuffer.allocate(4).putInt(i.intValue()).array();
    }

    // helper method to force a String to a length of 32
    private String fixLength(String s) {
        s = s.trim();
        if ( s.length() > 32 )
            s = s.substring(0, 31);
        while ( s.length() < 32 )
            s = s.concat(" ");
        return s;
    }
}
