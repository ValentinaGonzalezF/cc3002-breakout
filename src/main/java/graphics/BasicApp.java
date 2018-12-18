package graphics;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.settings.GameSettings;
import com.almasb.fxgl.ui.FontType;
import facade.HomeworkTwoFacade;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import logic.brick.Brick;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static graphics.ExampleGameFactory.*;

public class BasicApp extends GameApplication {
    private PlayerControl playerControl;
    private HomeworkTwoFacade facade;
    private List<Entity> listEntitiesLevel;
    private Entity player;
    private Entity ball;

    private Text gameOver,win;
    private Text nlevel,totalscor;

    private boolean initMovement=false;
    private boolean winner=false;
    private boolean initContact=true;
    private boolean level=false;
    private int contadorLevel=0;


    public enum ExampleType {
        PLAYER,BALL,WALL,BRICK
    }
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1000);
        gameSettings.setHeight(780);
        gameSettings.setTitle("BreakOut");
        gameSettings.setVersion("");
    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(ExampleType.BALL, ExampleType.WALL) {
                    @Override
                    protected void onHitBoxTrigger(Entity ball1, Entity wall,
                                                   HitBox boxBall, HitBox boxWall) {
                        if (boxWall.getName().equals("BOT")) {
                            ball1.removeFromWorld();
                            facade.dropBall();
                            getGameState().setValue("lives", facade.getBallsLeft());
                            if(facade.getBallsLeft()>0){
                                initBall();
                            }
                            else{
                                getGameScene().addUINode(gameOver);
                            }
                        }
                    }
                });

        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(ExampleType.PLAYER, ExampleType.WALL) {
                    @Override
                    protected void onHitBoxTrigger(Entity player, Entity wall,
                                                   HitBox boxPlayer, HitBox boxWall) {
                        playerControl.setVelocity(0);
                        if (boxWall.getName().equals("RIGHT")) {
                            playerControl.setPlayerWallRight(true);


                        }
                        if (boxWall.getName().equals("LEFT")) {
                            playerControl.setPlayerWallLeft(true);


                        }
                    }
                });
        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(ExampleType.BALL, ExampleType.BRICK) {
                    @Override
                    protected void onHitBoxTrigger(Entity ball, Entity brick1,
                                                   HitBox boxBall, HitBox boxBrick) {
                        getAudioPlayer().playSound("Block_Destroy.wav");

                        brick1.getComponent(BrickComponent.class).getComponent().hit();
                        if(brick1.getComponent(BrickComponent.class).getComponent().remainingHits()==0) {
                            brick1.removeFromWorld();

                            listEntitiesLevel.remove(brick1);
                            getGameState().setValue("score", facade.getCurrentLevel().getActualPoints());
                            getGameState().setValue("totalscore", facade.getCurrentPoints());
                            getGameState().setValue("lives", facade.getBallsLeft());
                        }
                        if(facade.winner()){
                            getGameScene().addUINode(win);
                            getGameWorld().removeEntity(ball);
                            winner=true;
                        }

                        else if (facade.getCurrentLevel().getActualPoints()==0 && listEntitiesLevel.size()==0){
                                createLevel(facade.getBricks());
                                for (Entity en : listEntitiesLevel) {
                                    getGameWorld().addEntity(en);
                                }
                                getGameState().setValue("nlevel", Integer.parseInt(facade.getLevelName()));
                                getGameWorld().removeEntity(ball);
                                initBall();
                        }
                    }
                });

        getPhysicsWorld().addCollisionHandler(
                new CollisionHandler(ExampleType.BALL, ExampleType.PLAYER) {
                    @Override
                    protected void onHitBoxTrigger(Entity ball, Entity player,
                                                   HitBox boxBall, HitBox boxPlayer) {
                        if(!initContact){
                            getAudioPlayer().playSound("Ball_Bounce.wav");
                        }
                    }
                });
    }
    @Override
    protected void initGame() {
        Entity bg= newBackground();
        player = newPlayer(460, 710);
        playerControl = player.getComponent(PlayerControl.class);
        Entity walls=newWalls();
        ball=newBall(player.getX()+50,player.getY()-3);
        facade= new HomeworkTwoFacade();
        getGameWorld().addEntities(bg,player,ball,walls);
    }

    @Override
    protected void initInput() {
        Input input = getInput();
        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                if(!winner) {
                    playerControl.right();
                    if (initContact && !playerControl.getPlayerWallRight()) {
                        ball.getComponent(PhysicsComponent.class).setLinearVelocity(130, 0);
                    }
                }
            }
            @Override
            protected void onActionEnd() {
                if(!winner) {
                    playerControl.stop();
                    if (!initMovement) {
                        ball.getComponent(PhysicsComponent.class).setLinearVelocity(0, 0);
                    }
                }
            }
        }, KeyCode.RIGHT);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                if(!winner) {
                    playerControl.left();
                    if (initContact && !playerControl.getPlayerWallLeft()) {
                        ball.getComponent(PhysicsComponent.class).setLinearVelocity(-130, 0);
                    }
                }
            }
            @Override
            protected void onActionEnd() {
                if (!winner) {
                    playerControl.stop();
                    if (!initMovement) {
                        ball.getComponent(PhysicsComponent.class).setLinearVelocity(0, 0);
                    }
                }
            }
        }, KeyCode.LEFT);

        input.addAction(new UserAction("Move Ball") {
            @Override
            protected void onAction() {
                if(facade.getBallsLeft()>0 && !winner) {
                    ball.getComponent(PhysicsComponent.class).setLinearVelocity(-80, -80);
                    initMovement = true;
                    getPhysicsWorld().setGravity(0.0, 0.0);
                    initContact = false;
                }
            }
        }, KeyCode.SPACE);

        input.addAction(new UserAction("New Level") {
            @Override
            protected void onActionBegin() {
                int seed= 1;
                double probB= Math.random();
                double nivel=Math.random();
                contadorLevel++;
                    if (nivel > 0.5) {
                        //int numberOfB=(int)( Math.random() * (75 - 70+ 1 ) ) + 70;
                        int numberOfB = (int) (Math.random() * (8 - 7 + 1)) + 7;
                        double probM = Math.random();
                        facade.addPlayingLevel(facade.newLevelWithBricksFull(Integer.toString(contadorLevel), numberOfB, probB, probM, seed));
                    } else {
                        //int numberOfB=(int)( Math.random() * (80 - 70+ 1 ) ) + 70;
                        int numberOfB = (int) (Math.random() * (8 - 7 + 1)) + 7;
                        facade.addPlayingLevel(facade.newLevelWithBricksNoMetal(Integer.toString(contadorLevel), numberOfB, probB, seed));
                    }

                    if (!level) {
                        level = true;
                        createLevel(facade.getBricks());
                        for (Entity en : listEntitiesLevel) {
                            getGameWorld().addEntity(en);
                        }
                    }
                    getGameState().setValue("nlevel", Integer.parseInt(facade.getLevelName()));
                    System.out.print(contadorLevel+"\n");
            }
        }, KeyCode.N);
    }

    @Override
    protected void initUI() {
        Font fontText = getUIFactory().newFont(FontType.GAME, 40.0);
        Font fontText1 = getUIFactory().newFont(FontType.TEXT, 170.0);
        Text textLive = new Text("LIVES");
        textLive.setFont(fontText);
        textLive.setTranslateX(50);
        textLive.setTranslateY(50);
        textLive.setFill(Color.WHITE);
        getGameScene().addUINode(textLive);

        Text textScore = new Text("SCORE");
        textScore.setFont(fontText);
        textScore.setTranslateX(230);
        textScore.setTranslateY(50);
        textScore.setFill(Color.WHITE);
        getGameScene().addUINode(textScore);

        Text textTotalScore = new Text("TOTALSCORE");
        textTotalScore.setFont(fontText);
        textTotalScore.setTranslateX(480);
        textTotalScore.setTranslateY(50);
        textTotalScore.setFill(Color.WHITE);
        getGameScene().addUINode(textTotalScore);

        Text numerolevel = new Text("LEVEL");
        numerolevel.setFont(fontText);
        numerolevel.setTranslateX(830);
        numerolevel.setTranslateY(50);
        numerolevel.setFill(Color.WHITE);
        getGameScene().addUINode(numerolevel);

        gameOver = new Text("GAMEOVER");
        gameOver.setFont(fontText1);
        gameOver.setTranslateX(100);
        gameOver.setTranslateY(450);
        gameOver.setFill(Color.WHITE);

        win = new Text("YOU WIN");
        win.setFont(fontText1);
        win.setTranslateX(100);
        win.setTranslateY(450);
        win.setFill(Color.WHITE);

        Text tscore = getUIFactory().newText("", Color.WHITE, 40);
        tscore.setFont(fontText);
        tscore.setTranslateY(50);
        tscore.setTranslateX(350);
        tscore.textProperty().bind(getGameState().intProperty("score").asString());
        getGameScene().addUINode(tscore);

        Text text = getUIFactory().newText("", Color.WHITE, 40);
        text.setFont(fontText);
        text.setTranslateY(50);
        text.setTranslateX(170);
        text.textProperty().bind(getGameState().intProperty("lives").asString());
        getGameScene().addUINode(text);

        nlevel = getUIFactory().newText("", Color.WHITE, 40);
        nlevel.setFont(fontText);
        nlevel.setTranslateY(50);
        nlevel.setTranslateX(950);
        nlevel.textProperty().bind(getGameState().intProperty("nlevel").asString());
        getGameScene().addUINode(nlevel);

        totalscor = getUIFactory().newText("", Color.WHITE, 40);
        totalscor.setFont(fontText);
        totalscor.setTranslateY(50);
        totalscor.setTranslateX(700);
        totalscor.textProperty().bind(getGameState().intProperty("totalscore").asString());
        getGameScene().addUINode(totalscor);

    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("lives", 3);
        vars.put("score", 0);
        vars.put("nlevel",0);
        vars.put("totalscore",0);
    }

    public void createLevel(List <Brick> listBrick){
        String glass="glass.png";
        String metal="metal.png";
        String wooden="wood.png";
        String s;
        List <Entity> listBrickEntities=new ArrayList();
        int x=0;
        int y=120;
        int dx=100;
        int dy=30;
        Collections.shuffle(listBrick);
        for(Brick brick:listBrick) {
            if (x >= 1000) {
                x = 0;
                y += dy;
            }
            if (brick.isGlass()) {
                s=glass;
            } else if (brick.isWooden()) {
                s=wooden;
            } else {
                s=metal;
            }
            listBrickEntities.add(newBrick(x, y,s,brick));
            x += dx;
        }
        listEntitiesLevel=listBrickEntities;
    }
    public void initBall(){
        ball=newBall(player.getX()+50,player.getY()-3);
        getGameWorld().addEntity(ball);
        initMovement=false;
        initContact=true;
    }

    public static void main(String... args) {
        launch(args);

    }


}