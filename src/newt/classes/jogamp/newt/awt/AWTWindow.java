/*
 * Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright (c) 2010 JogAmp Community. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 */

package jogamp.newt.awt;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.Insets;
import javax.media.nativewindow.NativeWindowException;
import javax.media.nativewindow.awt.AWTGraphicsDevice;
import javax.media.nativewindow.awt.AWTGraphicsScreen;
import javax.media.nativewindow.util.Point;
import jogamp.newt.WindowImpl;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;
import com.jogamp.newt.event.awt.AWTWindowAdapter;

/** An implementation of the Newt Window class built using the
    AWT. This is provided for convenience of porting to platforms
    supporting Java SE. */

public class AWTWindow extends WindowImpl {

    public AWTWindow() {
        this(null);
    }

    public static Class[] getCustomConstructorArgumentTypes() {
        return new Class[] { Container.class } ;
    }

    public AWTWindow(Container container) {
        super();
        title = "AWT NewtWindow";
        this.container = container;
        if(container instanceof Frame) {
            frame = (Frame) container;
        }
    }

    private boolean owningFrame;
    private Container container = null;
    private Frame frame = null; // same instance as container, just for impl. convenience
    private AWTCanvas canvas;

    protected void requestFocusImpl(boolean reparented) {
        container.requestFocus();
    }

    @Override
    protected void setTitleImpl(final String title) {
        if (frame != null) {
            frame.setTitle(title);
        }
    }

    protected void createNativeImpl() {
        if(0!=getParentWindowHandle()) {
            throw new RuntimeException("Window parenting not supported in AWT, use AWTWindow(Frame) cstr for wrapping instead");
        }

        if(null==container) {
            frame = new Frame();
            container = frame;
            owningFrame=true;
        } else {
            owningFrame=false;
            width = container.getWidth();
            height = container.getHeight();
            x = container.getX();
            y = container.getY();
        }
        if(null!=frame) {
            frame.setTitle(getTitle());
        }
        container.setLayout(new BorderLayout());
        canvas = new AWTCanvas(capsRequested, AWTWindow.this.capabilitiesChooser);

        addWindowListener(new LocalWindowListener());

        new AWTMouseAdapter(this).addTo(canvas); // fwd all AWT Mouse events to here
        new AWTKeyAdapter(this).addTo(canvas); // fwd all AWT Key events to here

        // canvas.addComponentListener(listener);
        container.add(canvas, BorderLayout.CENTER);
        container.setSize(width, height);
        container.setLocation(x, y);
        new AWTWindowAdapter(this).addTo(container); // fwd all AWT Window events to here

        if(null!=frame) {
            frame.setUndecorated(undecorated||fullscreen);
        }

        setWindowHandle(1); // just a marker ..
    }

    protected void closeNativeImpl() {
        setWindowHandle(0); // just a marker ..
        if(null!=container) {
            container.setVisible(false);
            container.remove(canvas);
            container.setEnabled(false);
            canvas.setEnabled(false);
        }
        if(owningFrame && null!=frame) {
            frame.dispose();
            owningFrame=false;
            frame = null;
        }
    }

    @Override
    public boolean hasDeviceChanged() {
        boolean res = canvas.hasDeviceChanged();
        if(res) {
            config = canvas.getAWTGraphicsConfiguration();
            if (config == null) {
                throw new NativeWindowException("Error Device change null GraphicsConfiguration: "+this);
            }
            updateDeviceData();
        }
        return res;
    }

    protected void setVisibleImpl(final boolean visible, int x, int y, int width, int height) {
        container.setVisible(visible);

        reconfigureWindowImpl(x, y, width, height, false, 0, 0);
        config = canvas.getAWTGraphicsConfiguration();

        if (config == null) {
            throw new NativeWindowException("Error choosing GraphicsConfiguration creating window: "+this);
        }

        updateDeviceData();
        visibleChanged(visible);
    }

    private void updateDeviceData() {
        // propagate new info ..
        ((AWTScreen)getScreen()).setAWTGraphicsScreen((AWTGraphicsScreen)config.getScreen());
        ((AWTDisplay)getScreen().getDisplay()).setAWTGraphicsDevice((AWTGraphicsDevice)config.getScreen().getDevice());

        final DisplayMode mode = ((AWTGraphicsDevice)config.getScreen().getDevice()).getGraphicsDevice().getDisplayMode();
        if(null != mode) {
            ((AWTScreen)getScreen()).setScreenSize(mode.getWidth(), mode.getHeight());
        }
        
    }

    @Override
    public javax.media.nativewindow.util.Insets getInsets() {
        final int insets[] = new int[] { 0, 0, 0, 0 };
        Insets contInsets = container.getInsets();
        insets[0] = contInsets.top;
        insets[1] = contInsets.left;
        insets[2] = contInsets.bottom;
        insets[3] = contInsets.right;
        return new javax.media.nativewindow.util.Insets(insets[0],insets[1],insets[2],insets[3]);
    }

    protected boolean reconfigureWindowImpl(final int x, final int y, final int width, final int height, final boolean parentChange, final int fullScreenChange, final int decorationChange) {
        if(decorationChange!=0 && null!=frame) {
            if(!container.isDisplayable()) {
                frame.setUndecorated(isUndecorated());
            } else {
                if(DEBUG_IMPLEMENTATION || DEBUG_WINDOW_EVENT) {
                    System.err.println("AWTWindow can't undecorate already created frame");
                }
            }
        }
        int _x=(x>=0)?x:AWTWindow.this.x;
        int _y=(x>=0)?y:AWTWindow.this.y;
        int _w=(width>0)?width:AWTWindow.this.width;
        int _h=(height>0)?height:AWTWindow.this.height;

        container.setLocation(_x, _y);
        Insets insets = container.getInsets();
        container.setSize(_w + insets.left + insets.right,
                          _h + insets.top + insets.bottom);
        return true;
    }

    protected Point getLocationOnScreenImpl(int x, int y) {
        java.awt.Point ap = canvas.getLocationOnScreen();
        ap.translate(x, y);
        return new Point((int)(ap.getX()+0.5),(int)(ap.getY()+0.5));
    }
   
    @Override
    public Object getWrappedWindow() {
        return canvas;
    }

    class LocalWindowListener extends com.jogamp.newt.event.WindowAdapter { 
        @Override
        public void windowMoved(com.jogamp.newt.event.WindowEvent e) {
            if(null!=container) {
                x = container.getX();
                y = container.getY();
            }
        }
        @Override
        public void windowResized(com.jogamp.newt.event.WindowEvent e) {
            if(null!=canvas) {
                width = canvas.getWidth();
                height = canvas.getHeight();
            }
        }
    }
}
