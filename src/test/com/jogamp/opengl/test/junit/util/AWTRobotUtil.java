/**
 * Copyright 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
 
package com.jogamp.opengl.test.junit.util;

import jogamp.newt.WindowImplAccess;
import java.lang.reflect.InvocationTargetException;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

public class AWTRobotUtil {

    public static int TIME_OUT = 1000; // 1s
    public static int ROBOT_DELAY = 50; // ms
    public static int POLL_DIVIDER = 20; // TO/20

    public static Point getCenterLocation(Object obj, boolean frameTitlebar) 
        throws InterruptedException, InvocationTargetException {
        Component comp = null;
        com.jogamp.newt.Window win = null;

        if(obj instanceof com.jogamp.newt.Window) {
            win = (com.jogamp.newt.Window) obj;
        } else if(obj instanceof Component) {
            comp = (Component) obj;
        } else {
            throw new RuntimeException("Neither AWT nor NEWT: "+obj);
        }

        int x0, y0;
        if(null!=comp) {
            Point p0 = comp.getLocationOnScreen();
            Rectangle r0 = comp.getBounds();
            if( frameTitlebar && comp instanceof JFrame ) {
                JFrame jFrame = (JFrame) comp;
                Container cont = jFrame.getContentPane();
                Point p1 = cont.getLocationOnScreen();
                int dx = (int) ( r0.getWidth() / 2.0 + .5 );
                int dy = (int) ( ( p1.getY() - p0.getY() ) / 2.0 + .5 );
                x0 = (int) ( p0.getX() + dx + .5 ) ;
                y0 = (int) ( p0.getY() + dy + .5 ) ;
            } else {
                x0 = (int) ( p0.getX() + r0.getWidth()  / 2.0 + .5 ) ;
                y0 = (int) ( p0.getY() + r0.getHeight() / 2.0 + .5 ) ;
            }
        } else {
            javax.media.nativewindow.util.Point p0 = win.getLocationOnScreen(null);
            p0.translate(win.getWidth()/2, win.getHeight()/2);
            x0 = p0.getX();
            y0 = p0.getY();
        }

        return new Point(x0, y0);
    }

    /**
     * toFront, call setVisible(true) and toFront(),
     * after positioning the mouse in the middle of the window via robot.
     * If the given robot is null, a new one is created (waitForIdle=true).
     *
     * @return True if the Window became the global focused Window within TIME_OUT
     */
    public static boolean toFront(Robot robot, final java.awt.Window window)
        throws AWTException, InterruptedException, InvocationTargetException {

        if(null == robot) {
            robot = new Robot();
            robot.setAutoWaitForIdle(true);
        }
        Point p0 = getCenterLocation(window, false);
        System.err.println("robot pos: "+p0);
        robot.mouseMove( (int) p0.getX(), (int) p0.getY() );
        robot.delay(ROBOT_DELAY);

        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                window.setVisible(true);
                window.toFront();
                window.requestFocus();
            }});
        robot.delay(ROBOT_DELAY);

        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        int wait;
        for (wait=0; wait<POLL_DIVIDER && window != kfm.getFocusedWindow(); wait++) {
            Thread.sleep(TIME_OUT/POLL_DIVIDER);
        }
        return wait<POLL_DIVIDER;
    }

    /**
     * centerMouse
     */
    public static void centerMouse(Robot robot, Object obj) 
        throws AWTException, InterruptedException, InvocationTargetException {

        if(null == robot) {
            robot = new Robot();
            robot.setAutoWaitForIdle(true);
        }

        Point p0 = getCenterLocation(obj, false);
        System.err.println("robot pos: "+p0);

        robot.mouseMove( (int) p0.getX(), (int) p0.getY() );
        robot.delay(ROBOT_DELAY);
    }

    /**
     * requestFocus, if robot is valid, use mouse operation,
     * otherwise programatic, ie call requestFocus
     */
    public static void requestFocus(Robot robot, Object obj) 
        throws AWTException, InterruptedException, InvocationTargetException {
        Component comp = null;
        com.jogamp.newt.Window win = null;

        if(obj instanceof com.jogamp.newt.Window) {
            win = (com.jogamp.newt.Window) obj;
        } else if(obj instanceof Component) {
            comp = (Component) obj;
        } else {
            throw new RuntimeException("Neither AWT nor NEWT: "+obj);
        }

        if(null == robot) {
            if(null!=comp) {
                final Component f_comp = comp;
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        f_comp.requestFocus();
                    }});
            } else {
                win.requestFocus();
            }
            return;
        }

        centerMouse(robot, obj);

        robot.delay(ROBOT_DELAY);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(ROBOT_DELAY);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(ROBOT_DELAY);
    }

    /**
     *
     * @return True if the Window became the global focused Window within TIME_OUT
     */
    public static boolean waitForFocus(Object obj) throws InterruptedException {
        int wait;
        if(obj instanceof Component) {
            Component comp = (Component) obj;
            KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            for (wait=0; wait<POLL_DIVIDER && comp != kfm.getPermanentFocusOwner(); wait++) {
                Thread.sleep(TIME_OUT/POLL_DIVIDER);
            }
        } else if(obj instanceof com.jogamp.newt.Window) {
            com.jogamp.newt.Window win = (com.jogamp.newt.Window) obj;
            for (wait=0; wait<POLL_DIVIDER && !win.hasFocus(); wait++) {
                Thread.sleep(TIME_OUT/POLL_DIVIDER);
            }
        } else {
            throw new RuntimeException("Neither AWT nor NEWT: "+obj);
        }
        return wait<POLL_DIVIDER;
    }

    /**
     *
     * @return True if the Window became the global focused Window within TIME_OUT
     */
    public static boolean waitForFocus(Object obj, int gainT0, EventCountAdapter gain, 
                                                   int lostT0, EventCountAdapter lost) throws InterruptedException {
        if(!waitForFocus(obj)) {
            return false;
        }
        int wait;
        for (wait=0; wait<POLL_DIVIDER; wait++) {
            int gainT1 = gain.getCount();
            int lostT1 = (null!=lost) ? lost.getCount() : -1;
            if(gainT1-gainT0==1 && lostT1-lostT0==-1) {
                return true;
            }
            Thread.sleep(TIME_OUT/POLL_DIVIDER);
        }
        return false;
    }

    public static boolean requestFocusAndWait(Robot robot, Object requestFocus, Object waitForFocus)
        throws AWTException, InterruptedException, InvocationTargetException {

        requestFocus(robot, requestFocus);
        return waitForFocus(waitForFocus);
    }

    public static boolean requestFocusAndWait(Robot robot, Object requestFocus, Object waitForFocus, 
                                              EventCountAdapter gain, EventCountAdapter lost)
        throws AWTException, InterruptedException, InvocationTargetException {

        int gainT0 = gain.getCount();
        int lostT0 = (null!=lost) ? lost.getCount() : 0;

        requestFocus(robot, requestFocus);
        return waitForFocus(waitForFocus, gainT0, gain, lostT0, lost);
    }

    /**
     * @param keyTypedCounter shall return the number of keys typed (press + release)
     * @return True if typeCount keys within TIME_OUT has been received
     */
    public static int testKeyType(Robot robot, int typeCount, Object obj, EventCountAdapter keyTypedCounter) 
        throws AWTException, InterruptedException, InvocationTargetException {
        Component comp = null;
        com.jogamp.newt.Window win = null;

        if(null == robot) {
            robot = new Robot();
            robot.setAutoWaitForIdle(true);
        }

        centerMouse(robot, obj);

        int c0 = keyTypedCounter.getCount();

        for(int i=0; i<typeCount; i++) {
            robot.keyPress(java.awt.event.KeyEvent.VK_A);
            robot.delay(ROBOT_DELAY);
            robot.keyRelease(java.awt.event.KeyEvent.VK_A);
            robot.delay(ROBOT_DELAY);
        }

        // Wait for the key events to be processed.
        int wait;
        for (wait=0; wait<POLL_DIVIDER && (keyTypedCounter.getCount()-c0)<typeCount; wait++) {
            Thread.sleep(TIME_OUT/POLL_DIVIDER);
        }
        return keyTypedCounter.getCount()-c0;
    }

    /**
     * @param mouseButton ie InputEvent.BUTTON1_MASK
     * @param clickCount ie 1, or 2
     * @return True if the desired clickCount within TIME_OUT has been received
     */
    public static int testMouseClick(Robot robot, int mouseButton, int clickCount,
                                     Object obj, EventCountAdapter mouseClickCounter) 
        throws AWTException, InterruptedException, InvocationTargetException {

        if(null == robot) {
            robot = new Robot();
            robot.setAutoWaitForIdle(true);
        }

        final int clickTO = com.jogamp.newt.event.MouseEvent.getClickTimeout();

        centerMouse(robot, obj);

        robot.delay(2*clickTO);

        int c0 = mouseClickCounter.getCount();

        for(int i=0; i<clickCount; i++) {
            robot.mousePress(mouseButton);
            robot.delay(clickTO/4);
            robot.mouseRelease(mouseButton);
            robot.delay(clickTO/4);
        }

        // Wait for the key events to be processed.
        int wait;
        for (wait=0; wait<POLL_DIVIDER && (mouseClickCounter.getCount()-c0)<clickCount; wait++) {
            Thread.sleep(TIME_OUT/POLL_DIVIDER);
        }
        return mouseClickCounter.getCount()-c0;
    }

    /**
     *
     * @return True if the EventCountAdapter became the desired value within TIME_OUT
     */
    public static boolean waitForCount(int desired, EventCountAdapter eca) throws InterruptedException {
        for (int wait=0; wait<POLL_DIVIDER; wait++) {
            if( eca.getCount() == desired ) {
                return true;
            }
            Thread.sleep(TIME_OUT/POLL_DIVIDER);
        }
        return false;
    }

    /**
     *
     * @return True if the Component becomes <code>visible</code> within TIME_OUT
     */
    public static boolean waitForVisible(Object obj, boolean visible) throws InterruptedException {
        int wait;
        if(obj instanceof Component) {
            Component comp = (Component) obj;
            for (wait=0; wait<POLL_DIVIDER && visible != comp.isVisible(); wait++) {
                Thread.sleep(TIME_OUT/POLL_DIVIDER);
            }
        } else if(obj instanceof com.jogamp.newt.Window) {
            com.jogamp.newt.Window win = (com.jogamp.newt.Window) obj;
            for (wait=0; wait<POLL_DIVIDER && visible != win.isVisible(); wait++) {
                Thread.sleep(TIME_OUT/POLL_DIVIDER);
            }
        } else {
            throw new RuntimeException("Neither AWT nor NEWT: "+obj);
        }
        return wait<POLL_DIVIDER;
    }

    /**
     *
     * @return True if the Component becomes realized (not displayable, native invalid) within TIME_OUT
     */
    public static boolean waitForRealized(Object obj, boolean realized) throws InterruptedException {
        int wait;
        if (obj instanceof Component) {
            Component comp = (Component) obj;
            for (wait=0; wait<POLL_DIVIDER && realized != comp.isDisplayable(); wait++) {
                Thread.sleep(TIME_OUT/POLL_DIVIDER);
            }
            // if GLCanvas, ensure it got also painted -> drawable.setRealized(true);
            if(wait<POLL_DIVIDER && comp instanceof GLCanvas) {
                GLCanvas glcanvas = (GLCanvas) comp;
                for (wait=0; wait<POLL_DIVIDER && realized != glcanvas.isRealized(); wait++) {
                    Thread.sleep(TIME_OUT/POLL_DIVIDER);
                }
                if(wait>=POLL_DIVIDER) {
                    // for some reason GLCanvas hasn't been painted yet, force it!
                    System.err.println("XXX: FORCE REPAINT PRE - canvas: "+glcanvas);
                    glcanvas.repaint();
                    for (wait=0; wait<POLL_DIVIDER && realized != glcanvas.isRealized(); wait++) {
                        Thread.sleep(TIME_OUT/POLL_DIVIDER);
                    }
                    System.err.println("XXX: FORCE REPAINT POST - canvas: "+glcanvas);
                }
                for (wait=0; wait<POLL_DIVIDER && realized != glcanvas.isRealized(); wait++) {
                    Thread.sleep(TIME_OUT/POLL_DIVIDER);
                }
            }            
        } else if(obj instanceof com.jogamp.newt.Window) {
            com.jogamp.newt.Window win = (com.jogamp.newt.Window) obj;
            for (wait=0; wait<POLL_DIVIDER && realized != win.isNativeValid(); wait++) {
                Thread.sleep(TIME_OUT/POLL_DIVIDER);
            }
        } else {
            throw new RuntimeException("Neither AWT nor NEWT: "+obj);
        }
        return wait<POLL_DIVIDER;
    }

    /**
     * Programmatically issue windowClosing on AWT or NEWT.
     * Wait until the window is closing within TIME_OUT.
     *
     * @param obj either an AWT Window (Frame, JFrame) or NEWT Window
     * @param willClose indicating that the window will close, hence this method waits for the window to be closed
     * @return True if the Window is closing and closed (if willClose is true), each within TIME_OUT
     * @throws InterruptedException
     */
    public static boolean closeWindow(Object obj, boolean willClose) throws InterruptedException, InvocationTargetException {
        WindowClosingListener closingListener = addClosingListener(obj);
        if(obj instanceof java.awt.Window) {
            final java.awt.Window win = (java.awt.Window) obj;
            Toolkit tk = Toolkit.getDefaultToolkit();
            final EventQueue evtQ = tk.getSystemEventQueue();
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    evtQ.postEvent(new java.awt.event.WindowEvent(win, java.awt.event.WindowEvent.WINDOW_CLOSING));
                } });
        } else if(obj instanceof com.jogamp.newt.Window) {
            com.jogamp.newt.Window win = (com.jogamp.newt.Window) obj;
            WindowImplAccess.windowDestroyNotify(win);
        }
        int wait;
        for (wait=0; wait<POLL_DIVIDER && !closingListener.isWindowClosing(); wait++) {
            Thread.sleep(TIME_OUT/POLL_DIVIDER);
        }
        if(wait<POLL_DIVIDER && willClose) {
            for (wait=0; wait<POLL_DIVIDER && !closingListener.isWindowClosed(); wait++) {
                Thread.sleep(TIME_OUT/POLL_DIVIDER);
            }
        }
        return wait<POLL_DIVIDER;
    }

    public static WindowClosingListener addClosingListener(Object obj) throws InterruptedException {
        WindowClosingListener cl = null;
        if(obj instanceof java.awt.Window) {
            java.awt.Window win = (java.awt.Window) obj;
            AWTWindowClosingAdapter acl = new AWTWindowClosingAdapter();
            win.addWindowListener(acl);
            cl = acl;
        } else if(obj instanceof com.jogamp.newt.Window) {
            com.jogamp.newt.Window win = (com.jogamp.newt.Window) obj;
            NEWTWindowClosingAdapter ncl = new NEWTWindowClosingAdapter();
            win.addWindowListener(ncl);
            cl = ncl;
        } else {
            throw new RuntimeException("Neither AWT nor NEWT: "+obj);
        }
        return cl;
    }
    public static interface WindowClosingListener {
        void reset();
        public boolean isWindowClosing();
        public boolean isWindowClosed();
    }
    static class AWTWindowClosingAdapter
            extends java.awt.event.WindowAdapter implements WindowClosingListener
    {
        volatile boolean closing = false;
        volatile boolean closed = false;

        public void reset() {
            closing = false;
            closed = false;
        }
        public boolean isWindowClosing() {
            return closing;
        }
        public boolean isWindowClosed() {
            return closed;
        }
        public void windowClosing(java.awt.event.WindowEvent e) {
            closing = true;
        }
        public void windowClosed(java.awt.event.WindowEvent e) {
            closed = true;
        }
    }
    static class NEWTWindowClosingAdapter
            extends com.jogamp.newt.event.WindowAdapter implements WindowClosingListener
    {
        volatile boolean closing = false;
        volatile boolean closed = false;

        public void reset() {
            closing = false;
            closed = false;
        }
        public boolean isWindowClosing() {
            return closing;
        }
        public boolean isWindowClosed() {
            return closed;
        }
        public void windowDestroyNotify(com.jogamp.newt.event.WindowEvent e) {
            closing = true;
        }
        public void windowDestroyed(com.jogamp.newt.event.WindowEvent e) {
            closed = true;
        }
    }

}

