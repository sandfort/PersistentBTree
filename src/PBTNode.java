import java.util.ArrayList;

/**
 * John Sandfort
 * CSC 365
 * Fall 2012
 */

class PBTNode {
    ArrayList<String> keys;
    ArrayList<Integer> links;
    int order;
    Integer index;
    PBTCache cache;

    PBTNode(int m, PBTCache cache) {
        keys = new ArrayList<String>(m-1);
        links = new ArrayList<Integer>(m);
        order = m;
        this.cache = cache;
        index = new Integer(-1);
    }

    // check for split condition BEFORE adding!
    boolean insert(String s) {
        if ( keys.contains(s) )
            return false;

        int i = 0;
        for ( String k : keys )
            if ( s.compareTo(k) > 0 )
                ++i;

        if ( leaf() ) {
            keys.add(i, s);
            //cache.write(this);
            return true;
        }

        // if the node needs to be split,
        // add to one of the new nodes.
        boolean b;

        PBTNode y = cache.read(links.get(i));

        if ( y.keys.size() >= order-1 ) {
            split(i);
            //cache.write(y);
            //cache.write(this);
            if ( s.compareTo(keys.get(i)) < 0 ) {
                y = cache.read(links.get(i));
            } else if ( s.compareTo(keys.get(i)) > 0 ) {
                y = cache.read(links.get(i+1));
            } else {
                return false;
            }
            //b = y.insert(s);
            //cache.write(y);
            //cache.write(this);
            //return b;
        }

        b = y.insert(s);
        cache.write(y);
        //cache.write(this);
        return b;
    }

    void split(int i) {
        PBTNode n = cache.read(links.get(i));
        PBTNode x = new PBTNode(order, cache);
        x.index = cache.getNextIndex();
        PBTNode c = new PBTNode(order, cache);
        c.index = cache.getNextIndex();
        cache.write(x);
        cache.write(c);
        int mid = (order/2)-1;
        String s = n.keys.remove(mid);
        //x.keys = new ArrayList<String>(n.keys.subList(0, mid));
        //c.keys = new ArrayList<String>(n.keys.subList(mid, n.keys.size()));
        for ( int j = 0; j < mid; ++j )
            x.keys.add(n.keys.get(j));
        for ( int j = mid; j < n.keys.size(); ++j )
            c.keys.add(n.keys.get(j));
        if ( !n.leaf() ) {
            //x.links = new ArrayList<Integer>(n.links.subList(0, mid+1));
            //c.links = new ArrayList<Integer>(n.links.subList(mid+1, n.links.size()));
            for ( int j = 0; j < mid+1; ++j )
                x.links.add(n.links.get(j));
            for ( int j = mid+1; j < n.links.size(); ++j )
                c.links.add(n.links.get(j));
        }
        cache.write(x);
        cache.write(c);
        keys.add(i, s);
        links.set(i, x.index);
        links.add(i+1, c.index);
        cache.erase(n);
        //cache.write(this);
    }

    boolean remove(String s) {
        if ( leaf() ) {
            boolean b = keys.remove(s);
            //if ( b )
                //cache.write(this);
            return b;
        }

        // internal node

        if ( keys.contains(s) ) {
            int i = keys.indexOf(s);
            if ( i == 0 ) {
                keys.set(i, stealSuccessor(i));
                PBTNode z = cache.read(links.get(i+1));
                z.fixMin();
                cache.write(z);
                if ( z.underflow() ) {
                    rebalance(i+1);
                    //cache.write(z);
                }
            } else {
                keys.set(i, stealPredecessor(i));
                PBTNode y = cache.read(links.get(i));
                y.fixMax();
                cache.write(y);
                if ( y.underflow() ) {
                    rebalance(i);
                    //cache.write(y);
                }
            }
            //cache.write(this);
            return true;
        }

        // internal node, key not found

        int i = 0;
        for ( String k : keys )
            if ( s.compareTo(k) > 0 )
                ++i;

        PBTNode y = cache.read(links.get(i));
        boolean b = y.remove(s);
        cache.write(y);
        if ( y.underflow() ) {
            rebalance(i);
            //cache.write(y);
        }
        return b;
    }

    boolean underflow() {
        return keys.size() < ((order/2)-1);
    }

    boolean leaf() {
        return links.isEmpty();
    }

    void rebalance(int i) {
        // i: the index of the deficient node
        // reading nodes from the cache...
        PBTNode x;
        PBTNode y;
        PBTNode z;
        if ( i > 0 )
            x = cache.read(links.get(i-1));
        else x = null;
        y = cache.read(links.get(i));
        if ( i < links.size()-1 )
            z = cache.read(links.get(i+1));
        else z = null;

        if ( z != null && i < links.size()-1 && z.keys.size() >= (order/2)-1 ) {
            // case 1
            // node has a right sibling AND that sibling has enough keys
            // steal through right
            y.keys.add(keys.get(i));
            cache.write(y);
            keys.set(i, z.keys.remove(0));
            cache.write(z);
            if ( !y.leaf() ) {
                y.links.add(z.links.remove(0));
                cache.write(z);
                cache.write(y);
            }
            //cache.write(y);
            //cache.write(z);
        } else if ( x != null && i > 0 && x.keys.size() >= (order/2)-1 ) {
            // case 2
            // node has a left sibling AND that sibling has enough keys
            // steal through left
            y.keys.add(0, keys.get(i-1));
            keys.set(i-1, x.keys.remove(x.keys.size()-1));
            cache.write(x);
            cache.write(y);
            if ( !y.leaf() ) {
                y.links.add(0, x.links.remove(x.links.size()-1));
                cache.write(x);
                cache.write(y);
            }
            //cache.write(x);
            //cache.write(y);
        } else {
            // case 3
            // neither sibling has enough keys
            // merge
            PBTNode n = new PBTNode(order, cache);
            n.index = cache.getNextIndex();
            cache.write(n);
            if ( i == 0 ) {
                // merge with right sibling
                for ( String s : y.keys )
                    n.keys.add(s);
                n.keys.add(keys.remove(i));
                for ( String s : z.keys )
                    n.keys.add(s);
                cache.write(n);
                if ( !y.leaf() ) {
                    for ( Integer j : y.links )
                        n.links.add(j);
                    for ( Integer j : z.links )
                        n.links.add(j);
                    cache.write(n);
                }
                cache.erase(y);
                cache.erase(z);
                links.set(i, n.index);
                links.remove(i+1);
                //cache.write(n);
            } else {
                // merge with left sibling
                for ( String s : x.keys )
                    n.keys.add(s);
                n.keys.add(keys.remove(i-1));
                for ( String s : y.keys )
                    n.keys.add(s);
                cache.write(n);
                if ( !y.leaf() ) {
                    for ( Integer j : x.links )
                        n.links.add(j);
                    for ( Integer j : y.links )
                        n.links.add(j);
                    cache.write(n);
                }
                cache.erase(x);
                cache.erase(y);
                links.remove(i);
                links.set(i-1, n.index);
                //cache.write(n);
            }
        }
        //cache.write(this);
    }

    String stealMax() {
        String s;
        if ( leaf() ) {
            s = keys.remove(keys.size()-1);
            //cache.write(this);
            return s;
        }
        PBTNode n = cache.read(links.get(links.size()-1));
        s = n.stealMax();
        cache.write(n);
        return s;
    }

    String stealMin() {
        String s;
        if ( leaf() ) {
            s = keys.remove(0);
            //cache.write(this);
            return s;
        }
        PBTNode n = cache.read(links.get(0));
        s = n.stealMin();
        cache.write(n);
        return s;
    }

    void fixMax() {
        if ( leaf() )
            return;
        PBTNode n = cache.read(links.get(links.size()-1));
        n.fixMax();
        cache.write(n);
        if ( n.underflow() ) {
            rebalance(links.size()-1);
            //cache.write(n);
        }
    }

    void fixMin() {
        if ( leaf() )
            return;
        PBTNode n = cache.read(links.get(0));
        n.fixMin();
        cache.write(n);
        if ( n.underflow() ) {
            rebalance(0);
            //cache.write(n);
        }
    }

    String stealPredecessor(int i) {
        PBTNode n = cache.read(links.get(i));
        String s = n.stealMax();
        cache.write(n);
        return s;
    }

    String stealSuccessor(int i) {
        PBTNode n = cache.read(links.get(i+1));
        String s = n.stealMin();
        cache.write(n);
        return s;
    }

    void display(int offset) {
        for ( String s : keys )
            System.out.print(s + " ");
        System.out.print("\n");
        for ( int i = 0; i < links.size(); ++i ) {
            for ( int j = 0; j < offset; ++j )
                System.out.print(" ");
            System.out.print("  " + i + ":");
            if ( links.get(i) != null )
                cache.read(links.get(i)).display(offset+2);
        }
        System.out.print("\n");
    }
}
