import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import javalib.worldcanvas.WorldCanvas;

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
  
  //EFFECT:connects this Node to the given Node through an edge
  //of the given weight
  //returns the edge that was created to connect the two nodes
  public Edge connect(Node other, int weight) {
    Edge temp = new Edge(this, other, weight);
    this.outedges.add(temp);
    other.outedges.add(temp);
    return temp;
  }
  
  //returns true if this node is connected to the given node
  //through any of its outedges
  public boolean isConnected(Node other) {
    for(Edge e : this.outedges) {
      if(e.connectsThisToThat(this, other)) {
        return true;
      }
    }
    return false;
  }
  
  //removes the given edge from this.outedges
  public void remove(Edge e) {
    this.outedges.remove(e);
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
  
  
  //returns true if this edge connects the two given Nodes
  public boolean connectsThisToThat(Node n1, Node n2) {
    return (this.from.equals(n1) && this.to.equals(n2)) || (this.from.equals(n2) && this.to.equals(n1)); 
  }
  
  //removes this edge from both from and to
  public void removeThisEdge() {
    this.from.remove(this);
    this.to.remove(this);
  }
}
 
class KruskalMaze {
  HashMap<Node, Node> representatives;
  // all edges in graph, sorted by edge weights
  ArrayList<Edge> worklist;
  // all edges that are not used in the algorithm
  ArrayList<Edge> leftOver;
  
  KruskalMaze(HashMap<Node, Node> representatives, ArrayList<Edge> worklist) {
    this.representatives = representatives;
    this.worklist = worklist;
    this.leftOver = new ArrayList<Edge>();
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
        this.leftOver.add(this.worklist.remove(0));
      } else {
        resultEdges.add(e);
        util.union(this.representatives, 
            util.find(this.representatives, e.from), 
            util.find(this.representatives, e.to));
        this.worklist.remove(0);
      }
    }
    this.leftOver.addAll(this.worklist);
    return resultEdges;
  }
  
  //removes all of this.leftover from its connected parts
  void removeAllLeftOver() {
    for(Edge e : this.leftOver) {
      e.removeThisEdge();
    }
  }
}

//this class is generalized even though it is only used in one specific case
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
    reps.replace(rep1, rep2);
  }

}

//class that represents the Maze

class MazeGame extends World{
  ArrayList<ArrayList<Node>> mazeBoard;
  //one value that controls the size of the board
  int squareSize;
  
  public MazeGame(int x, int y, int sqSize, Random rand) {
    this.mazeBoard = this.makeConnectedBoard(x, y, rand);
    this.squareSize = sqSize;
  }
  
  public MazeGame(int x, int y, int sqSize) {
    this(x, y, sqSize, new Random());
  }
  
  //creates the board with x length and y height 
  //where each cell is connected to each neighboring cell through an 
  //edge with a random length anywhere from 0 to 99
  //also adds each edge to kruskal's edges and then, at the end, creates Kruskal's edges
  //and makes every node in the hashmap equal to itself for Kruskal's algo later
  public ArrayList<ArrayList<Node>> makeConnectedBoard(int x, int y, Random rand) {
    //hashmap that represents what each node's representative is
    HashMap<Node, Node> boardHash = new HashMap<Node,Node>();
    //all of the edges in the boardd
    ArrayList<Edge> allEdges = new ArrayList<Edge>();
    ArrayList<ArrayList<Node>> resultBoard = new ArrayList<ArrayList<Node>>();
    for(int i = 0 ; i < y ; i +=1) {
      ArrayList<Node> rowI = new ArrayList<Node>();
      for(int j = 0 ; j < x ; j +=1) {
        Node temp = new Node();
        rowI.add(temp);
        //ensures that this node is added to the hashmap and maps to itself
        boardHash.put(temp, temp);
        //kruskalEdges starts with all of the edges in the list
        if(i > 0) {
          allEdges.add(temp.connect(resultBoard.get(i-1).get(j), rand.nextInt(100)));
        }
        if(j > 0) {
          allEdges.add(temp.connect(rowI.get(j-1), rand.nextInt(100)));
        }
      }
      resultBoard.add(rowI);
    }
    
    this.sortEdges(allEdges);
    KruskalMaze kru = new KruskalMaze(boardHash, allEdges);
    //creates a path through all of the cells, also filling the leftover field with
    //all the unused connections between the neighbor nodes
    kru.algorithm();
    //removes all of the leftoverpaths that are not used in kruskal's algo
    kru.removeAllLeftOver();
    
    return resultBoard;
  }
  
  //sorts the edges to be put into kruskal's algorithm
  public void sortEdges(ArrayList<Edge> input) {
    input.sort(new CompareByWeight());
  }
  
  
  //draws the maze
  public WorldImage drawMaze() {
    WorldImage bkg = new EmptyImage();
    for(int i = 0 ; i < this.mazeBoard.size() ; i += 1) {
      WorldImage row = new EmptyImage();
      for(int j = 0 ; j < this.mazeBoard.get(i).size() ; j += 1) {
        WorldImage cell = new RectangleImage(this.squareSize, this.squareSize, OutlineMode.SOLID, Color.gray);
        //if the cell to the left is not connected, create a cell with a wall to the left
        if(j > 0 && !this.mazeBoard.get(i).get(j).isConnected(this.mazeBoard.get(i).get(j-1))) {
          cell = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, new RectangleImage(1, this.squareSize, OutlineMode.SOLID, Color.black), 0, 0, cell);
        }
        //if the cell above this cell is not connected, create a cell with a wall above
        if(i > 0 && !this.mazeBoard.get(i).get(j).isConnected(this.mazeBoard.get(i-1).get(j))) {
          cell = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, new RectangleImage(this.squareSize, 1, OutlineMode.SOLID,Color.black), 0, 0, cell);
        }
        row = new BesideImage(row,cell);
      }
      bkg = new AboveImage(bkg, row);
    }
    //return new OverlayImage(bkg, new RectangleImage(this.mazeBoard.get(0).size()*this.squareSize+ 5, this.mazeBoard.size() * this.squareSize + 5, OutlineMode.SOLID, Color.black));
    return bkg;
  }

  //makes the scene
  public WorldScene makeScene() {
    WorldScene bkg = this.getEmptyScene();
    bkg.placeImageXY(this.drawMaze(), this.getMazeWidth()/2, this.getMazeHeight()/2);
    return bkg;
  }
  
  //returns the width of the screen for the current maze game
  public int getMazeWidth() {
    return this.mazeBoard.get(0).size() * this.squareSize;
  }
  
//returns the height of the screen for the current maze game
  public int getMazeHeight() {
    return this.mazeBoard.size() * this.squareSize;
  }
  
}



class CompareByWeight implements Comparator<Edge> {

  @Override
  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }
}


class ExampleMaze {
  MazeGame g1;
  
  void initConditions() {
    this.g1 = new MazeGame(250, 200,4);
  }
  
  boolean testScene(Tester t) {
    this.initConditions();
    WorldCanvas c = new WorldCanvas(this.g1.getMazeWidth(),this.g1.getMazeHeight());
    return c.drawScene(this.g1.makeScene()) && c.show();
  }
}


