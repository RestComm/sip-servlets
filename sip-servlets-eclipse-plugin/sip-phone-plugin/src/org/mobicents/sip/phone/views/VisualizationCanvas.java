package org.mobicents.sip.phone.views;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class VisualizationCanvas extends Canvas implements VisualizationService{

	private int width;
	private int height;
	private int rate;
	private int max = 200;
	private int i = 0;
	private Point[] dataBuffer;
	
	public VisualizationCanvas(final Composite parent, int style, final int width, final int height, int rate) {
		super(parent, style);
		
		this.width = width;
		this.height = height;
		this.rate = rate;
		this.dataBuffer = new Point[width];
		setSize(width, height);
		setLayoutData(new RowData(width, height));
		addPaintListener(new PaintListener(){

			@Override
			public void paintControl(PaintEvent e) {
				int w = width;
				int h = height;
				Image cached = new Image(parent.getDisplay(), w, h);
				GC buffer = new GC(cached);
				buffer.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
				buffer.setForeground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
				buffer.fillRectangle(0, 0, w, h);
				if(dataBuffer == null) return;
				int verticalCenter = height/2;
				for(int q=0; q<w; q++) {
					Point a = dataBuffer[q];
					if(a != null) {
						buffer.drawLine(q, verticalCenter + a.x, q, verticalCenter+a.y);
					} else {
						buffer.drawLine(q, verticalCenter + 0, q, verticalCenter+0);
					}
				}
				
				e.gc.drawImage(cached, 0, 0);
				buffer.dispose();
			}
			
		});
		
		final Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				try {
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							try {
								redraw();
							} catch (Throwable throwable) {
								t.cancel();
							}
						}
					});
				} catch (Exception e) {}
			}
			
		}, 0, rate);
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.sip.phone.views.VisualizationService#addValue(org.eclipse.swt.graphics.Point)
	 */
	public synchronized void addValue(Point value) {
		for(int q=0; q<dataBuffer.length-1; q++) {
			dataBuffer[q] = dataBuffer[q+1];
		}
		this.max = Math.max(this.max, value.x);
		this.max = Math.max(this.max, value.y);
		value.x = (value.x*this.height/2)/this.max;
		value.y = (value.y*this.height/2)/this.max;
		
		dataBuffer[dataBuffer.length-1] = value;
	}
	
	public synchronized void setLimits(int max) {
		this.max = max;
	}

}
