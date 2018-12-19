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

import java.security.Key;
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

    private Text gameOver, win;
    private Text nlevel, totalscor, levellef;

    private boolean initMovement;
    private boolean initContact;
    private boolean level=false;
    private int contadorLevel=0;
    private int jugados;


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
                        brick1.getComponent(BrickComponent.class).crackedBrickset();
                        if(brick1.getComponent(BrickComponent.class).getComponent().remainingHits()==0) {
                            brick1.removeFromWorld();

                            listEntitiesLevel.remove(brick1);
                            getGameState().setValue("score", facade.getCurrentLevel().getActualPoints());
                            getGameState().setValue("totalscore", facade.getCurrentPoints());
                            getGameState().setValue("lives", facade.getBallsLeft());
                        }
                        if(facade.winner()){
                            jugados++;
                            getGameScene().addUINode(win);
                            getGameState().setValue("nlevel", Integer.parseInt(facade.getLevelName())-1);
                            getGameWorld().removeEntity(ball);

                        }

                        else if (facade.getCurrentLevel().getActualPoints()==0 && listEntitiesLevel.size()==0){
                                createLevel(facade.getBricks());
                                for (Entity en : listEntitiesLevel) {
                                    getGameWorld().addEntity(en);
                                }
                                jugados++;
                                getGameState().setValue("nlevel", Integer.parseInt(facade.getLevelName())-1);
                                getGameState().setValue("levelleft", contadorLevel - jugados - 1);
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
        getGameWorld().addEntities(bg,player,ball,walls);
        initVars();
    }

    @Override
    protected void initInput() {
        Input input = getInput();
        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                if(!facade.isGameOver()) {
                    playerControl.right();
                    if (initContact && !playerControl.getPlayerWallRight()) {
                        ball.getComponent(PhysicsComponent.class).setLinearVelocity(130, 0);
                    }
                }
            }
            @Override
            protected void onActionEnd() {
                if(!facade.isGameOver()){
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
                if(!facade.isGameOver()) {
                    playerControl.left();
                    if (initContact && !playerControl.getPlayerWallLeft()) {
                        ball.getComponent(PhysicsComponent.class).setLinearVelocity(-130, 0);
                    }
                }
            }
            @Override
            protected void onActionEnd() {
                if (!facade.isGameOver()) {
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
                if(facade.getBallsLeft()>0 && !facade.isGameOver()) {
                    ball.getComponent(PhysicsComponent.class).setLinearVelocity(-80, -80);
                    initMovement = true;
                    getPhysicsWorld().setGravity(0.1, -0.1);
                    initContact = false;
                }
            }
        }, KeyCode.SPACE);

        input.addAction(new UserAction("Restart Game") {
            @Override
            protected void onActionBegin() {
                if(facade.isGameOver()){
                    getGameWorld().removeEntity(player);
                    player = newPlayer(460, 710);
                    playerControl = player.getComponent(PlayerControl.class);
                    getGameWorld().addEntity(player);
                    initVars();
                    initBall();
                    if(listEntitiesLevel.size()>0){
                        getGameWorld().removeEntities(listEntitiesLevel);
                    }
                }
            }
        }, KeyCode.R);

        input.addAction(new UserAction("New Level") {
            @Override
            protected void onActionBegin() {
                if(!facade.isGameOver()) {
                    int seed = 1;
                    double probB = Math.random();
                    double nivel = Math.random();
                    contadorLevel++;
                    if (nivel > 0.5) {
                        //int numberOfB=(int)( Math.random() * (65 - 60+ 1 ) ) + 60;
                        int numberOfB = (int) (Math.random() * (6 - 6 + 1)) + 6;
                        double probM = Math.random();
                        facade.addPlayingLevel(facade.newLevelWithBricksFull(Integer.toString(contadorLevel), numberOfB, probB, probM, seed));
                    } else {
                        //int numberOfB=(int)( Math.random() * (75 - 70+ 1 ) ) + 70;
                        int numberOfB = (int) (Math.random() * (6 - 6 + 1)) + 6;
                        facade.addPlayingLevel(facade.newLevelWithBricksNoMetal(Integer.toString(contadorLevel), numberOfB, probB, seed));
                    }

                    if (!level) {
                        level = true;
                        createLevel(facade.getBricks());
                        for (Entity en : listEntitiesLevel) {
                            getGameWorld().addEntity(en);
                        }
                    }
                    getGameState().setValue("levelleft", contadorLevel - jugados - 1);
                }

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

        Text textScore = new Text("LEVELSCORE");
        textScore.setFont(fontText);
        textScore.setTranslateX(600);
        textScore.setTranslateY(100);
        textScore.setFill(Color.WHITE);
        getGameScene().addUINode(textScore);

        Text textTotalScore = new Text("TOTALSCORE");
        textTotalScore.setFont(fontText);
        textTotalScore.setTranslateX(600);
        textTotalScore.setTranslateY(50);
        textTotalScore.setFill(Color.WHITE);
        getGameScene().addUINode(textTotalScore);

        Text numerolevel = new Text("LEVEL");
        numerolevel.setFont(fontText);
        numerolevel.setTranslateX(230);
        numerolevel.setTranslateY(100);
        numerolevel.setFill(Color.WHITE);
        getGameScene().addUINode(numerolevel);

        Text levelleft = new Text("LEVELLEFT");
        levelleft .setFont(fontText);
        levelleft .setTranslateX(230);
        levelleft .setTranslateY(50);
        levelleft .setFill(Color.WHITE);
        getGameScene().addUINode(levelleft );

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
        tscore.setTranslateY(100);
        tscore.setTranslateX(850);
        tscore.textProperty().bind(getGameState().intProperty("score").asString());
        getGameScene().addUINode(tscore);

        Text text = getUIFactory().newText("", Color.WHITE, 40);
        text.setFont(fontText);
        text.setTranslateY(50);
        text.setTranslateX(170);
        text.textProperty().bind(getGameState().intProperty("lives").asString());
        getGameScene().addUINode(text);

        levellef = getUIFactory().newText("", Color.WHITE, 40);
        levellef.setFont(fontText);
        levellef.setTranslateY(50);
        levellef.setTranslateX(450);
        levellef.textProperty().bind(getGameState().intProperty("levelleft").asString());
        getGameScene().addUINode(levellef);

        nlevel = getUIFactory().newText("", Color.WHITE, 40);
        nlevel.setFont(fontText);
        nlevel.setTranslateY(100);
        nlevel.setTranslateX(450);
        nlevel.textProperty().bind(getGameState().intProperty("nlevel").asString());
        getGameScene().addUINode(nlevel);

        totalscor = getUIFactory().newText("", Color.WHITE, 40);
        totalscor.setFont(fontText);
        totalscor.setTranslateY(50);
        totalscor.setTranslateX(850);
        totalscor.textProperty().bind(getGameState().intProperty("totalscore").asString());
        getGameScene().addUINode(totalscor);

    }


    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("lives", 3);
        vars.put("score", 0);
        vars.put("nlevel",0);
        vars.put("totalscore",0);
        vars.put("levelleft",0);
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
            } else if (brick.isMetal()){
                s=metal;
            }
            else{//nuevo brick

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

    public void initVars(){
        player.setPosition(460,710);
        facade=new HomeworkTwoFacade();
        initMovement=false;
        initContact=true;
        level=false;
        contadorLevel=0;
        jugados=0;

        getGameScene().removeUINode(win);
        getGameScene().removeUINode(gameOver);
        getGameState().setValue("nlevel", 0);
        getGameState().setValue("levelleft", 0);
        getGameState().setValue("totalscore", 0);
        getGameState().setValue("lives", 3);
        getGameState().setValue("score", 0);
    }

    public static void main(String... args) {
        launch(args);

    }


}