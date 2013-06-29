# Persistent B-Tree #

This was the final project for CSC 365 - Advanced Data Structures & Algorithms
at SUNY Oswego.

A B-Tree is a searching tree data structure.  Every node of a b-tree of order
M can contain up to M-1 keys and M links to subsequent nodes, and no less than
(M-1)/2 keys and M/2 links.  Data in a b-tree is guaranteed to be ordered such
that (as with a binary search tree) a depth-first search will traverse all
data are visited in order (when keys and links are traversed iteratively from
left to right).

This particular implementation of the b-tree uses a fixed-length random access
file (FLRAF) to store its nodes during and after the execution of the program.
The PBTCache reduces the number of reads and writes by keeping up to four
nodes in its memory.
