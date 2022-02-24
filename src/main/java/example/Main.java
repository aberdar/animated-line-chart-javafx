package example;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;

// 10:00
public class Main extends Application {

    private List<CanvasLineChart> charts = new ArrayList<>(); // хранение линейных диаграмм

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(800, 600);

        Canvas canvas = new Canvas(800, 600); // создаем холст для отрисовки
        GraphicsContext g = canvas.getGraphicsContext2D(); // добавдяем графический контекст

        // добавление графиков
        charts.add(new CanvasLineChart(g, Color.RED, new RandomDataSource()));
        charts.add(new CanvasLineChart(g, Color.GREEN, new RandomDataSource()));
        charts.add(new CanvasLineChart(g, Color.BLUE, () -> Math.random() * 0.3)); // отличае в построение

        // таймер для анимирования
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                g.clearRect(0, 0, 800, 600); // очистка графического контеста

                charts.forEach(CanvasLineChart::update); // добавляем и обновляем диаграммы
            }
        };
        timer.start();

        root.getChildren().add(canvas); // добавляем камеры в графическую сцену

        return root;
    }

    // динамическая линейная диаграмма
    private static class CanvasLineChart {
        private GraphicsContext g; // графический контекст для рисования
        private Color color; // цвет линейной диаграммы
        private DataSource<Double> dataSource; // иситочник данных

        private Deque<Double> buffer = new ArrayDeque<>(); // буфер для отслеживания данных с течением времени

        private double oldX = -1;
        private double oldY = -1;

        public CanvasLineChart(GraphicsContext g, Color color, DataSource<Double> dataSource) {
            this.g = g;
            this.color = color;
            this.dataSource = dataSource;
        }

        public void update() {
            double value = dataSource.getValue();

            buffer.addLast(value);

            if (buffer.size() > 800) {
                buffer.removeFirst(); // если больше 800 убираем первуй пункт
            }

            // рендеринг
            g.setStroke(color); // цвет линии

            buffer.forEach(y -> {
                if (oldY > -1) {
                    // [0..1] * 600 = [0..600]
                    g.strokeLine(oldX, oldY * 600, oldX+1, y * 600); // создание линии
                    // oldX + 1 так как есть взаимно однозначное соответствие между количестовом пикселей
                    // и количеством значений в буфере
                }

                oldX = oldX + 1;
                oldY = y;
            });

            oldX = -1;
            oldY = -1;
        }
    }

    // случайный источник данных
    private static class RandomDataSource implements DataSource<Double> {

        private Random random = new Random();

        @Override
        public Double getValue() {
            return random.nextDouble();
        }
    }

    // интерфес источника данных
    private interface DataSource<T> {
        T getValue();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
