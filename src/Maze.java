import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

 
/*
HashMap<String, String> representatives;
List<Edge> edgesInTree;
List<Edge> worklist = all edges in graph, sorted by edge weights;
 
initialize every node's representative to itself
While(there's more than one tree)
  Pick the next cheapest edge of the graph: suppose it connects X and Y.
  If find(representatives, X) equals find(representatives, Y):
    discard this edge  // they're already connected
  Else:
    Record this edge in edgesInTree
    union(representatives,
          find(representatives, X),
          find(representatives, Y))
Return the edgesInTree
*/
 
// represents a node that is connected by a list of edges
class Node {
  
  //We are aware there probably should be more fields in Node
  //but for now, we are just focused on creating the Maze itself
  ArrayList<Edge> outedges;
  
  Node(ArrayList<Edge> outedges) {
    this.outedges = outedges;
  }
  
 //empty constructor if the constructed Node has no edges
  Node() {
    this.outedges = new ArrayList<Edge>();
  }
  
  //connects this Node to the given Node through an edge
  //of the given weight
  public void connect(Node other, int weight) {
    new Edge(this, other, weight);
  }
  
  
}

 
// represents an edge that connects the two nodes
class Edge {
  Node from, to;
  int weight; // how expensive is the edge;
  
  public Edge(Node from, Node to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }


  Edge compare(Edge that) {
    if (that.weight > this.weight) {
      return this;
    } else {
      return that;
    }
  }
}
 
class KruskalMaze {
  HashMap<Node, Node> representatives;
  // all edges in graph, sorted by edge weights
  ArrayList<Edge> worklist;
  
  KruskalMaze(HashMap<Node, Node> representatives, ArrayList<Edge> worklist) {
    this.representatives = representatives;
    this.worklist = worklist;
  }
  
  //Kruskal's Algorithm utilized for constructing the maze
  ArrayList<Edge> algorithm() {
    HashUtils<Node> util = new HashUtils<Node>();
    ArrayList<Edge> resultEdges = new ArrayList<Edge>();
    //we know algo is done when edges in tree equals the number of nodes - 1;
    while (resultEdges.size() != this.representatives.size() - 1) {
      Edge e = this.worklist.get(0);
      //checks to see if this edge creates a loop
      if (util.find(this.representatives, e.from).equals(util.find(this.representatives, e.to))) {
        this.worklist.remove(0);
      } else {
        resultEdges.add(e);
        util.union(this.representatives, 
            util.find(this.representatives, e.from), 
            util.find(this.representatives, e.to));
        this.worklist.remove(0);
      }
    }
    return resultEdges;
  }
}
//utilities for Hashmaps for Nodes
class HashUtils<T> {
  //returns the highest representative of the given node
  T find(HashMap<T, T> reps, T node) {
    //if this node equals the given node, return it
    if (node.equals(reps.get(node))) {
      return node;
    } else {
      //if not, use the same hashmap and the node that this node points to
      return this.find(reps, reps.get(node));
    }
  }

 
  // sets rep1's representative to rep2
  void union(HashMap<T, T> reps, T rep1, T rep2) {
    rep2 = this.find(reps, rep1);
  }
}

//class that represents the Maze

class MazeGame extends World{
  ArrayList<ArrayList<Node>> mazeBoard;


  public MazeGame(int x, int y) {
    this.mazeBoard = this.makeMaze(x, y);
  }
  
  //creates the board with x length and y height 
  //where each cell is connected to each neighboring cell through an 
  //edge with a random length anywhere from 0 to 100
  public ArrayList<ArrayList<Node>> makeConnectedBoard(int x, int y, Random rand) {
    ArrayList<ArrayList<Node>> result = new ArrayList<ArrayList<Node>>();
    for(int i = 0 ; i < y ; i +=1) {
      ArrayList<Node> rowI = new ArrayList<Node>();
      for(int j = 0 ; j < x ; j +=1) {
        Node temp = new Node();
        rowI.add(temp);
        if(i > 0) {
          temp.connect(result.get(i-1).get(j), rand.nextInt(101));
        }
        if(j > 0) {
          temp.connect(rowI.get(j-1), rand.nextInt(101));
        }
      }
    }
    return result;
  }
  
  
}





