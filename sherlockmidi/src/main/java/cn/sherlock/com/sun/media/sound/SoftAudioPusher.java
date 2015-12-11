/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package cn.sherlock.com.sun.media.sound;

import java.io.IOException;

import cn.sherlock.javax.sound.sampled.AudioInputStream;
import cn.sherlock.javax.sound.sampled.SourceDataLine;

/**
 * This is a processor object that writes into SourceDataLine
 *
 * @author Karl Helgason
 */
public class SoftAudioPusher implements Runnable {

    private volatile boolean active = false;
    private SourceDataLine sourceDataLine = null;
    private Thread audiothread;
    private AudioInputStream ais;
    private byte[] buffer;

    public SoftAudioPusher(SourceDataLine sourceDataLine, AudioInputStream ais,
            int workbuffersizer) {
        this.ais = ais;
        this.buffer = new byte[workbuffersizer];
        this.sourceDataLine = sourceDataLine;
    }

    public synchronized void start() {
        if (active)
            return;
        active = true;
        audiothread = new Thread(this);
        audiothread.setDaemon(true);
        audiothread.setPriority(Thread.MAX_PRIORITY);
        audiothread.start();
    }

    public synchronized void stop() {
        if (!active)
            return;
        active = false;
        try {
            audiothread.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    public void run() {
        byte[] buffer = SoftAudioPusher.this.buffer;
        AudioInputStream ais = SoftAudioPusher.this.ais;
        SourceDataLine sourceDataLine = SoftAudioPusher.this.sourceDataLine;

        try {
            while (active) {
                // Read from audio source
                int count = ais.read(buffer);
                if(count < 0) break;
                // Write byte buffer to source output
                sourceDataLine.write(buffer, 0, count);
            }
        } catch (IOException e) {
            active = false;
            //e.printStackTrace();
        }

    }
}