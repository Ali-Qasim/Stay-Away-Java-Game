import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class StayAway { //outer class


    public static class Game1 extends JComponent{

        private double accelerate = 9.8; //Using acceleration and velocity to make somewhat realistic physics
        double velocity = 0;

        private int yDist = 563 / 3; //height of player character
        private int xDist; //x-transformation of NPCs

        private boolean init = false;
        private boolean started = true;

        final int LENGTH = 5; //Change this to change game length
        int score = 0;

        Sprite maskMan = new Sprite("src/maskMan.png"); //Player sprite
        final int POS = 100;

        Sprite house = new Sprite("src/house.png"); // House sprite

        List<Integer[]> coords;

        Sprite[] people = {new Sprite("src/pedestrian1.png"), new Sprite("src/pedestrian2.png"), new Sprite("src/pedestrian3.png"), new Sprite("src/pedestrian4.png")};
        //Array of NPC models

        public Game1() throws IOException, InterruptedException {

            JFrame frame = new JFrame();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setPreferredSize(new Dimension(800, 600));
            frame.setResizable(false);
            frame.setTitle("Stay Away! (A game about social distancing)");

            frame.add(this);

            EventHandler e = new EventHandler(this);

            frame.addMouseListener(e);
            frame.addKeyListener(e);

            frame.pack();// make
            frame.setVisible(true);

            populate();//generate coordinates for NPC sprites

            while (started) {

                frame.repaint();

                if (init) {
                    tick();
                }

                Thread.sleep(1000 / 120); //120 fps

            }
                Thread.sleep(10000);

                frame.dispose();//delete old frame
                new Game1();

        }

        public void welcome(Graphics2D g) { // draw welcome screen

            Font normal = g.getFont();
            Font title = new Font("", Font.ITALIC, 30);

            g.setColor(Color.ORANGE);
            g.setFont(title);
            g.drawString("Stay away! (A game about social distancing)", 100, getHeight() / 2 - 75);

            g.setColor(Color.WHITE);
            g.setFont(normal);
            g.drawString("Get home with your shopping while social distancing!", 250, getHeight() / 2);
            g.drawString("Press Spacebar or Left Click to start", 285, getHeight() / 2 + 30);
        }

        public void gameOver(Graphics2D g) throws FileNotFoundException { // draw game-over screen
            Font normal = g.getFont();
            Font over = new Font("TimesRoman", Font.BOLD, 30);

            g.setFont(over);

            if(score == LENGTH){ //if all NPCs are passed, game is won
                g.setColor(Color.green);

                g.drawString("Congratulations!", 255, getHeight() / 2 - 100);
                g.setFont(normal);
                g.drawString("You got home safely", 320, getHeight() / 2 - 75);
            } else {
                g.setColor(Color.RED);

                g.drawString("GAME OVER", 285, getHeight() / 2 - 100);
                g.setFont(normal);
                g.drawString("You didn't stay 6 feet away!", 310, getHeight() / 2 - 75);
            }

            String e = "Your score: " + score;
            g.drawString(e, getWidth() / 2 - 50, getHeight() / 2 - 35);

            g.setFont(new Font("TimesRoman", Font.ITALIC, 20));
            g.drawString("HIGH SCORES", 310, getHeight() / 2 );

            g.setFont(normal);

            updateHighScores();

            Integer[] highScores = (new scores()).getHighScores(); // populate array from file

            for (int i = 0; i < highScores.length; i++) { //display list of high-scores
                g.drawString(Integer.toString(highScores[i]), 375, (getHeight() / 2 +30) +20*i);
            }

        }

        public void populate() { //generate coordinates for NPCs

            coords = new ArrayList<>();

            int x = 500;
            int y;
            for (int i = 0; i < LENGTH; i++) {
                x += (int) (Math.random() * 200 + (300));
                y = (int) (Math.random() * 150);
                coords.add(new Integer[]{x, y, 0});
            }
        }

        public void tick() { //controls physics and movement of NPCs

            if (yDist >= getHeight() - 70 || yDist < 0) {
                velocity *= -0.7;
            }

            velocity = (velocity + accelerate / 60); //to match fps
            yDist = (int) (yDist + velocity);
            accelerate = 5;

            xDist -= 2;

        }

        public void calcScore(int i) {
            for (Integer[] c : coords) {

                int npcPos = c[0] + people[i].getWidth(this) + xDist;

                if (c[2] == 0) {
                    if (npcPos < POS) { //if player passes NPC
                        score++;
                        c[2] = 1;
                    }
                }
                if (score == LENGTH){ //if  all NPCs are passed
                    started = false;
                }
            }
        }

        public void updateHighScores(){
            try {
                Integer[] scores = getHighScores(); //Populate array from file

                for (int i = 0; i < 5  ; i++) {

                    Arrays.sort(scores); //sort array in ascending order so that lowest high-score is replaced first
                    if (score > scores[i]) {
                        scores[i] = score;
                        break;
                    }
                }
                Arrays.sort(scores, Collections.reverseOrder()); // rearrange scores by highest-first

                FileWriter writer = new FileWriter("src/highscores.txt");

                for(int highSc: scores){
                    writer.write("\n"+highSc);
                }
                writer.close();

            } catch (Exception e) { // if file not found

                File highScores = new File("highscores.txt");

                try {

                    FileWriter writer = new FileWriter(highScores);

                    writer.write("\n"+score);
                    writer.close();

                } catch (IOException ioException) {

                    ioException.printStackTrace();

                }
            }
        }

        public Integer[] getHighScores() throws FileNotFoundException { //read scores from file into array
            return readScores();
        }

        private static Integer[] readScores() throws FileNotFoundException {
            Scanner input = new Scanner(new File("src/highscores.txt"));

            input.nextLine(); // skip first line

            Integer[] scores = {0,0,0,0,0}; // initialise array so that empty slots are 0

            for (int i = 0; input.hasNext(); i++) {
                scores[i] = Integer.parseInt(input.nextLine());
            }
            input.close();

            return scores;
        }

        public void drawUI(Graphics2D g) { //display score in frame

            String line = "Score: " + score;
            g.drawString(line, 50, 550);

            g.drawString("[SPACE] or [LEFT CLICK] to jump", getWidth()-250, 550);

        }

        public void drawNPC(Graphics2D g) {
            g.setColor(Color.YELLOW);
            int i = 0;

            for (Integer[] co : coords) { //draw NPCs

                Sprite c = people[i];

                c.drawSprite(g, co[0] + xDist, co[1], this);
                g.fillRect(co[0] + xDist - 30, co[1] + 120, people[i].getWidth(this) + 60, 5);

                g.fillRect(co[0] + xDist - 30, co[1] + 270, people[i].getWidth(this) + 60, 5);

                c.drawSprite(g, co[0] + xDist, co[1] + 330, this);

                collisions(i, co);
                calcScore(i);
                i++;
                if (i > 3) {
                    i = 0;
                }
            }

            house.drawSprite(g,coords.get(LENGTH-1)[0] + xDist +400, 200, this);

        }

        public void collisions(int i, Integer[] coordinates) { // Collision detection


            int borderStart = coordinates[0] + xDist - 30;
            int borderLength = people[i].getWidth(this) + 60;
            if (coordinates[2] == 0) {

                if ((borderStart) <= (POS + maskMan.getWidth(this)) && (borderStart + borderLength) >= POS) {  //if Player's coordinates match with NPC coordinates
                    if ((coordinates[1] + 125 >= yDist) || (coordinates[1] + 275 <= yDist + maskMan.getHeight(this))) {
                        started = false; //trigger gameOver()
                    }
                }
            }
        }

        public void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(Color.BLACK); //Create background
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (!init) { // init == false on start

                welcome(g2); //draws welcome screen and sets init == true

            } else {

                maskMan.drawSprite(g2, POS, yDist, this);

                drawNPC(g2);
                drawUI(g2);

                if (!started) { // On completion/game-over

                    new scores();

                    try {
                        gameOver(g2);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        public static class EventHandler implements MouseListener, KeyListener {

            Game1 game;

            EventHandler(Game1 game) {
                this.game = game;
            }

            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON1) {
                    game.velocity = -2; //Change this value to change height of jumps
                    if (!game.init) { //Start game if not started
                        game.init = true;
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    game.velocity = -2; //Change this value to change height of jumps
                    if (!game.init) { //Start game if not started
                        game.init = true;
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        }
    }

    public static class scores {

        public void updateHighScores(Game1 g){
            try {
                Integer[] scores = getHighScores(); //Populate array from file

                for (int i = 0; i < 5  ; i++) {

                    Arrays.sort(scores); //sort array in ascending order so that lowest high-score is replaced first
                    if (g.score > scores[i]) {
                        scores[i] = g.score;
                        break;
                    }
                }
                Arrays.sort(scores, Collections.reverseOrder()); // rearrange scores by highest-first

                FileWriter writer = new FileWriter("src/highscores.txt");

                for(int highSc: scores){
                    writer.write("\n"+highSc);
                }
                writer.close();

            } catch (Exception e) { // if file not found

                File highScores = new File("highscores.txt");

                try {

                    FileWriter writer = new FileWriter(highScores);

                    writer.write("\n"+g.score);
                    writer.close();

                } catch (IOException ioException) {

                    ioException.printStackTrace();

                }
            }
        }

        public Integer[] getHighScores() throws FileNotFoundException { //read scores from file into array
            return Game1.readScores();
        }
    }

    public static class Sprite extends Image{ //Sprite class extends abstract class image

        String src;// = "src/pedestrian1.png";

        BufferedImage person;

        Sprite(String s) throws IOException {

            src = s;
            person = ImageIO.read(new File(src));

        }

        public void drawSprite(Graphics2D g, int x, int y, ImageObserver a){
            g.drawImage(this.person, x, y, a);
        }

        @Override
        public int getWidth(ImageObserver observer) {
            return person.getWidth();
        }

        @Override
        public int getHeight(ImageObserver observer) {
            return person.getHeight();
        }

        @Override
        public ImageProducer getSource() {
            return person.getSource();
        }

        @Override
        public Graphics getGraphics() {
            return person.getGraphics();
        }

        @Override
        public Object getProperty(String name, ImageObserver observer) {
            return person.getProperty(name, observer);
        }
    }

        public static void main(String[] args) throws InterruptedException, IOException {

            Game1 gt = new Game1();

        }
}
