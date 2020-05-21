package sample;

import java.util.ArrayList;

abstract class Observable {
	private ArrayList<Observer> observers;
	public Observable() {
		this.observers = new ArrayList<>();
	}
	public void addObserver(Observer o) {
		this.observers.add(o);
	}
	public void notifyObservers() {
		for (Observer o : observers) {
			o.update();
		}
	}
}

class Model extends Observable {
	public Island island;
	public Player player;

	public ArrayList<Treasure> treasureDeck = new ArrayList<>();
	public ArrayList<Treasure> discardingDeck = new ArrayList<>();

	// Coordinates of the selected tile
	public int x;
	public int y;

	public Model() {
		this.player = new Player(this, 3, 3);
		this.island = new Island(this);
		island.board[player.y][player.x].playerOnBase();
		for(int i = 0; i < 5; i++)
		{
			discardingDeck.add(Treasure.AirKey);
			discardingDeck.add(Treasure.EarthKey);
			discardingDeck.add(Treasure.FireKey);
			discardingDeck.add(Treasure.WaterKey);
		}
		for(int i = 0; i < 3; i++)
		{
			discardingDeck.add(Treasure.RisingWaters);
			discardingDeck.add(Treasure.Helicopter);
		}
		for(int i = 0; i < 2; i++)
		{
			discardingDeck.add(Treasure.SandBag);
		}
		this.shuffle();
	}

	public void shuffle()
	{
		ArrayList<Treasure> shuffler = new ArrayList<>();
		int nb = discardingDeck.size();
		for(int i = 0; i < nb; i++)
		{
			int idx = (int) (Math.random()*(discardingDeck.size()-1));
			Treasure card = discardingDeck.get(idx);
			shuffler.add(card);
			discardingDeck.remove(idx);
		}
		treasureDeck = shuffler;
	}

	public void draw(Player p) {
		if(treasureDeck.size() > 0) {
			if(treasureDeck.get(0)==Treasure.RisingWaters) {
				island.floodableTiles.remove(island.board[p.y][p.x]);
				island.board[p.y][p.x].flood();
				if(island.board[p.y][p.x].state!=State.Submerged) {
					island.floodableTiles.add(island.board[p.y][p.x]);
				}
				discardingDeck.add(Treasure.RisingWaters);
			}
			else
				p.Inventory.add(treasureDeck.get(0));
			treasureDeck.remove(0);
		}
		else
			shuffle();
	}

	public void newTurn () {
		if(island.floodableTiles.size() < 3
		|| !island.eventExists(Event.Heliport)
		|| !island.eventExists(Event.Temple)
		|| !island.eventExists(Event.Garden)
		|| !island.eventExists(Event.Cavern)
		|| !island.eventExists(Event.Palace))
			endGame();
		else {
			//System.out.print("Activated ! \n");
			island.sink();
			draw(player);
			System.out.println("x:"+x+" y:"+y);
			System.out.println(player.toString());
		}
		notifyObservers();
	}

	public void endGame () {
		System.out.println("Game ended");
	}

	public Tile[][] getBoard() { return island.board; }

	public void moveToTile(Tile t) {
		if(((player.x-t.x) < 2 && player.y == t.y)
		|| ((player.y-t.y) < 2 && player.x == t.x)) {
			island.board[player.y][player.x].playerOnBase();
			player.x = t.x;
			player.y = t.y;
			island.board[player.y][player.x].playerOnBase();
		}
		else {
			if (player.treasureExists(Treasure.Helicopter)) {
				island.board[player.y][player.x].playerOnBase();
				player.x = t.x;
				player.y = t.y;
				island.board[player.y][player.x].playerOnBase();
				player.Inventory.remove(Treasure.Helicopter);
				discardingDeck.add(Treasure.Helicopter);
			} else {
				System.out.println("You cannot do that");
			}
		}
		notifyObservers();
	}

	public void dryCurrentZone(Tile t) {
		if(((player.x-t.x) < 2 && player.y == t.y)
		|| ((player.y-t.y) < 2 && player.x == t.x)) {
			island.floodableTiles.remove(island.board[t.y][t.x]);
			island.board[t.y][t.x].state = State.Dry;
			island.floodableTiles.add(island.board[t.y][t.x]);
		}
		else {
			if (player.treasureExists(Treasure.SandBag)) {
				island.floodableTiles.remove(island.board[t.y][t.x]);
				island.board[t.y][t.x].state = State.Dry;
				island.floodableTiles.add(island.board[t.y][t.x]);
				player.Inventory.remove(Treasure.SandBag);
				discardingDeck.add(Treasure.SandBag);
			} else {
				System.out.println("You cannot do that");
			}
		}
		notifyObservers();
	}

	public void searchCurrentZone(Tile t)
	{
		if(player.x == t.x && player.y == t.y) {
			if(island.board[t.y][t.x].event==Event.Temple && player.treasureExists(Treasure.AirKey)) {
				player.artifacts[3] = true;
				player.Inventory.remove(Treasure.AirKey);
				discardingDeck.add(Treasure.AirKey);
			} else {
				if(island.board[t.y][t.x].event==Event.Garden && player.treasureExists(Treasure.EarthKey)) {
					player.artifacts[2] = true;
					player.Inventory.remove(Treasure.EarthKey);
					discardingDeck.add(Treasure.EarthKey);
				} else {
					if(island.board[t.y][t.x].event==Event.Palace && player.treasureExists(Treasure.FireKey)) {
						player.artifacts[1] = true;
						player.Inventory.remove(Treasure.FireKey);
						discardingDeck.add(Treasure.FireKey);
					} else {
						if(island.board[t.y][t.x].event==Event.Cavern && player.treasureExists(Treasure.WaterKey)) {
							player.artifacts[0] = true;
							player.Inventory.remove(Treasure.WaterKey);
							discardingDeck.add(Treasure.WaterKey);
						} else {
							System.out.println("You cannot do that");
						}
					}
				}
			}
		} else {
			System.out.println("You cannot do that");
		}
	}
}

class Island {
	// Model of the Island
	public Model model;

	// Array to store all the tiles
	public Tile[][] board;

	// Lenght and width of the Island
	public static final int HEIGHT = 6;
	public static final int WIDTH = 6;

	int[][] boardSetup = {
			{0, 0, 1, 1, 0, 0},
			{0, 1, 1, 1, 1, 0},
			{1, 1, 1, 1, 1, 1},
			{1, 1, 1, 1, 1, 1},
			{0, 1, 1, 1, 1, 0},
			{0, 0, 1, 1, 0, 0}
	};

	// Tiles that are yet to be submerged
	public ArrayList<Tile> floodableTiles = new ArrayList<>();

	public Island(Model m) {
		this.model = m;
		board = new Tile[WIDTH][HEIGHT];
		for(int i=0; i<HEIGHT; i++) {
			for(int j=0; j<WIDTH; j++) {
				board[i][j] = new Tile(j, i, m);
			}
		}
		init();
	}

	public void init() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (boardSetup[i][j] == 1) {
					board[i][j].state = State.Dry;
					floodableTiles.add(board[i][j]);
				}
			}
		}
		ArrayList<Tile> specialTiles = new ArrayList<>();
		int tile;
		for(int i = 0; i < 9 ; i++) {
			tile = (int) (Math.random() * (floodableTiles.size() - 1));
			Tile t = floodableTiles.get(tile);
			floodableTiles.remove(tile);
			if(i < 1)
				board[t.y][t.x].setEvent(Event.Heliport);
			else
			if(i < 3)
				board[t.y][t.x].setEvent(Event.Temple);
			else
			if(i < 5)
				board[t.y][t.x].setEvent(Event.Garden);
			else
			if(i < 7)
				board[t.y][t.x].setEvent(Event.Cavern);
			else
				board[t.y][t.x].setEvent(Event.Palace);
			specialTiles.add(board[t.y][t.x]);
		}
		floodableTiles.addAll(specialTiles);
	}

	public void sink() {
		//System.out.print("Sink activated ! \n");
		//System.out.print(floodableTiles.size()+"\n");
		for (int i = 0; i < 3; i++) {
			int tile = (int) (Math.random() * (floodableTiles.size() - 1));
			Tile t = floodableTiles.get(tile);
			floodableTiles.remove(tile);
			board[t.y][t.x].flood();
			if(board[t.y][t.x].getState() != State.Submerged)
				floodableTiles.add(board[t.y][t.x]);
		}
	}

	public boolean eventExists(Event e) {
		for(Tile t:floodableTiles) {
			if(t.event==e)
				return true;
		}
		return false;
	}

	public void dryPlayerZone(Player p){
		if(this.board[p.y][p.x].state == State.Intermediate)
			this.floodableTiles.remove(this.board[p.y][p.x]);
			this.board[p.y][p.x].state = State.Dry;
			this.floodableTiles.add(this.board[p.y][p.x]);
	}
}

enum State
{
	Dry,
	Intermediate,
	Submerged
}

enum Event
{
	Ordinary,
	Heliport,
	Temple,
	Garden,
	Cavern,
	Palace
}

class Tile{
	Model model;
	public State state;
	public Event event;
	boolean isPlayerOnBase;

	public int x;
	public int y;

	public Tile(int i, int j, Model m){
		this.model = m;
		this.x = i;
		this.y = j;
		this.state = State.Submerged;
		this.event = Event.Ordinary;
		this.isPlayerOnBase = false;
	}

	public void setEvent(Event e)
	{
		this.event = e;
	}

	public void flood() {
		if(this.state == State.Intermediate)
			this.state = State.Submerged;
		else
			this.state = State.Intermediate;
	}

	public void playerOnBase(){this.isPlayerOnBase = !this.isPlayerOnBase;}

	public boolean isPlayerOnBaseF(){return isPlayerOnBase;}

	public State getState() {
		return this.state;
	}

	public String toString() { return String.format("(%d, %d) %s %s", x, y, state, event); }
}

enum Treasure
{
	FireKey,
	EarthKey,
	WaterKey,
	AirKey,
	SandBag,
	Helicopter,
	RisingWaters
}

class Player {
	// Model of the island
	public Model model;

	// Player's controller
	public Controller controller;

	// Player's position on the board
	public int x;
	public int y;

	// Artifacts carried by the player: Water - Fire - Earth - Air
	public boolean[] artifacts = new boolean[4];

	// Player's inventory
	public ArrayList<Treasure> Inventory;

	public Player(Model m, int i, int j) {
		this.model = m;
		for(int k = 0; k < 4; k++) {
			this.artifacts[k] = false;
		}
		this.Inventory = new ArrayList<>();
		this.x = i;
		this.y = j;
	}

	public boolean treasureExists(Treasure t) {
		for(Treasure treasure:Inventory) {
			if(treasure==t)
				return true;
		}
		return false;
	}

	public void moveUp() { this.x = this.x + 1; }

	public void moveDown() { this.x = this.x - 1; }

	public void moveRight() { this.y = this.y + 1; }

	public void moveLeft() { this.y = this.y - 1; }

	public String toString() {
		return String.format("(%d, %d) W:%s F:%s E:%s A:%s\n%s",
				x, y, artifacts[0], artifacts[1], artifacts[2], artifacts[3], Inventory);
	}
}