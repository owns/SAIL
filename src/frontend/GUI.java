/*******************************************************************************
 * Copyright (c) 2015 University of Illinois Board of Trustees, All rights reserved.
 * Developed at GSLIS/ the iSchool, by Dr. Jana Diesner, Shubhanshu Mishra, Liang Tao, and Chieh-Li Chin.    
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses>.
 *******************************************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frontend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class for launching the tool.
 * @author ltao3
 */
public class GUI extends Application {
	
	public static final String configFile = "./data/config.properties";
	public static final String configDefaultsFile = "./data/config.defaults.properties";
	public static final String licenseFile = "./data/license.txt";
	public GUIController controller;
	private int exitCode = 1;
	private String curModel = null;
	@Override
    public void start(Stage stage) throws Exception {
    	stage.setTitle("SAIL (Sentiment Analysis and Incremental Learning)");
    	final FXMLLoader loader = new FXMLLoader(
    		      getClass().getResource(
    		        "GUI.fxml"
    		      )
    		    );
    	
        Parent root = (Parent) loader.load();
        controller = loader.getController();
        controller.setStage(stage);
        Scene scene = new Scene(root);
        //
        /*scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                System.out.println("Width: " + newSceneWidth);
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                System.out.println("Height: " + newSceneHeight);
            }
        });*/
        //
        //Scene scene = new Scene(root, 790, 600);
        stage.setResizable(true);
        stage.setScene(scene);
        
        //======================================================================
        // Handle arguments
        //======================================================================
        // print args to console
        List<String> args = getParameters().getRaw();
        System.out.format("--- %02d args: ---%n",args.size());
        int c = 0;
        for(String a: args){System.out.format("%d. %s%n",++c,a);}
        System.out.println("----------------");
        
        // .show() if no args, else handle!
        String inpath = null;
        String outpath = null;
        String config = null;
        switch(args.size()){
        default: System.err.format("Too many arguments; just using first 3... %s%n",args.toString());
        case 3: config = args.get(2);
        case 2: outpath = args.get(1);
        case 1: inpath = args.get(0);
        	break;
        case 0: exitCode = 0; stage.show(); return;
        }
        
 		// 01 set input folder
 		System.out.format("Input  Folder: \"%s\"%n",inpath);
 		// is dir and exists?
 		File a = new File(inpath);
 		if(!a.isDirectory()){ 
 			System.err.println("INPUT FOLDER DNE OR IS NOT A DIRECTORY!!!");
 			exitCode = 3;
 			Platform.exit(); return;
 		}
 		// are there files for us to process?
 		if(a.listFiles().length==0){
 			System.err.println("INPUT FOLDER IS EMPTY... SUCCESS!");
 			exitCode = 0; Platform.exit(); return; // stop
 		}
 		controller.setInputFolder(inpath);
 		
 			
 		// 02 set output folder
 		if(outpath != null){
 			System.out.format("Output Folder: \"%s\"%n",outpath);
	 		// does it exist and is it a directory? 
	 		if(!(new File(outpath).isDirectory())){
	 			System.err.println("OUTPUT FOLDER DNE OR IS NOT A DIRECTORY!!!");
	 			exitCode = 3; Platform.exit(); return; // stop
	 		}
 		} else {
 			System.out.format("Output Folder: \"%s\"%n",outpath);
 			outpath = inpath;
 		}
 		controller.setOutputFolder(outpath);

 		// set config - assume model for now
 		// TODO determine what kind of param / add way to set more config...
 		if(config == null){controller.restoreDefaultProperties();}
 		else{
 			if(!(new File(config)).isFile()){
 				System.out.println("config not a file! "+config);
 				exitCode = 3; Platform.exit(); return; // stop
 			} else {
 				// check file is readable
 				java.io.InputStream b;
 				try {b = new FileInputStream(config);b.close();}
 				catch (IOException e) {
 					//e.printStackTrace();
 					System.out.format("config file cannot be read - %s. %s%n",e.getMessage(),config);
 					exitCode = 3; Platform.exit(); return;
 					}
 				finally {b = null;}
 				
 				curModel = controller.getProperty("model");
 				controller.setProperty("model",config);
 			}
 		}
 		
 		// click the next button!
 		Thread mythread = controller.processInput(new Runnable() {
            @Override public void run() {
            	System.err.println("Success!");
            	exitCode = 0;
            	Platform.exit(); return;
            }
        },new Runnable() {
            @Override public void run() {
        		System.err.println("Task Failed");
        		exitCode = 2;
        		Platform.exit(); return;
        	}
        });
 		
 		//start!
 		mythread.join();
 		exitCode = 0; Platform.exit(); return; // in case somehow a Runnable isn't called
    }
    
    @Override
    public void stop() throws Exception {
    	// roll back config if changed
    	if(curModel != null){
    		System.out.format("rolling back change to model in config file...");
    		controller.setProperty("model",curModel);
		}
    	controller.exit();
    	super.stop();
    	System.out.format("exit code: %d %n",exitCode);
    	System.exit(exitCode);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	/*try {
			System.setOut(new PrintStream("log.stdout.txt"));
			System.setErr(new PrintStream("log.stderr.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	System.out.println("This is the first output to the log.stdout.txt file");
    	System.err.println("This is the first output to the log.stderr.txt file");
    	//*/
        launch(args);
    }
    
}
