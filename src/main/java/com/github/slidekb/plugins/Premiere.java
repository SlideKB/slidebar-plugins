/**
 Copyright 2017 John Kester (Jack Kester)

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.github.slidekb.plugins;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;

import com.github.slidekb.api.AlphaKeyManager;
import com.github.slidekb.api.HotKeyManager;
import com.github.slidekb.api.SlideBarPlugin;
import com.github.slidekb.api.Slider;
import com.google.auto.service.AutoService;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

/**
 * Created by JackSec on 4/21/2017.
 */
@AutoService(SlideBarPlugin.class)
public class Premiere implements SlideBarPlugin {

    Slider slider;

    ThisConfig cfg;

    boolean playing = false;

    HotKeyManager hkm;

    Robot robot = null;

    int virtualIndex = 15;

    int virtualparts = 30;

    boolean toggle = false;

    List<String> attachedProcesses = new ArrayList<>();

    AutoHotKeyDll lib;

    public Premiere() {
        loadConfiguration();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String[] getProcessNames() {
        return attachedProcesses.toArray(new String[attachedProcesses.size()]);
    }

    @Override
    public void reloadPropFile() {
        try {
            FileInputStream in = new FileInputStream(ClassLoader.getSystemClassLoader().getResource(".").getPath() + "\\configs\\Premiere.properties");
            cfg.load(in);
            in.close();

            attachedProcesses = new ArrayList<>(Arrays.asList(cfg.processList()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Sleeper(int delay) {

        Instant start = Instant.now();
        do {
        } while (Duration.between(start, Instant.now()).toMillis() < delay);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private boolean loadConfiguration() {
        cfg = ConfigFactory.create(ThisConfig.class);
        attachedProcesses = new ArrayList<>(Arrays.asList(cfg.processList()));

        System.out.println("running in " + System.getProperty("sun.arch.data.model"));
        lib = (AutoHotKeyDll) Native.loadLibrary("AutoHotkey.dll", AutoHotKeyDll.class);
        lib.ahktextdll(new WString(""), new WString(""), new WString(""));
        return true;
    }

    public interface AutoHotKeyDll extends Library {
        public void ahkExec(WString s);

        public void ahkdll(WString s, WString o, WString p);

        public void addFile(WString s, int a);

        public void ahktextdll(WString s, WString o, WString p);

        public Pointer ahkFunction(WString f, WString p1, WString p2, WString p3, WString p4, WString p5, WString p6, WString p7, WString p8, WString p9, WString p10);
    }

    @Config.Sources({ "classpath:configs/Premiere.properties" })
    private interface ThisConfig extends Accessible, Mutable {
        @DefaultValue(", Adobe Premiere Pro.exe")
        String[] processList();
    }

    @Override
    public void run(String process) {

        String[] keys = hkm.getHotkeys();
        String key = "";
        if (keys.length > 0) {
            key = keys[keys.length - 1];
            for (String k : keys) {
                if (k.equals("Shift")) {
                    toggle = true;
                }
            }

        } else {
            toggle = false;
        }
        int slideIndex = slider.getVirtualPartIndex(virtualparts);
        if (virtualIndex < slideIndex) {
            playing = false;
            System.out.println(Arrays.toString(keys));
            if (toggle) {

                lib.ahkExec(new WString("SendEvent {g}"));

            } else {
                lib.ahkExec(new WString("SendEvent {Left}"));
            }
            virtualIndex++;
            System.out.println(slideIndex);
        }
        if (virtualIndex > slideIndex) {
            playing = false;
            System.out.println(Arrays.toString(keys));
            if (toggle) {

                lib.ahkExec(new WString("SendEvent {;}"));

            } else {
                lib.ahkExec(new WString("SendEvent {Right}"));
            }
            virtualIndex--;
            System.out.println(slideIndex);
        }
        // if (slideIndex == virtualparts-1){
        // playing = false;
        // if (toggle){
        // lib.ahkExec(new WString("Send {Shift}{Left}"));
        // } else {
        // lib.ahkExec(new WString("Send {Left}"));
        // }
        // Sleeper(5);
        // }
        // if (slideIndex == 0){
        // if (toggle){
        // lib.ahkExec(new WString("Send {Shift}{Right}"));
        // } else {
        // lib.ahkExec(new WString("Send {Right}"));
        // }
        // Sleeper(5);
        // }
        if (slideIndex == virtualparts - 1 && !playing) {
            playing = true;
            lib.ahkExec(new WString("Send {j}"));
        }
        if (slideIndex == 0 && !playing) {
            playing = true;
            lib.ahkExec(new WString("Send {l}"));
        }
        if (key.equals("Space") || key.equals("Ctrl")) {
            playing = false;
            if (slideIndex != (virtualparts / 2) && slideIndex != (virtualparts / 2) + 1 && slideIndex != (virtualparts / 2) - 1) {
                slider.writeUntilComplete(500);
                virtualIndex = slider.getVirtualPartIndex(virtualparts);
            }
        }
    }

    @Override
    public JFrame getConfigWindow() {
        return null;
    }

    @Override
    public void runFirst(String process) {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.out.println("Robot could not be created...");
        }
        slider.writeUntilComplete(512);
        virtualIndex = slider.getVirtualPartIndex(virtualparts);
    }

    @Override
    public String getLabelName() {
        return "Adobe Premiere Scrubber";
    }

    @Override
    public JFrame getProcessWindow() {
        return null;
    }

    @Override
    public void setAlphaKeyManager(AlphaKeyManager alphaKeyManager) {
        // NOP
    }

    @Override
    public void setHotKeyManager(com.github.slidekb.api.HotKeyManager hotKeyManager) {
        this.hkm = hotKeyManager;

        hkm.addKey("Space");
    }

    @Override
    public void setSlider(com.github.slidekb.api.Slider slider) {
        this.slider = slider;
    }

    @Override
    public void attachToProcess(String processName) {
        attachedProcesses.add(processName);
    }

    @Override
    public void detachFromProcess(String processName) {
        attachedProcesses.remove(processName);
    }
}
