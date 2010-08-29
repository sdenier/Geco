/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * @author Simon Denier
 * @since Aug 27, 2010
 *
 */
public class MapControl {

	public static final int StrokeWidth = 4;

	public static final int ControlDiameter = 40;

	private String code;

	private Point position;
	
	private String status;
	
	
	MapControl(String code, Point position) {
		this.code = code;
		this.position = position;
		resetStatus();
	}
	
	public String getCode() {
		return code;
	}

	public Point getPosition() {
		return position;
	}

	public void drawOn(Graphics2D g2) {
		g2.setStroke(new BasicStroke(StrokeWidth));
		g2.setColor(getStatusColor());
		g2.drawOval(position.x, position.y, ControlDiameter, ControlDiameter);
		g2.drawString(code, position.x + ControlDiameter, position.y + ControlDiameter + 20);
	}
	
	public Color getStatusColor() {
		if (status == "missed") {
			return Color.red;
		}
		if (status == "ok") {
			return Color.magenta;
		}
		if (status == "added") {
			return Color.blue;
		}
		return Color.magenta;
	}

	public void resetStatus() {
		this.status = "default";
	}
	
	public void beOkControl() {
		// default < added < ok < missed
		if( this.status!="missed" ) {
			this.status = "ok";
		}
	}
	
	public void beMissedControl() {
		// default < added < ok < missed
		this.status = "missed";
	}
	
	public void beAddedControl() {
		// default < added < ok < missed
		if( this.status=="default" ) {
			this.status = "added";
		}
	}

	
}
