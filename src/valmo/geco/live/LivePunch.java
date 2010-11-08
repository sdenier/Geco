/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.Date;

import valmo.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Aug 27, 2010
 *
 */
public class LivePunch implements Trace {

	private ControlCircle mapControl;
	
	private LivePunch nextPunch;
	
	private LivePunch nextMissedPunch;
	
	private boolean missed;

	private boolean added;
	
	private String order;

	public LivePunch(ControlCircle control) {
		this.mapControl = control;
	}
	
	public LivePunch(ControlCircle control, int order) {
		this(control);
		this.order = Integer.toString(order);
	}
	
	public LivePunch clone() {
		try {
			LivePunch clone = (LivePunch) super.clone();
			if( clone.nextPunch!=null ) {
				clone.setNextPunch(nextPunch.clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public LivePunch getNextPunch() {
		return nextPunch;
	}
	
	public void setNextPunch(LivePunch punch) {
		nextPunch = punch;
	}
	
	public void nextPunchMissed() {
		nextPunch.beMissed();
		if( nextMissedPunch==null ) {
			nextMissedPunch = nextPunch;
		}
	}
	
	public void beMissed() {
		mapControl.beMissedControl(order);
		missed = true;
	}
	
	public void beOk() {
		mapControl.beOkControl(order);
	}
	
	public void beAdded() {
		mapControl.beAddedControl();
		added = true;
	}

	public void drawOn(Graphics2D g2) {
		mapControl.drawOn(g2);
		if( nextPunch!=null ) {
			BasicStroke stroke;
			Color color;
			if( isMP() ) {
				stroke = missedStroke();
				color = getColor();
			} else {
				stroke = new BasicStroke(ControlCircle.StrokeWidth);
				if( nextMissedPunch!=null || nextPunch.isAdded() ) {
					color = Color.cyan;
				} else {
					color = Color.magenta; //nextPunch.getColor();
				}
			}
			drawLine(
					this,
					nextPunch, 
					stroke,
					color,
					g2);
			if( !isMP() || nextPunch.isMP() ){
				nextPunch.drawOn(g2);
			}
		}
		if( nextMissedPunch!=null ) {
			drawLine(
				this,
				nextMissedPunch, 
				missedStroke(),
				nextMissedPunch.getColor(),
				g2);
			nextMissedPunch.drawOn(g2);
		}
	}

	private BasicStroke missedStroke() {
		return new BasicStroke(ControlCircle.StrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, new float[] {20f, 15f}, 0f);
	}
	
	public void drawLine(LivePunch start, LivePunch end, Stroke stroke, Color color, Graphics2D g2) {
		g2.setStroke(stroke);				
		g2.setColor(color);
		int startX = start.getPosition().x;
		int endX = end.getPosition().x;
		int sX = Math.min(startX, endX);
		int eX = Math.max(startX, endX);
		int sY, eY;
		if( sX==startX ) {
			sY = start.getPosition().y;
			eY = end.getPosition().y;
		} else {
			sY = end.getPosition().y;
			eY = start.getPosition().y;
		}
		double dx = eX - sX;
		double dy = eY - sY;
		
		int clip = (int) (ControlCircle.ControlDiameter * 0.9f);
		if( dx * dx + dy * dy > Math.pow(2 * clip, 2)) {
			int diagX = (int) (clip * Math.cos(Math.atan(dy / dx)));
			int diagY = (int) (clip * Math.sin(Math.atan(dy / dx)));
			g2.drawLine(sX + diagX, sY + diagY, eX - diagX, eY - diagY);
		}
	}
	
	/* (non-Javadoc)
	 * @see valmo.geco.model.Trace#getCode()
	 */
	@Override
	public String getCode() {
		return mapControl.getCode();
	}
	@Override
	public String getBasicCode() {
		if( isOK() ) {
			return mapControl.getCode();
		} else {
			return mapControl.getCode().substring(1); // works dumbly with subst trace
		}
	}

	/* (non-Javadoc)
	 * @see valmo.geco.model.Trace#getTime()
	 */
	@Override
	public Date getTime() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Point getPosition() {
		return mapControl.getPosition();
	}

	public Color getColor() {
		return mapControl.getStatusColor();
	}

	@Override
	public boolean isOK() {
		return !( isMP() || isAdded());
	}
	@Override
	public boolean isMP() {
		return missed;
	}
	@Override
	public boolean isAdded() {
		return added;
	}
	@Override
	public boolean isSubst() {
		// subst should have been split in MP + ADD
		return false;
	}
}
