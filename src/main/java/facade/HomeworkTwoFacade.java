package main.java.facade;

import main.java.controller.Game;
import main.java.logic.Visitor;
import main.java.logic.brick.*;
import main.java.logic.level.*;
import main.java.logic.*;

import java.util.*;

/**
 * Facade class to expose the logic of the game to a GUI in the upcoming homework.
 *
 * @author Juan-Pablo Silva
 */
public abstract class HomeworkTwoFacade{
    /**
     * Instance of the game controller.
     *
     * @see Game
     */
    public Game game = new Game(3);

    /**
     * Creates a new level with the given parameters.
     *
     * @param name           the name of the level
     * @param numberOfBricks the number of bricks in the level
     * @param probOfGlass    the probability of a {@link main.java.logic.brick.GlassBrick}
     * @param probOfMetal    the probability of a {@link main.java.logic.brick.MetalBrick}
     * @param seed           the seed for the random number generator
     * @return a new level determined by the parameters
     * @see Level
     */
    public Level newLevelWithBricksFull(String name, int numberOfBricks, double probOfGlass, double probOfMetal, int seed) {
        return game.newLevelWithBricksFull(name,numberOfBricks,probOfGlass,probOfMetal,seed);
    }

    /**
     * Creates a new level with the given parameters with no metal bricks.
     *
     * @param name           the name of the level
     * @param numberOfBricks the number of bricks in the level
     * @param probOfGlass    the probability of a {@link main.java.logic.brick.GlassBrick}
     * @param seed           the seed for the random number generator
     * @return a new level determined by the parameters
     * @see Level
     */
    public Level newLevelWithBricksNoMetal(String name, int numberOfBricks, double probOfGlass, int seed) {
        return game.newLevelWithBricksNoMetal(name,numberOfBricks,probOfGlass,seed);
    }
    /**
     * Gets the number of {@link Brick} in the current level, that are still not destroyed
     *
     *
     * @return the number of intact bricks in the current level
     */
    public int numberOfBricks() {
        return 0;
    }

    /**
     * Gets the list of {@link Brick} in the current level.
     *
     * @return the list of bricks
     */
    public List<Brick> getBricks() {
        return  null;
    }

    /**
     * Whether the current {@link Level}'s next level is playable or not.
     *
     * @return true if the current level's next level is playable, false otherwise
     */
    public boolean hasNextLevel() {
        return false;
    }

    /**
     * Pass to the next level of the current {@link Level}. Ignores all conditions and skip to the next level.
     */
    public void goNextLevel() {

    }

    /**
     * Gets whether the current {@link Level} is playable or not.
     *
     * @return true if the current level is playable, false otherwise
     */
    public boolean hasCurrentLevel() {
        return false;
    }

    /**
     * Gets the name of the current {@link Level}.
     *
     * @return the name of the current level
     */
    public String getLevelName() {
        return null;
    }

    /**
     * Gets the current {@link Level}.
     *
     * @return the current level
     * @see Level
     */
    public Level getCurrentLevel() {
        return null;
    }

    /**
     * Sets a {@link Level} as the current playing level.
     *
     * @param level the level to be used as the current level
     * @see Level
     */
    public void setCurrentLevel(Level level) {

    }

    /**
     * Adds a level to the list of {@link Level} to play. This adds the level in the last position of the list.
     *
     * @param level the level to be added
     */
    public void addPlayingLevel(Level level) {

    }

    /**
     * Gets the number of points required to pass to the next level. Gets the points obtainable in the current {@link Level}.
     *
     * @return the number of points in the current level
     */
    public int getLevelPoints() {
        return 0;
    }

    /**
     * Gets the accumulated points through all levels and current {@link Level}.
     *
     * @return the cumulative points
     */
    public int getCurrentPoints() {
        return 0;
    }

    /**
     * Gets the current number of available balls to play.
     *
     * @return the number of available balls
     */
    public int getBallsLeft() {
        return 0;
    }

    /**
     * Reduces the count of available balls and returns the new number.
     *
     * @return the new number of available balls
     */
    public int dropBall() {
        return 0;
    }

    /**
     * Checks whether the game is over or not. A game is over when the number of available balls are 0 or the player won the game.
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return false;
    }

    /**
     * Gets the state of the player.
     *
     * @return true if the player won the game, false otherwise
     */
    public boolean winner() {
        return game.winner();
    }
}
