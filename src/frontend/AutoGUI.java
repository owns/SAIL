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
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import core.AppProperties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import sentinets.Prediction.MODELTYPE;

/**
 * Main class for launching the tool.
 * @author ltao3
 */
public class AutoGUI extends Application {
	
	public static final String configFile = "./data/config.properties";
	public static final String configDefaultsFile = "./data/config.defaults.properties";
	public static final String licenseFile = "./data/license.txt";
	public GUIController controller;
	
	public int exitCode = 1;
	
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
        //stage.show(); // Elias Wood - no need to show.
        
        
        // get input and output folders
        List<String> args = getParameters().getRaw();
        System.out.format("%d args:%n",args.size());
        int c = 0;
        for(String a: args){
        	System.out.format("%d. %s%n",++c,a);
        }
        
 		if(args.size()<2){
 			System.err.println("NOT AT LEAST 2 ARGUEMNTS GIVEN!!! \"inputFolder\" \"outputFolder\"");
 			exitCode = 3;
 			Platform.exit(); return;
 		}

 		String inpath = args.get(args.size()-2);
 		String outpath = args.get(args.size()-1);
 		
 		// Check input folder
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
 			exitCode = 0;
 			Platform.exit(); return;
 		}
 			
 		// Check output folder
 		System.out.format("Output Folder: \"%s\"%n",outpath);
 		// does it exist and is it a directory? 
 		if(!(new File(outpath).isDirectory())){
 			System.err.println("OUTPUT FOLDER DNE OR IS NOT A DIRECTORY!!!");
 			exitCode = 3;
 			Platform.exit(); return;
 		}
 		
 		// set input/output folders
 		controller.setInputFolder(inpath);
 		controller.setOutputFolder(outpath);
 		
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
    }
    
    @Override
    public void stop() throws Exception {
    	controller.exit();
    	System.err.format("exit code: %d%n",exitCode);
    	super.stop();
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
		}//*/
    	System.out.println("This is the first output to the log.stdout.txt file");
    	System.err.println("This is the first output to the log.stderr.txt file");
        launch(args);
        
    }
    
}
