package phyObj;

import java.io.Serializable;

public class Vector2f implements Serializable{
	
	private static final long serialVersionUID = 42L;
	public float x;
	public float y;
	
	public Vector2f() {
		this(0, 0);
	}
	
	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2f(Vector2f other) {
		if (other == null)
			throw new IllegalArgumentException();

		x = other.x;
		y = other.y;
	}

	public float length() {
		return (float)Math.sqrt(x * x + y * y);
	}

	public float dot(Vector2f other) {
		if (other == null)
			throw new IllegalArgumentException();
		
		return x * other.x + y * other.y;
	}

	// Fake cross product Z component
	public float cross(Vector2f other) {
		return x * other.y - other.x * y;
	}

	public void sum(Vector2f other) {
		if (other == null)
			throw new IllegalArgumentException();
		
		x += other.x;
		y += other.y;
	}
	
	public void scale(float s) {
		x *= s;
		y *= s;
	}
	
	public void sumScale(Vector2f other, float s) {
		if (other == null)
			throw new IllegalArgumentException();
		
		x += other.x * s;
		y += other.y * s;
	}
	
	public void normalize() {
		float length = length();
		if (length == 0)
			throw new IllegalStateException();
		
		scale(1 / length);
	}
	
	public void rotate(float radians) {
		float oldX = x;
		x = (float)(Math.cos(radians) * x - Math.sin(radians) * y);
		y = (float)(Math.sin(radians) * oldX + Math.cos(radians) * y);
	}
	
	public void setLength(float mag) {
		if (length() != 0) {
			scale(mag / length());
		}
	}
	
	
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
	
}
