/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * @author Simon Denier
 * @since Aug 27, 2010
 *
 */
public class ControlCircle {

	public static final int StrokeWidth = 3;

	public static final int ControlDiameter = 30;

	private String code;

	private Point position;
	
	private Point drawPosition;

	private String status;

	private StringBuilder labelBuffer;
	
	
	ControlCircle(String code, Point position) {
		this.code = code;
		this.position = position;
		this.drawPosition = new Point(position.x - ControlDiameter / 2, position.y - ControlDiameter / 2);
		resetStatus();
	}
	
	public String getCode() {
		return code;
	}

	public Point getPosition() {
		return position;
	}

	public Point translate(int dx, int dy) {
		this.position.translate(dx, dy);
		this.drawPosition.translate(dx, dy);
		return this.position;
	}

	public void drawOn(Graphics2D g2) {
		g2.setStroke(new BasicStroke(StrokeWidth));
		g2.setColor(getStatusColor());
		g2.fillOval(position.x - 1, position.y - 1, 3, 3);
		g2.drawOval(drawPosition.x, drawPosition.y, ControlDiameter, ControlDiameter);
		g2.drawString(getLabel(), drawPosition.x + ControlDiameter, drawPosition.y + ControlDiameter + 10);
	}

	private String getLabel() {
		if( labelBuffer==null ) {
			return code;
		} else {
			return labelBuffer + "-" + code; //$NON-NLS-1$
		}
	}
	
	public Color getStatusColor() {
		if (status == "missed") { //$NON-NLS-1$
			return Color.red;
		}
		if (status == "ok") { //$NON-NLS-1$
			return Color.magenta;
		}
		if (status == "added") { //$NON-NLS-1$
			return Color.blue;
		}
		return Color.magenta;
	}

	public void resetStatus() {
		this.status = "default"; //$NON-NLS-1$
		this.labelBuffer = null;
	}
	
	public void beOkControl(String order) {
		// default < added < ok < missed
		if( this.status!="missed" ) { //$NON-NLS-1$
			this.status = "ok"; //$NON-NLS-1$
		}
		addLabel(order);
	}
	
	public void beMissedControl(String order) {
		// default < added < ok < missed
		this.status = "missed"; //$NON-NLS-1$
		addLabel(order);
	}

	public void beAddedControl() {
		// default < added < ok < missed
		if( this.status=="default" ) { //$NON-NLS-1$
			this.status = "added"; //$NON-NLS-1$
		}
	}
	
	private void addLabel(String order) {
		if( labelBuffer==null ) {
			labelBuffer = new StringBuilder(order);
		} else {
			labelBuffer.append("/").append(order); //$NON-NLS-1$
		}
	}

	
}
