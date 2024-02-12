/* 
 * CellularAutomaton1D.java
 * 
 * Created: 02-08-2024
 * By: Kazuma Tsujii
 * 
 * 一次元セル・オートマトン．
*/
package org;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class OneDimCellularAutomaton extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int CELL_SIZE = 5;

    private int[] cells;
    private int[] rule;
    
    private int[][] board = new int[HEIGHT/CELL_SIZE][WIDTH/CELL_SIZE];
    private int row = 1;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.setCenter(canvas);

        primaryStage.setTitle("Cellular Automaton");
        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();

        initAutomaton();
        drawAutomaton(canvas.getGraphicsContext2D());
        
        //Thread.sleep()を描画毎に実行したいが，JavaFxアプリケーションスレッド内でsleepすると全ての処理が停止する．
        //描画処理はJavaFxアプリケーションスレッドのみ行える．バックグラウンドスレッドから描画処理を行う場合はPlatform.runLater()を使用する．
        //ScheduledService: Background thread for JavaFx. Recall the task when it's done.
        ScheduledService<Boolean> service = new ScheduledService<Boolean>() {
        	@Override
        	protected Task<Boolean> createTask(){
        		
        		Task<Boolean> task = new Task<Boolean>() {
                	@Override
                	protected Boolean call(){
                		updateAutomaton();
                		//drawAutomaton()はアプリケーションスレッドで実行される．
                        Platform.runLater(() -> drawAutomaton(canvas.getGraphicsContext2D()));
                        try {
                        	Thread.sleep(50); // Adjust speed here
                            } 
                        catch (InterruptedException e) {
                        	e.printStackTrace();
                            }
                		return true;
                	}
                };
                
                return task;    
        	}
        	
        };
        
        service.start();
        
    }

    private void initAutomaton() {
        cells = new int[WIDTH / CELL_SIZE];
        //0~255の二進数でRuleを定義（配列には小さな位から入れる）
        rule = new int[]{0, 1, 1, 1, 1, 0, 0, 0}; // Rule 30

        // Initialize cells
        for (int i = 0; i < cells.length; i++) {
        	cells[i] = 0; 
        	//cells[i] = Math.random() < 0.5 ? 1 : 0;	
        }
        
        cells[cells.length/2] = 1;
        
        board[0] = cells;
    }

    private void updateAutomaton() {
        int[] nextGeneration = new int[cells.length];
        for (int i = 0; i < cells.length; i++) {
        	int right = cells[(i+1) % cells.length];
        	int center = cells[i];
        	int left = cells[(i+ cells.length - 1) % cells.length];
        	
        	//left,center,rightを３桁の二進数として，その値をruleのインデックスとする．
        	//Ex. 110(2) = 6, nextGeneration[i] <- rule[6]=0
        	nextGeneration[i] = rule[left*4 + center*2 + right];
        }
        cells = nextGeneration;
        if(row == board.length) {
        	for(int i = 0; i < board.length - 1; i++) board[i] = board[i+1];
        	board[board.length - 1] = cells;
        } else {
        	board[row] = cells;
        	row++;
        }
    }

    private void drawAutomaton(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        for (int i = 0; i < board.length; i++) {
        	for(int j = 0; j < board[0].length; j++) {
        		if (board[i][j] == 1) {
        			gc.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);		
        		}        		
        	}
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
