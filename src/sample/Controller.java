package sample;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;


public class Controller {

    Model model;
    Button button;
    VueTile tile;

    Controller(Model m, Button button){
        this.model = m;
        this.button = button;
    }

    Controller(Model m) {
        this.model = m;
    }

    public void tileClicked(MouseEvent e) {
        if (tile != null)
            tile.deselect();
        VueTile t = (VueTile) e.getSource();
        tile = t;
        model.x = t.x;
        model.y = t.y;
        t.select();
    }

    public void handle(ActionEvent actionEvent) {
        if(actionEvent.getSource() == button)
            model.newTurn();
    }

    public void dryZone(ActionEvent e) {
        if(e.getSource() == button)
            model.dryCurrentZone(model.island.board[model.y][model.x]);
    }

    public void moveTo(ActionEvent e) {
        if(e.getSource() == button)
            model.moveToTile(model.island.board[model.y][model.x]);
    }

    public void searchZone(ActionEvent e) {
        if(e.getSource() == button)
            model.searchCurrentZone(model.island.board[model.y][model.x]);
    }
}