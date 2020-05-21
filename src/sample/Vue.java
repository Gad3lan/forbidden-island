package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;

import java.util.HashMap;
import java.util.Map;


interface Observer {
	void update();
}

public class Vue extends BorderPane{
	VueGrid grid;
	VueCommand commands;
	VueCommand commands2;
	VueCommand commands3;
	static double vueWidth = Screen.getPrimary().getBounds().getWidth();
	static double vueHeight = Screen.getPrimary().getBounds().getHeight();
	Vue(Model model) {
		grid = new VueGrid(model);
		commands = new VueCommand(model);
		setCenter(grid);
		setRight(commands);
	}
	Scene getGameScene() {
		return new Scene(this, vueWidth, vueHeight);
	}
}

class VueGrid extends GridPane implements Observer{
	Model m;
	VueTile[][] tiles;
	Map<String, Image> images;
	int cavernNb = 1;
	int templeNb = 1;
	int gardenNb = 1;
	int palaceNb = 1;

	static void test(Object o) {
		System.out.println(o);
	}
	VueGrid(Model model) {
		this.m = model;
		this.m.addObserver(this);
		this.setVgap(5);
		this.setHgap(5);
		Tile[][] modelBoard = model.getBoard();
		tiles = new VueTile[Island.WIDTH][Island.HEIGHT];
		images = loadImages();
		VueTile selectedTile;
		Controller ctrl = new Controller(m);
		double size = Math.min(
				(Vue.vueWidth*0.65) / Island.WIDTH,
				(Vue.vueHeight*0.95) / Island.HEIGHT);
		for (int y = 0; y < Island.HEIGHT; y++) {
			for (int x = 0; x < Island.WIDTH; x++) {
				if (modelBoard[y][x].getState() != State.Submerged) {
					tiles[y][x] = new VueTile(x, y, images.get(getName(modelBoard[y][x].event)), size, model, ctrl);
					if (modelBoard[y][x].isPlayerOnBaseF()) {
						tiles[y][x].displayPlayer(images.get("Player1"));
					}
					this.add(tiles[y][x], x, y);
				}
			}
		}
		setAlignment(Pos.CENTER);
	}

	String getName(Event e) {
		switch (e) {
			case Cavern -> {
				if (cavernNb == 1) {
					cavernNb++;
					return e.toString() + "1";
				}
				return e.toString() + "2";
			}
			case Garden -> {
				if (gardenNb == 1) {
					gardenNb++;
					return e.toString() + "1";
				}
				return e.toString() + "2";
			}
			case Palace -> {
				if (palaceNb == 1) {
					palaceNb++;
					return e.toString() + "1";
				}
				return e.toString() + "2";
			}
			case Temple -> {
				if (templeNb == 1) {
					templeNb++;
					return e.toString() + "1";
				}
				return e.toString() + "2";
			}
			default -> {
				return e.toString();
			}
		}
	}

	Map<String, Image> loadImages() {
		Map<String, Image> dict = new HashMap<>();
		dict.put("Player1", new Image(getClass().getResource("../Images/Joueur1.png").toExternalForm()));
		dict.put("Player2", new Image(getClass().getResource("../Images/Joueur2.png").toExternalForm()));
		dict.put("Player3", new Image(getClass().getResource("../Images/Joueur3.png").toExternalForm()));
		dict.put("Player4", new Image(getClass().getResource("../Images/Joueur4.png").toExternalForm()));
		dict.put("Ordinary", new Image(getClass().getResource("../Images/JardinsDesHurlements.png").toExternalForm()));
		dict.put("Cavern1", new Image(getClass().getResource("../Images/CaverneDesOmbres.png").toExternalForm()));
		dict.put("Cavern2", new Image(getClass().getResource("../Images/JardinsDesHurlements.png").toExternalForm()));
		dict.put("Garden1", new Image(getClass().getResource("../Images/JardinsDesHurlements.png").toExternalForm()));
		dict.put("Garden2", new Image(getClass().getResource("../Images/JardinsDesHurlements.png").toExternalForm()));
		dict.put("Palace1", new Image(getClass().getResource("../Images/JardinsDesHurlements.png").toExternalForm()));
		dict.put("Palace2", new Image(getClass().getResource("../Images/JardinsDesHurlements.png").toExternalForm()));
		dict.put("Temple1", new Image(getClass().getResource("../Images/TempleDuSoleil.png").toExternalForm()));
		dict.put("Temple2", new Image(getClass().getResource("../Images/TempleDeLaLune.png").toExternalForm()));
		dict.put("Heliport", new Image(getClass().getResource("../Images/JardinsDesHurlements.png").toExternalForm()));
		dict.put("Observatory", new Image(getClass().getResource("../Images/Observatoire.png").toExternalForm()));
		return dict;
	}

	@Override
	public void update() {
		//System.out.print("Update activated ! \n");
		Tile[][] board = m.getBoard();
		for (int y = 0; y < Island.HEIGHT; y++) {
			for (int x = 0; x < Island.WIDTH; x++) {
				if (tiles[y][x] != null) {
					if (board[y][x].isPlayerOnBaseF() && !tiles[y][x].havePlayer) {
						tiles[y][x].displayPlayer(images.get("Player1"));

					} else if (!board[y][x].isPlayerOnBaseF() && tiles[y][x].havePlayer) {
						tiles[y][x].removePlayer();
					}
					if (board[y][x].getState() == State.Dry && tiles[y][x].isFlooded) {
						tiles[y][x].dry();
					} else if (board[y][x].getState() == State.Intermediate && !tiles[y][x].isFlooded) {
						tiles[y][x].sink();
					} else if (board[y][x].getState() == State.Submerged) {
						getChildren().remove(tiles[y][x]);
						tiles[y][x] = null;
					}
				}
			}
		}
	}
}

class VueTile extends Pane {
	final int x, y;
	private double size;
	boolean isFlooded;
	boolean havePlayer;
	boolean selected;
	ImageView imageView;
	ImageView playerView;
	Model model;

	public VueTile(int x, int y, Image image, double size, Model m, Controller ctrl) {
		this.model = m;
		this.x = x;
		this.y = y;
		this.size = size*0.975;
		this.isFlooded = false;
		this.selected = false;
		imageView = new ImageView(image);
		imageView.setFitWidth(this.size);
		imageView.setFitHeight(this.size);
		imageView.setTranslateX(size*0.0125);
		imageView.setTranslateY(size*0.0125);
		getChildren().add(imageView);
		this.setPrefWidth(size);
		this.setPrefHeight(size);
		this.setOnMouseClicked((MouseEvent e) -> {
			ctrl.tileClicked(e);
		});
	}

	void dry() {
		imageView.setEffect(null);
		isFlooded = false;
	}
	void sink() {
		Lighting lighting = new Lighting();
		lighting.setDiffuseConstant(1.0);
		lighting.setSpecularConstant(0.0);
		lighting.setSpecularExponent(0.0);
		lighting.setSurfaceScale(1.0);
		lighting.setLight(new Light.Distant(45, 45, Color.AQUA));
		imageView.setEffect(lighting);
		isFlooded = true;
	}

	static void test2(double x, double y) {
		System.out.println(x + ", " + y);
	}

	void displayPlayer(Image player) {
		playerView = new ImageView(player);
		double playerRatio = player.getHeight()/player.getWidth();
		playerView.setFitWidth(this.size/5.);
		playerView.setFitHeight(this.size/5.*playerRatio);

		playerView.setX((Math.random() * (2*this.size/3. - playerView.getFitWidth())) + this.size/6.);
		playerView.setY((Math.random() * (2*this.size/3. - playerView.getFitHeight())) + this.size/6.);
		getChildren().add(playerView);
		havePlayer = true;
	}

	void removePlayer() {
		getChildren().remove(playerView);
		havePlayer = false;
	}

	void select() {
		setStyle("-fx-background-color: black");
		selected = true;
	}

	void deselect() {
		setStyle(null);
		selected = false;
	}
}

class VueCommand extends VBox{
	private Model m;


	VueCommand(Model model) {
		getChildren().add(new VueCommandActions(model, "End Button"));
		getChildren().add(new VueCommandActions(model, "Dry the current zone"));
		getChildren().add(new VueCommandActions(model, "Move Player"));
		setPrefWidth(Vue.vueWidth * 0.15);
		setAlignment(Pos.CENTER_LEFT);
	}
}

class VueCommandActions extends Pane implements EventHandler<ActionEvent>{
	Controller ctrl;

	VueCommandActions(Model model, String role) {
		switch (role) {
			case "End Button" -> {
				Button endButton = new Button(role);
				this.ctrl = new Controller(model, endButton);
				endButton.setOnAction(this);
				double buttonSize = (Vue.vueWidth - Vue.vueHeight) / 2;
				endButton.setPrefWidth(Vue.vueWidth*0.15);
				endButton.setPrefHeight(50);
				getChildren().add(endButton);
			}
			case "Dry the current zone" -> {
				Button dryZone = new Button(role);
				this.ctrl = new Controller(model, dryZone);
				dryZone.setOnAction(this::dryZone);
				double buttonSize = (Vue.vueWidth - Vue.vueHeight) / 2;
				dryZone.setPrefWidth(Vue.vueWidth*0.15);
				dryZone.setPrefHeight(50);
				getChildren().add(dryZone);
			}
			case "Move Player" -> {
				Button movePlayer = new Button(role);
				this.ctrl = new Controller(model, movePlayer);
				movePlayer.setOnAction(this::movePlayer);
				double buttonSize = (Vue.vueWidth - Vue.vueHeight) / 2;
				movePlayer.setPrefWidth(Vue.vueWidth*0.15);
				movePlayer.setPrefHeight(50);
				getChildren().add(movePlayer);
			}
		}
	}
	@Override
	public void handle(ActionEvent actionEvent) {this.ctrl.handle(actionEvent);}
	public void dryZone(ActionEvent e){this.ctrl.dryZone(e);}
	public void movePlayer(ActionEvent e){this.ctrl.moveTo(e);}
	public void search(ActionEvent e){this.ctrl.searchZone(e);}
}
