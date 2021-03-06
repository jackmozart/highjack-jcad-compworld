package main;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;


/**
 * 
 * @author Steven Cozart Jonathan Caddey
 *
 */
@SuppressWarnings("serial")
public class Window extends JFrame implements Observer, ActionListener{
	
	
	private static final double MAX_RATIO = 1.8;
	private final AsteroidsGame my_game;
	private final AsteroidsCanvas my_canvas;
	private final JTextField my_score;
	private final JTextField my_level;
	private final JButton my_new_game_button;
	private final NewGameDialog my_new_game_dialog;
	private final JTextField my_bombs;
	
	
	public Window(String the_title){
		super(the_title);
		
		//Enforces a min size of screen and a spect ratio
		addComponentListener(new java.awt.event.ComponentAdapter() {
			  public void componentResized(ComponentEvent event) {
				 int width = getWidth();
				 int hight = getHeight();
				 
				 if(width > (hight *MAX_RATIO)){
					 setSize( (int)(hight * MAX_RATIO ), hight);
				 }else if(hight > (width *MAX_RATIO)){
					 setSize(width, (int)( width * MAX_RATIO )) ;
				 }
				
		
			  }
			});
		
		
		my_game = new AsteroidsGame(this);
		my_game.addObserver(this);
		my_canvas = AsteroidsCanvas.getInstance(my_game);
		my_score = new JTextField(15);
		my_score.setEnabled(false);
		my_level = new JTextField(5);
		my_level.setEnabled(false);
		
		my_bombs = new JTextField(2);
		my_bombs.setText("0");
		my_bombs.setEnabled(false);
		
		my_new_game_dialog = new NewGameDialog(this);
		my_new_game_button = new JButton("New Game...");
		setup();
		JOptionPane.showMessageDialog(null, "Use arrow keys to move, press space to fire,\nand press down arrow for a rechargeable shield.\nUse B to fire a bomb that will destroy everything.\nStart a game with New Game.", "Welcome to Asteroids!", JOptionPane.INFORMATION_MESSAGE);
	}

	
	

	private void setup() {
		//sets the icon to the hulk..... hulk smash
		Image icon = Toolkit.getDefaultToolkit().createImage(
		"src/hulk.jpg");
		this.setIconImage(icon);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		JPanel root = new JPanel();
		root.setLayout(new BorderLayout());
		root.add(my_canvas, BorderLayout.CENTER);
		JPanel status = new JPanel();
		//status.setLayout(new BoxLayout(status, BoxLayout.X_AXIS));
		my_new_game_button.addActionListener(this);
		status.add(my_new_game_button);
		
		JPanel p;
		
		p = new JPanel();
		p.add(new JLabel("Score:"));
		p.add(my_score);
		status.add(p);
		
		p = new JPanel();
		p.add(new JLabel("Level:"));
		p.add(my_level);
		status.add(p);
		
		p = new JPanel();
		p.add(new JLabel("Bombs:"));
		p.add(my_bombs);
		status.add(p);
		
		root.add(status, BorderLayout.NORTH);
		this.add(root);
		
	
		pack();
		this.setMinimumSize(new Dimension(this.getWidth(), 500));
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(size.width * 5 / 8, size.height * 5 / 8);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	@Override
	public void update(Observable the_observer, Object the_arg) {
		if (the_arg.getClass().equals(Long.class)) {
			my_score.setText(String.valueOf((Long) the_arg));
		} else if (the_arg.getClass().equals(Integer.class)) {
			my_level.setText(String.valueOf((Integer) the_arg));
		} else if (the_arg instanceof Boolean) {
			JOptionPane.showMessageDialog(this, "Game Over");
			my_new_game_button.requestFocus();
		}else if(the_arg.getClass().equals(String.class)){
			my_bombs.setText((String) the_arg);
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == my_new_game_button) {
			if (my_new_game_dialog.showDialog()) {
				my_game.setPeer(my_new_game_dialog.getNetworkingPeer());
				my_game.startGame();
				my_canvas.requestFocus();
			}
		}
		
	}
}
