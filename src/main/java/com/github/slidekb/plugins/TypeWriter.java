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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

@AutoService(SlideBarPlugin.class)
public class TypeWriter implements SlideBarPlugin {

    AlphaKeyManager akm;

    Slider slider;

    String previous = "";

    ThisConfig cfg;

    List<String> attachedProcesses = new ArrayList<>();

    public TypeWriter() {
        loadConfiguration();
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public String[] getProcessNames() {
        return attachedProcesses.toArray(new String[attachedProcesses.size()]);
    }

    /**
     * returns the human readable name of this class.
     */
    @Override
    public String getLabelName() {
        return "Type Writer";
    }

    @Override
    public JFrame getProcessWindow() {
        return null;
    }

    @Override
    public void reloadPropFile() {
        try {
            FileInputStream in = new FileInputStream(ClassLoader.getSystemClassLoader().getResource(".").getPath() + "\\configs\\TypeWriter.properties");
            cfg.load(in);
            in.close();

            attachedProcesses = new ArrayList<>(Arrays.asList(cfg.processList()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean loadConfiguration() {
        cfg = ConfigFactory.create(ThisConfig.class);
        attachedProcesses = new ArrayList<>(Arrays.asList(cfg.processList()));

        return true;
    }

    @Config.Sources({ "classpath:configs/TypeWriter.properties" })
    private interface ThisConfig extends Accessible, Mutable {
        String[] processList();
    }

    /**
     * calls writeValues();
     */
    @Override
    public void run(String process) {
        try {
            writeValues();
        } catch (Exception e) {
            System.out.println("Type Writer Process threw an exception");
        }

    }

    /**
     * creates part detector for the run() method. 26 parts for 26 letters in
     * the alphabet.
     */
    @Override
    public void runFirst(String process) {
        slider.write(1010);
    }

    /**
     * Listens to AKM and runs when there is an alpha key pressed. moves arduino
     * to position.
     */
    public void writeValues() {
        String[] keys = akm.getAlphaKeys();
        if (keys.length > 0) {
            String key = keys[keys.length - 1];
            if (!key.equals(previous)) {
                if (!key.equals("Enter")) {
                    slider.bumpLeft(10);
                    previous = key;
                } else {
                    slider.writeUntilComplete(1010);
                    previous = key;
                }

            }
        } else {
            previous = "";
        }
    }

    /**
     * not used for the type-writer.
     */
    public void readValues() {
    }

    @Override
    public JFrame getConfigWindow() {
        return null;
    }

    @Override
    public void setAlphaKeyManager(AlphaKeyManager alphaKeyManager) {
        this.akm = alphaKeyManager;

        akm.addKey("Enter");
        akm.addKey("Space");
    }

    @Override
    public void setHotKeyManager(HotKeyManager hotKeyManager) {
        // NOP
    }

    @Override
    public void setSlider(Slider slider) {
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
