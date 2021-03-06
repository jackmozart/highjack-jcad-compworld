package main;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JFrame;

import network.Peer;

import phyObj.Asteroid;
import phyObj.Bullet;
import phyObj.CollisionInfo;
import phyObj.EqTriangleAsteroid;
import phyObj.PhyObject;
import phyObj.RightTriangleAsteroid;
import phyObj.Ship;
import phyObj.SquareAsteroid;
import phyObj.Vector2f;
import sound.SoundPlayer;

public class AsteroidsGame extends Observable implements Observer{
	private static final int MAX_BOMBS = 3;
	private static final int RESOLUTION_REPEATS = 100;
	private static final int POINTS_FOR_BOMB = 1000;
	private static final int MAX_ASTEROIDS = 15;
	
	//sound player
	private static final String DEATH_SOUND = "sound/death.wav";
	private static final String BOMB = "sound/DABOMB.wav";
	private static final float LEVEL_TIME = 6;
	private static final float MAX_SIZE = 10;
	private static final float MIN_SIZE = .25f;
	private static final float MAX_MOMENTUM = 50;
	private SoundPlayer my_music;

	
	private SceneGraphNode my_asteroid_root;
	private SceneGraphNode my_bullet_root;

	private float my_field_width;
	private float my_field_height;
	
	private Ship my_ship;
	private final List<Bullet> my_bullets;
	private final List<Asteroid> my_asteroids;
	
	private boolean my_game_over;
	private float my_score;
	private int my_level;
	private Peer my_peer;

	private long my_id;
	
	//bad code ...like really bad
	private JFrame my_frame;
	private int my_bombs;
	private int my_bomb_pointer;

	private Asteroid my_to_add;
	
	private float my_red, my_blue, my_green;
	private int my_min_asteroids;
	private float my_timer;
	private float my_max_size;
	private float my_min_size;
	private float my_max_momentum;
	public AsteroidsGame(JFrame the_frame) {
		my_frame = the_frame;
		
		my_music = new SoundPlayer();
		my_music.preLoad(DEATH_SOUND);
		
		my_asteroid_root = new SceneGraphNode();
		my_bullet_root = new SceneGraphNode();
		my_bullets = new ArrayList<Bullet>();
		my_asteroids = new ArrayList<Asteroid>();
		my_game_over = true;
		
		
		// Add independent SceneGraphNode representing all the HalfSpaces.
//		my_root.addChild(new SceneGraphNode(false) {
//			public void renderGeometry(GLAutoDrawable drawable) {
//				GL2 gl = drawable.getGL().getGL2();
//				gl.glColor3f(1, 1, 1);
//				gl.glBegin(GL.GL_LINE_LOOP);
//				gl.glVertex2f(-5, -5);
//				gl.glVertex2f(5, -5);
//				gl.glVertex2f(5, 5); 
//				gl.glVertex2f(-5, 5);
//				gl.glEnd();
//			}
//		}); 
	}
	
	public void startGame() {
		my_bomb_pointer = POINTS_FOR_BOMB;
		my_bombs = 0;
		my_score = 0;
		my_level = 0;
		
		my_timer = 0;
		setLevel(0);
		
		my_asteroid_root = new SceneGraphNode();
		my_bullet_root = new SceneGraphNode();
		my_bullets.clear();
		my_asteroids.clear();
		my_ship = new Ship();
		setChanged();
		notifyObservers(new Long((long)my_score));
		setChanged();
		notifyObservers(new Integer(my_level));
		setChanged();
		notifyObservers(new String("" + my_bombs));
		my_game_over = false;
		
	}
	
	public void keyPressed(KeyEvent the_e) {
		if (my_game_over) return;
		int code = the_e.getKeyCode();
		switch (code) {
			case KeyEvent.VK_UP:
				my_ship.toggleForward(true);
			break;
			case KeyEvent.VK_LEFT:
				my_ship.toggleLeft(true);
			break;
			case KeyEvent.VK_RIGHT:
				my_ship.toggleRight(true);
			break;
			case KeyEvent.VK_DOWN:
				my_ship.toggleShield(true);
			break;
			case KeyEvent.VK_SPACE:
				my_ship.toggleFire(true);
			break;
			case KeyEvent.VK_B:
				bomb();
			break;	
		}
	}
	
	
	/**
	 * if a bomb is avalible remove all objects shake screen 
	 */
	private void bomb() {
		if(my_bombs > 0 ){
			my_bombs --;
			
			setChanged();
			notifyObservers(new String(""+ my_bombs));
			
			ListIterator<Asteroid> ait = my_asteroids.listIterator();
			while (ait.hasNext()) {
				final Asteroid a = ait.next();
					ait.remove();
					my_asteroid_root.removeChild(a.getRenderable());
			}
			my_music.play(BOMB);
			FrameUtils.vibrate(my_frame);
		}

		

	}

	public void keyReleased(KeyEvent the_e) {
		if (my_game_over) return;
		int code = the_e.getKeyCode();
		switch (code) {
			case KeyEvent.VK_UP:
				my_ship.toggleForward(false);
			break;
			case KeyEvent.VK_LEFT:
				my_ship.toggleLeft(false);
			break;
			case KeyEvent.VK_RIGHT:
				my_ship.toggleRight(false);
			break;
			case KeyEvent.VK_DOWN:
				my_ship.toggleShield(false);
			break;
			case KeyEvent.VK_SPACE:
				my_ship.toggleFire(false);
			break;
		}
	}
	
	
	
	public void display(final GLAutoDrawable the_drawable, final float the_time_passed) {
		
		//update timer for level here
		if(!my_game_over){
			my_timer += the_time_passed;
			if (my_timer > LEVEL_TIME){
				my_timer -= LEVEL_TIME;
				setLevel(my_level+1);
			}
		}
		
		GL2 gl = the_drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
		// Add any asteroids the network may have given us
		if (my_to_add != null) {
			my_asteroids.add(my_to_add);
			my_asteroid_root.addChild(my_to_add.getRenderable());
			my_to_add = null;
		}
		// update bullets
		for (Bullet b : my_bullets) {
			b.updateState(the_time_passed);
		}
		
		
		// update ship
		if (!my_game_over) {
				my_ship.updateState(the_time_passed);
			Vector2f position = my_ship.getPosition();
			float radius = .2f;
			if (position.x < -getWidth() / 2 - radius) {
				my_ship.setPosition(getWidth() + 2 * radius + position.x, position.y);
			} else if (position.x > getWidth() / 2 + radius) {
				my_ship.setPosition(-getWidth() - 2 * radius + position.x, position.y);
			}
			if (position.y < -getHeight() / 2 - radius) {
				my_ship.setPosition(position.x, getHeight() + 2 * radius + position.y);
			} else if (position.y > getHeight() / 2 + radius) {
				my_ship.setPosition(position.x, -getHeight() - 2 * radius + position.y);
			}
		}
		
		
		// add any bullets fired since updating the ships state
		if (!my_game_over) {
			for (Bullet bill : my_ship.getBullets()) {
				my_bullet_root.addChild(bill.getRenderable());
				my_bullets.add(bill);
			}
		}
		
		// update asteroids
		for (Asteroid a : my_asteroids) {
			PhyObject phy = a.getObject();
			phy.updateState(the_time_passed);
			Vector2f position = phy.getPosition();
			final float diameter = a.getObject().getSize();
			long id;
			final float height = getHeight() + diameter;
			final float width = getWidth() + diameter;
			if (position.x < -width / 2) {
				id = (long)((position.y + height / 2) / height * Peer.ID_LIMIT / 4 + Peer.ID_LIMIT * 3 / 4);
				phy.setPosition(position.x + width / 2, 0);
				a.setDestination(id);
				sendAsteroid(a);
			} else if (position.x > width / 2) {
				id = (long)(-(position.y + height / 2) / height * Peer.ID_LIMIT / 4 + Peer.ID_LIMIT / 2);
				phy.setPosition(position.x - width / 2, 0);
				a.setDestination(id);
				sendAsteroid(a);
			} else if (position.y < -height / 2) {
				id = (long)(-(position.x + width / 2) / width * Peer.ID_LIMIT / 4 + Peer.ID_LIMIT * 3 / 4);
				phy.setPosition(0, position.y + height / 2);
				a.setDestination(id);
				sendAsteroid(a);
			} else if (position.y > height / 2) {
				id = (long)((position.x + width / 2) / width * Peer.ID_LIMIT / 4);
				phy.setPosition(0, position.y - height / 2);
				a.setDestination(id);
				sendAsteroid(a);
			}
			
		}
		
		// check for collisions between asteroids and asteroids
		boolean noCollisions = false;
		for (int repeat = 0; repeat < RESOLUTION_REPEATS && !noCollisions; repeat++) {
			noCollisions = true;
			for (int i = 0; i < my_asteroids.size(); i++) {
				Asteroid a = my_asteroids.get(i);
				for (int j = i + 1; j < my_asteroids.size(); j++) {
					Asteroid b = my_asteroids.get(j);
					CollisionInfo cInfo = a.getObject().getCollision(b.getObject());
					if (cInfo != null) {
						noCollisions = false;
						a.getObject().resolveCollision(b.getObject(), cInfo);
					}
				}
			}
		}
		
		// check for collisions between bullets and asteroids
		for (Bullet bill : my_bullets) {
			for (Asteroid a : my_asteroids) {
				CollisionInfo c = bill.getCollision(a.getObject());
				if (c != null) {
					noCollisions = false;
					final float prevSpeed = bill.getVelocity().length();
					bill.resolveCollision(a.getObject(), c);
					final Vector2f velocity = bill.getVelocity();
					velocity.setLength(prevSpeed);
					bill.setVelocity(velocity);
					
					a.decrementHP(bill.getDamage());
					if (!a.isAlive()) {
						my_score += a.getMaxHP();
						
						
						//gives you more bombs!
						if(my_bomb_pointer < my_score){
							if(my_bombs < MAX_BOMBS ){
								my_bombs ++;
							}
							my_bomb_pointer += POINTS_FOR_BOMB;

							setChanged();
							notifyObservers(new String(""+ my_bombs));
						}
						
						setChanged();
						notifyObservers(new Long((long)my_score));
						
						
						
						
					}
					
					bill.bounce();
					
				}
			}
		}
		
		// check for collision between ship and asteroids
		noCollisions = false;
		if (!my_game_over) {
			for (int repeat = 0; repeat < RESOLUTION_REPEATS && !noCollisions; repeat++) {
				for (Asteroid a : my_asteroids) {
					CollisionInfo c = my_ship.getCollision(a.getObject());
					if (c != null) {
						my_ship.resolveCollision(a.getObject(), c);
						my_ship.autoShield();
						if (!my_ship.isShielded()) {
							gameOver();
						}
					}
				}
			}
		}

		// remove any bullets that need to be removed
		Iterator<Bullet> bit = my_bullets.iterator();
		while (bit.hasNext()) {
			final Bullet b = bit.next();
			if (!b.isAlive()) {
				bit.remove();
				my_bullet_root.removeChild(b.getRenderable());
			}
		}
		
		// remove any asteroids that need to be removed
		ListIterator<Asteroid> ait = my_asteroids.listIterator();
		while (ait.hasNext()) {
			final Asteroid a = ait.next();
			if (!a.isAlive()) {
				ait.remove();
				my_asteroid_root.removeChild(a.getRenderable());
				if (a.getDestination() != -1) {
					for (Asteroid b : a.getFragments(my_min_size, my_max_momentum / 20)) {
						ait.add(b);
						my_asteroid_root.addChild(b.getRenderable());
					}
				}
			}
		}
		
		//used to add extra asteroids if the number gets to low
		if (my_asteroids.size() < my_min_asteroids ) {
			addRandomAsteroid();
		}
		

		noCollisions = false;

		
		
		

		
		if (!my_game_over) {
			my_ship.updateRenderable();
		}
		
		for (Bullet b : my_bullets) {
			b.updateRenderable();
		}
		for (Asteroid a : my_asteroids) {
			a.getObject().updateRenderable();
		}
		my_bullet_root.render(the_drawable);
		my_asteroid_root.render(the_drawable);
		if (!my_game_over) {
			my_ship.getRenderable().render(the_drawable);
		}
		
	}

	
	
	private void addRandomAsteroid() {
		double type = Math.random();
		float size = 1 + (my_max_size - 1) * (float) Math.random();
		final Asteroid a;
		final float hp = 7;
		if (type <= .3) {
			a = new EqTriangleAsteroid(my_id, size, hp);
		} else if (type <= .6) {
			a = new RightTriangleAsteroid(my_id, size, hp);
		} else {
			a = new SquareAsteroid(my_id, size, hp);
		}
		final float position1 = (float) Math.random() - .5f;
		final float position2 = Math.random() < .5 ? -.5f : .5f;
		if (Math.random() < .5) {
			a.getObject().setPosition(position1 * (getWidth() + size),
					position2 * (getHeight() + size));
		} else {
			a.getObject().setPosition(position2 * (getWidth() + size),
					position1 * (getHeight() + size));
		}
		final float speed = (1 + (my_max_momentum - 1) * (float) Math.random()) / a.getObject().getMass();
		Vector2f tmp = new Vector2f(0, speed);
		tmp.rotate((float) (Math.random() * 2 * Math.PI));
		a.getObject().setVelocity(tmp);
		if (!my_game_over) {
			Vector2f positionShip = my_ship.getPosition();
			tmp = new Vector2f((float) (3 * Math.random() - 1.5), (float) (3 * Math.random() - 1.5));
			positionShip.sum(tmp);
			tmp = new Vector2f(a.getObject().getPosition());
			positionShip.sumScale(tmp,  -1);
			if (tmp.length() != 0) {
				tmp.setLength(-speed);
				a.getObject().setVelocity(tmp);
			}
		}
		
		//code to set color based on id
		if(my_peer != null){
//			int r = (int) (( (my_id + 1) * 10) % 255);
//			int g = (int)( (my_id + 2 ) * 50) % 255;
//			int b = (int) ( (my_id + 3) * 100) % 255;
			
			a.getRenderable().setRGBf(my_red, my_green, my_blue);
		}

		
		my_asteroids.add(a);
		my_asteroid_root.addChild(a.getRenderable());
	}
	
	private void setLevel(int level){
		my_level = level;
		my_min_asteroids = (int) (MAX_ASTEROIDS - MAX_ASTEROIDS * Math.pow(16f / 17, my_level)) + 1;
		my_max_size = (float) (MAX_SIZE - MAX_SIZE * Math.pow(16f / 17, my_level)) +1;
		my_min_size = (float) (MIN_SIZE + 2 * Math.pow(18f / 19, my_level));
		my_max_momentum = (float) (MAX_MOMENTUM - MAX_MOMENTUM * Math.pow(14f / 15, my_level));
//		System.out.println("max / min size " + my_max_size + " / " + my_min_size);
//		System.out.println("max velocity " + my_max_momentum);
		setChanged();
		notifyObservers(new Integer(my_level));
	}
	
	private void sendAsteroid(Asteroid the_asteroid) {
		receiveAsteroid(the_asteroid);
		if (my_peer != null) {
			my_peer.sendObject(the_asteroid, the_asteroid.getDestination());
			the_asteroid.decrementHP(the_asteroid.getMaxHP());
			the_asteroid.setDestination(-1);
		}
	}
	/**
	 * The asteroid should have its position relative to the border it came from, not the origin.
	 * @param the_asteroid
	 */
	private void receiveAsteroid(Asteroid the_asteroid) {
		Vector2f position = idToPosition(the_asteroid.getDestination(), my_field_width + the_asteroid.getObject().getSize(), my_field_height + the_asteroid.getObject().getSize());
		position.sum(the_asteroid.getObject().getPosition());
		the_asteroid.getObject().setPosition(position.x, position.y);
		
	}
	
	private Vector2f idToPosition(final long the_id, float width, float height) {
		float x = 0;
		float y = 0;
		float id = the_id;
		if (id < Peer.ID_LIMIT / 4) {
			y = -height / 2;
			x = id / (Peer.ID_LIMIT / 4) * width - width / 2;
		} else if (id < Peer.ID_LIMIT / 2) {
			x = -width / 2;
			y = -(id - Peer.ID_LIMIT / 4) / (Peer.ID_LIMIT / 4) * height + height / 2;
		} else if (id < Peer.ID_LIMIT * 3 / 4) {
			y = height / 2;
			x = (id - Peer.ID_LIMIT * 5 / 8) / (Peer.ID_LIMIT / 4) * width;
		} else {
			x = width / 2;
			y = (id - Peer.ID_LIMIT * 3 / 4) / (Peer.ID_LIMIT / 4) * height - height / 2;
		}
		return new Vector2f(x, y);
	}
	
	private void gameOver() {
		if (!my_game_over) {
			my_game_over = true;
			
			//sound
			my_music.play(DEATH_SOUND);
			
			setChanged();
			notifyObservers(new Boolean(true));
		}
	}

	public float getWidth() {
		return my_field_width;
	}

	public void setWidth(float the_field_width) {
		this.my_field_width = the_field_width;
	}

	public float getHeight() {
		return my_field_height;
	}

	public void setHeight(float the_field_height) {
		this.my_field_height = the_field_height;
	}
	
	public void setPeer(final Peer the_peer) {
		if (my_peer != null) {
			my_peer.deleteObserver(this);
		}
		my_peer = the_peer;
		if (my_peer != null) {
			my_peer.addObserver(this);
			my_id = my_peer.getID();
		}
		my_red = (float) Math.random();
		my_blue = (float) Math.random();
		my_green = (float) Math.random();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o.equals(my_peer)) {
			Asteroid a = (Asteroid) arg;
			my_to_add = a;
		}
		
	}

}
