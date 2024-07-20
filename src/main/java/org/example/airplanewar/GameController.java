package org.example.airplanewar;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;

import java.net.URL;
import java.util.*;

public class GameController implements Initializable {

    private static final int UNIT = 10;
    private static final int SHOOT_INTERVAL = 200;

    @FXML
    private ImageView airplaneImageView;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label scoreLabel;
    @FXML
    private ProgressBar healthProgressBar;

    private volatile boolean gameOver = false;
    private boolean shooting = false;
    private Thread shootingThread;
    private ArrayList<ImageView> strangers, playerBullets, strangersBullets;
    private ArrayList<Thread> strangerShootingThreads;
    private int score = 0;
    private AudioClip exp1, exp2;
    private Image explosion;
    private Timeline addBossTimeline;
    private HashMap<ImageView, Health> enemyHealthMap;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        enemyHealthMap = new HashMap<>();
        strangers = new ArrayList<>();
        playerBullets = new ArrayList<>();
        strangersBullets = new ArrayList<>();
        strangerShootingThreads = new ArrayList<>();
        exp1 = new AudioClip(Objects.requireNonNull(getClass().getResource("sounds/explosion1.mp3")).toExternalForm());
        exp2 = new AudioClip(Objects.requireNonNull(getClass().getResource("sounds/explosion2.mp3")).toExternalForm());
        explosion = new Image(Objects.requireNonNull(getClass().getResource("images/explosion.png")).toExternalForm());

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(5);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(Color.BLACK);

        healthProgressBar.setStyle("-fx-accent: #50C878;");

        airplaneImageView.setFocusTraversable(true);
        rootPane.setFocusTraversable(true);
        rootPane.requestFocus();
        rootPane.setOnKeyPressed(this::handleKeyPress);

        Thread strangerCreatorThread = new Thread(() -> {
            while (!gameOver) {
                try {
                    Thread.sleep(1200);
                    Platform.runLater(this::addStranger);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        strangerCreatorThread.setDaemon(true);
        strangerCreatorThread.start();

         addBossTimeline = new Timeline(new KeyFrame(Duration.seconds(15), event -> {
            Platform.runLater(this::addBoss);
        }));
        addBossTimeline.setCycleCount(Timeline.INDEFINITE);
        addBossTimeline.play();

        scoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        updateScore();
    }

    private void handleKeyPress(KeyEvent keyEvent) {
        double x = airplaneImageView.getLayoutX();

        if (keyEvent.getCode() == KeyCode.RIGHT) {
            if (x + UNIT < 730) {
                x += UNIT;
            }
            airplaneImageView.setLayoutX(x);
        } else if (keyEvent.getCode() == KeyCode.LEFT) {
            if (x - UNIT > -10) {
                x -= UNIT;
            }
            airplaneImageView.setLayoutX(x);
        } else if (keyEvent.getCode() == KeyCode.SPACE) {
            toggleShooting();
        }
    }

    private synchronized void toggleShooting() {
        shooting = !shooting;
        if (shooting) {
            startShooting();
        } else {
            stopShooting();
        }
    }

    private void startShooting() {
        shootingThread = new Thread(() -> {
            try {
                while (shooting) {
                    Platform.runLater(this::shootBullet);
                    Thread.sleep(SHOOT_INTERVAL);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        shootingThread.setDaemon(true);
        shootingThread.start();
    }

    private void stopShooting() {
        shooting = false;
        if (shootingThread != null && shootingThread.isAlive()) {
            shootingThread.interrupt();
        }
    }

    private void shootBullet() {
        try {
            Image bulletImage = new Image(Objects.requireNonNull(getClass().getResource("images/missile1.png")).toExternalForm());

            ImageView newBullet = new ImageView(bulletImage);
            newBullet.setFitWidth(25);
            newBullet.setFitHeight(25);
            newBullet.setPreserveRatio(true);

            double bulletX = airplaneImageView.getLayoutX() + 37;
            double bulletY = airplaneImageView.getLayoutY() - 40;
            newBullet.setLayoutX(bulletX);
            newBullet.setLayoutY(bulletY);

            playerBullets.add(newBullet);
            rootPane.getChildren().add(newBullet);

            TranslateTransition bulletMovement = new TranslateTransition();
            bulletMovement.setNode(newBullet);
            bulletMovement.setDuration(Duration.millis(800));
            bulletMovement.setByY(-420);
            bulletMovement.setCycleCount(1);
            bulletMovement.setAutoReverse(false);
            bulletMovement.setOnFinished(event -> {
                rootPane.getChildren().remove(newBullet);
                playerBullets.remove(newBullet);
            });
            bulletMovement.play();

            bulletMovement.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                checkCollisions(newBullet);
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading bullet image.");
        }
    }

    private void strangerShootBullet(ImageView newStranger) {
        try {
            Image bulletImage = new Image(Objects.requireNonNull(getClass().getResource("images/missile2.png")).toExternalForm());

            ImageView newBullet = new ImageView(bulletImage);
            newBullet.setFitWidth(20);
            newBullet.setFitHeight(20);
            newBullet.setPreserveRatio(true);

            double bulletX = newStranger.getLayoutX() + 10;
            double bulletY = newStranger.getLayoutY() + 10;
            newBullet.setLayoutX(bulletX);
            newBullet.setLayoutY(bulletY);

            strangersBullets.add(newBullet);
            rootPane.getChildren().add(newBullet);

            TranslateTransition bulletMovement = new TranslateTransition();
            bulletMovement.setNode(newBullet);
            bulletMovement.setDuration(Duration.millis(1000));
            bulletMovement.setByY(500);
            bulletMovement.setCycleCount(1);
            bulletMovement.setAutoReverse(false);
            bulletMovement.setOnFinished(event -> {
                rootPane.getChildren().remove(newBullet);
                strangersBullets.remove(newBullet);
            });
            bulletMovement.play();

            bulletMovement.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (newBullet.getBoundsInParent().intersects(airplaneImageView.getBoundsInParent())) {
                    rootPane.getChildren().remove(newBullet);
                    healthProgressBar.setProgress(healthProgressBar.getProgress() - 0.001);
                    exp1.play();
                    checkGameOver();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading bullet image.");
        }
    }

    private void bossShootBullet(ImageView boss) {
        Platform.runLater(() -> {
            try {
                Image bulletImage = new Image(Objects.requireNonNull(getClass().getResource("images/missile2.png")).toExternalForm());

                ImageView newBullet = new ImageView(bulletImage);
                newBullet.setFitWidth(40);
                newBullet.setFitHeight(40);
                newBullet.setPreserveRatio(true);

                Random random = new Random();
                double bulletX = random.nextInt(800);
                double bulletY = 0;
                newBullet.setLayoutX(bulletX);
                newBullet.setLayoutY(bulletY);

                strangersBullets.add(newBullet);
                rootPane.getChildren().add(newBullet);

                TranslateTransition bulletMovement = new TranslateTransition();
                bulletMovement.setNode(newBullet);
                bulletMovement.setDuration(Duration.millis(1200));
                bulletMovement.setByY(500);
                bulletMovement.setCycleCount(1);
                bulletMovement.setAutoReverse(false);
                bulletMovement.setOnFinished(event -> {
                    rootPane.getChildren().remove(newBullet);
                    strangersBullets.remove(newBullet);
                });
                bulletMovement.play();

                bulletMovement.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                    if (newBullet.getBoundsInParent().intersects(airplaneImageView.getBoundsInParent())) {
                        rootPane.getChildren().remove(newBullet);
                        healthProgressBar.setProgress(healthProgressBar.getProgress() - 0.01);
                        exp2.play();
                        checkGameOver();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error loading bullet image.");
            }
        });
    }

    private void checkBulletCollisions() {
        ArrayList<ImageView> bulletsToRemove = new ArrayList<>();

        for (ImageView playerBullet : new ArrayList<>(playerBullets)) {
            for (ImageView strangerBullet : new ArrayList<>(strangersBullets)) {
                if (playerBullet.getBoundsInParent().intersects(strangerBullet.getBoundsInParent())) {
                    bulletsToRemove.add(playerBullet);
                    bulletsToRemove.add(strangerBullet);
                    exp1.play();
                }
            }
        }

        Platform.runLater(() -> {
            for (ImageView bullet : bulletsToRemove) {
                rootPane.getChildren().remove(bullet);
                playerBullets.remove(bullet);
                strangersBullets.remove(bullet);
            }
        });
    }

    private void checkCollisions(ImageView bullet) {
        ImageView strangerToRemove = null;
        for (ImageView stranger : new ArrayList<>(strangers)) {
            if (bullet.getBoundsInParent().intersects(stranger.getBoundsInParent())) {
                rootPane.getChildren().remove(bullet);
                playerBullets.remove(bullet);

                Health health = enemyHealthMap.get(stranger);
                health.decreaseHealth(10);

                String[] wholePathImage = stranger.getImage().getUrl().split("/");
                String imageName = wholePathImage[wholePathImage.length - 1];

                if (health.isDead() && !imageName.contains("boss")) {
                    stranger.setImage(explosion);
                    strangerToRemove = stranger;
                    score += 10;
                    updateScore();
                    exp1.play();
                } else if (health.isDead() && imageName.contains("boss")) {
                    stranger.setImage(explosion);
                    strangerToRemove = stranger;
                    score += 100;
                    updateScore();
                    exp2.play();
                }
                break;
            }
        }

        if (strangerToRemove != null) {
            strangers.remove(strangerToRemove);
            enemyHealthMap.remove(strangerToRemove);

            ImageView finalStrangerToRemove = strangerToRemove;
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Platform.runLater(() -> rootPane.getChildren().remove(finalStrangerToRemove));
            }).start();
        }

        checkBulletCollisions();
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    private void addStranger() {
        Image strangerImage = new Image(Objects.requireNonNull(getClass().getResource("images/spaceship.png")).toExternalForm());

        ImageView newStranger = new ImageView();
        newStranger.setImage(strangerImage);
        newStranger.setFitWidth(40);
        newStranger.setFitHeight(40);
        newStranger.setPreserveRatio(true);

        Random random = new Random();
        double randomX = random.nextInt(750);
        double randomY = random.nextInt(100);
        newStranger.setLayoutX(randomX);
        newStranger.setLayoutY(randomY);

        strangers.add(newStranger);

        enemyHealthMap.put(newStranger, new Health(50));

        rootPane.getChildren().add(newStranger);

        Thread strangerShootingThread = new Thread(() -> {
            try {
                while (strangers.contains(newStranger) && !gameOver) {
                    Platform.runLater(() -> strangerShootBullet(newStranger));
                    Thread.sleep(1100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        strangerShootingThread.setDaemon(true);
        strangerShootingThread.start();
        strangerShootingThreads.add(strangerShootingThread);
    }

    private void addBoss() {
        Image bossImage = new Image(Objects.requireNonNull(getClass().getResource("images/boss.png")).toExternalForm());
        ImageView boss = new ImageView(bossImage);
        boss.setLayoutX(0);
        boss.setLayoutY(50);
        boss.setFitWidth(150);
        boss.setFitHeight(150);

        enemyHealthMap.put(boss, new Health(2000));

        TranslateTransition bossMovement = new TranslateTransition();
        bossMovement.setNode(boss);
        bossMovement.setDuration(Duration.millis(2000));
        bossMovement.setFromX(0);
        bossMovement.setByX(650);
        bossMovement.setCycleCount(Timeline.INDEFINITE);
        bossMovement.setAutoReverse(true);

        boss.setEffect(new DropShadow(10, Color.BLACK));
        strangers.add(boss);
        rootPane.getChildren().add(boss);

        bossMovement.play();

        Thread bossShootingThread = new Thread(() -> {
            try {
                while (strangers.contains(boss) && !gameOver) {
                    Platform.runLater(() -> bossShootBullet(boss));
                    Thread.sleep(1200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        bossShootingThread.setDaemon(true);
        bossShootingThread.start();
        strangerShootingThreads.add(bossShootingThread);
    }

    private void checkGameOver() {
        if (healthProgressBar.getProgress() <= 0 && !gameOver) {
            airplaneImageView.setOnKeyPressed(null);
            rootPane.setOnKeyPressed(null);
            gameOver = true;
            Platform.runLater(() -> {
                stopAllThreads();
                showGameOverMessage();
                applyBlurEffect();
                addBossTimeline.stop();
            });
        }
    }

    private void showGameOverMessage() {
        Label gameOverLabel = new Label("Game Over!");
        gameOverLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
        gameOverLabel.setStyle("-fx-text-fill: white;");
        gameOverLabel.setLayoutX((rootPane.getWidth() - 300) / 2);
        gameOverLabel.setLayoutY(rootPane.getHeight() / 2 - 150);

        Label showScoreLabel = new Label("Your Score: " + score);
        showScoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        showScoreLabel.setStyle("-fx-text-fill: white;");
        showScoreLabel.setLayoutX((rootPane.getWidth() - 300) / 2);
        showScoreLabel.setLayoutY(rootPane.getHeight() / 2 - 50);

        rootPane.getChildren().add(gameOverLabel);
        rootPane.getChildren().add(showScoreLabel);

        gameOverLabel.toFront();
        showScoreLabel.toFront();
    }

    private void stopAllThreads() {
        shooting = false;
        if (shootingThread != null && shootingThread.isAlive()) {
            shootingThread.interrupt();
        }

        for (Thread strangerThread : strangerShootingThreads) {
            if (strangerThread.isAlive()) {
                strangerThread.interrupt();
            }
        }
    }

    private void applyBlurEffect() {
        GaussianBlur blur = new GaussianBlur(10);
        for (ImageView stranger : strangers) {
            stranger.setEffect(blur);
        }
        airplaneImageView.setEffect(blur);
        scoreLabel.setEffect(blur);
        healthProgressBar.setEffect(blur);
    }
}
