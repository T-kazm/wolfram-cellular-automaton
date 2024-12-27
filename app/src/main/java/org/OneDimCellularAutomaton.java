/* 
 *  Name: CellularAutomaton1D
 *  
 *  Created: 2024/02/08
 *  Last Updated: 2024/12/27
 *  By: Kazuma Tsujii
 *  
 *  Description: 一次元セル・オートマトンシミュレータ
*/

package org;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class OneDimCellularAutomaton extends Application {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 400;
    private static final int CELL_SIZE = 5;

    private int[] cells;
    private int[] rule;

    private int[][] board = new int[HEIGHT / CELL_SIZE][WIDTH / CELL_SIZE];
    private int row = 1;

    private ScheduledService<Boolean> service;
    private int updateDelay = 50; // 初期速度

    @Override
    public void start(Stage primaryStage) {
        // レイアウト設定
        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.setCenter(canvas);

        VBox controls = new VBox(10);
        controls.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // UI要素の作成
        HBox ruleAndBtns = new HBox(10);
        Label ruleLabel = new Label("Rule (0-255):");
        TextField ruleInput = new TextField("30"); // 初期値はルール30
        ruleInput.setPrefWidth(100);

        Button startButton = new Button("Start");
        Button stopButton = new Button("Stop");
        ruleAndBtns.getChildren().addAll(ruleLabel, ruleInput, startButton, stopButton);

        Label speedLabel = new Label("Speed:");
        Slider speedSlider = new Slider(10, 1000, updateDelay);

        controls.getChildren().addAll(ruleAndBtns, speedLabel, speedSlider);
        root.setBottom(controls);

        Scene scene = new Scene(root, WIDTH, HEIGHT + 150);
        primaryStage.setTitle("Cellular Automaton");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 初期化
        initAutomaton(30);
        drawAutomaton(canvas.getGraphicsContext2D());

        service = createService(canvas.getGraphicsContext2D());

        // スタートボタンのアクション
        startButton.setOnAction(e -> {
            int ruleNumber = parseRule(ruleInput.getText());
            initAutomaton(ruleNumber);
            drawAutomaton(canvas.getGraphicsContext2D());
            if (service.isRunning()) {
                service.cancel();
            }
            service.restart();
        });

        // ストップボタンのアクション
        stopButton.setOnAction(e -> {
            if (service.isRunning()) {
                service.cancel();
            }
        });

        // スピードスライダーのアクション
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateDelay = newVal.intValue());
    }

    private ScheduledService<Boolean> createService(GraphicsContext gc) {
        ScheduledService<Boolean> newService = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<>() {
                    @Override
                    protected Boolean call() {
                        updateAutomaton();
                        Platform.runLater(() -> drawAutomaton(gc));
                        try {
                            Thread.sleep(updateDelay); // アップデート速度を調整
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                };
            }
        };
        return newService;
    }

    private void initAutomaton(int ruleNumber) {
        cells = new int[WIDTH / CELL_SIZE];
        rule = parseRuleToArray(ruleNumber);

        // セルの初期化
        for (int i = 0; i < cells.length; i++) {
            cells[i] = 0;
        }
        cells[cells.length / 2] = 1;

        board = new int[HEIGHT / CELL_SIZE][WIDTH / CELL_SIZE];
        board[0] = cells;
        row = 1;
    }

    private void updateAutomaton() {
        int[] nextGeneration = new int[cells.length];
        for (int i = 0; i < cells.length; i++) {
            int right = cells[(i + 1) % cells.length];
            int center = cells[i];
            int left = cells[(i + cells.length - 1) % cells.length];
            nextGeneration[i] = rule[left * 4 + center * 2 + right];
        }
        cells = nextGeneration;
        if (row == board.length) {
            System.arraycopy(board, 1, board, 0, board.length - 1);
            board[board.length - 1] = cells;
        } else {
            board[row] = cells;
            row++;
        }
    }

    private void drawAutomaton(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 1) {
                    gc.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    private int parseRule(String input) {
        try {
            int ruleNumber = Integer.parseInt(input);
            if (ruleNumber < 0 || ruleNumber > 255) {
                throw new NumberFormatException("Rule must be between 0 and 255.");
            }
            return ruleNumber;
        } catch (NumberFormatException e) {
            System.err.println("Invalid rule input. Defaulting to Rule 30.");
            return 30;
        }
    }

    private int[] parseRuleToArray(int ruleNumber) {
        int[] ruleArray = new int[8];
        for (int i = 0; i < 8; i++) {
            ruleArray[i] = (ruleNumber >> i) & 1;
        }
        return ruleArray;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
