package view;

import controller.Controller;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Class;
import model.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GUI extends Application {

    private static final String STANDARDTEXT = "class name";
    private static final String CLASSLISTSTART = "Classes in the system:";
    private static final String CLASSIFYTEXT = "file to classify";
    private static final String SELECTTEXT = "select";
    private Controller controller;
    private FileChooser fileChooser;
    private List<Document> inputDocuments;
    private VBox right;
    private TextField className;
    private Text classList;
    private Button fileInput;
    private Button addInput;
    private Button train;
    private Button classify;
    private Text result;
    private boolean classifying;

    @Override
    public void start(Stage primaryStage) throws Exception {
        controller = new Controller();
        fileChooser = new FileChooser();
        BorderPane pane = new BorderPane();
        VBox left = new VBox();
        right = new VBox();
        classList = new Text();
        className = new TextField();
        fileInput = new Button();
        addInput = new Button();
        train = new Button();
        classify = new Button();
        result = new Text();
        classifying = false;

        fileInput.setText("select");
        addInput.setText("add");
        train.setText("train");
        classify.setText("classify");

        pane.setLeft(left);
        pane.setRight(right);

        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(40));

        left.getChildren().add(classList);
        right.getChildren().add(className);
        right.getChildren().add(fileInput);
        right.getChildren().add(addInput);
        right.getChildren().add(train);
        right.getChildren().add(classify);
        right.getChildren().add(result);

        setSelectState();
        updateClassList();

        fileInput.setOnAction(event -> {
            System.out.println("fileInput");
            inputDocuments = new ArrayList<>();
            inputDocuments.addAll(fileChooser.showOpenMultipleDialog(primaryStage).stream().map(Document::new).collect(Collectors.toList()));
            setAddedState();
        });

        addInput.setOnAction(event -> {
            if (validClass()) {
                System.out.println("AddInput");
                controller.addClassWithDocs(new Class(className.getText(), controller.getBaysianClassifier()), inputDocuments);
                setSelectState();
            } else {
                showPopup("Please select a classname", primaryStage);
            }
        });

        train.setOnAction(event -> {
            controller.train();
            setClassifyState();
        });

        classify.setOnAction(event -> {
            if (inputDocuments.size() == 1) {
                Class resultClass = controller.classify(inputDocuments.get(0));
                showResult(resultClass, primaryStage);
            } else {
                showPopup("Please only select one file to Classify", primaryStage);
            }
        });
        Scene scene = new Scene(pane, 343, 150);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showPopup(String s, Stage primaryStage) {
        Stage dialog = new Stage();
        VBox dialogVbox = new VBox();

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        dialogVbox.getChildren().add(new Text(s));

        Scene dialogScene = new Scene(dialogVbox, 150, 100);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void setSelectState() {
        setInvisRight();
        fileInput.setVisible(true);
        fileInput.setText(SELECTTEXT);
        className.setText(STANDARDTEXT);
        displayTrainClassify();
        updateClassList();
    }

    private void setAddedState() {
        if (!classifying) {
            setInvisRight();
            addInput.setVisible(true);
            className.setVisible(true);
            displayTrainClassify();
            updateClassList();
        } else {
            displayTrainClassify();
        }
    }

    private void setClassifyState() {
        setInvisRight();
        fileInput.setVisible(true);
        fileInput.setText(CLASSIFYTEXT);
        displayTrainClassify();
        classifying = true;
        updateClassList();
    }

    private void showResult(Class resultClass, Stage primaryStage) {
        showPopup(resultClass.toString(), primaryStage);
    }

    private void displayTrainClassify() {
        if (controller.canTrain() && !controller.canClassify() && !className.isVisible()) {
            train.setVisible(true);
        }
        if (controller.canClassify() && classifying) {
            classify.setVisible(true);
        }
    }

    private void setInvisRight() {
        for (Node n : right.getChildren()) n.setVisible(false);
    }

    private boolean validClass() {
        return !className.getText().equals(STANDARDTEXT) && !className.getText().equals("");
    }

    private void updateClassList() {
        String list = CLASSLISTSTART;
        for (Class c : controller.getClassAndDocs().keySet()) {
            list += "\n\t" + c.getName() + "\t\t" + controller.getClassAndDocs().get(c).size();
        }
        classList.setText(list);
    }
}