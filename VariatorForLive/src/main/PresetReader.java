package main;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PresetReader {

	private static String PRESET_FILE = "/VariatorPresets.xml";
	
	// No Argument for Bar Number returns the preset for bar 0
	public static LinkedHashMap<String, double[]> getData(String mapType, String targetName) {
		return getData(mapType, targetName, 0);
	}
	
	public static LinkedHashMap<String, double[]> getData(String mapType, String targetName, int targetBar) {
		
		LinkedHashMap<String, double[]> data = new LinkedHashMap<String, double[]>();
		
		try {
			
			// 
			InputStream inputStream = PresetReader.class.getResourceAsStream(PRESET_FILE);
			//File fXmlFile = new File("/Users/drewdyer-symonds/git/Variator/VariatorForLive/src/PresetsTest.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputStream);
			
			doc.getDocumentElement().normalize();
			
			// Gets the root node for the preset list (i.e. "presets")
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
					
			// Node List of all elements that match the MAP TYPE 
			// (i.e basis, home, or velocity)
			NodeList nList = doc.getElementsByTagName(mapType);
			
			System.out.println("---------------------------");
			
			// Iterate through the node list (There might 
			// only be only one element)
			for (int i = 0; i < nList.getLength(); i++) {
				
				Node nNode = nList.item(i);
				
				System.out.println("\nDataMap Type: " + nNode.getNodeName());
				
				// Node List of PRESETS (i.e. rock, funk, etc)
				NodeList nPresets = nNode.getChildNodes();
				
				// default condition for preset existstence, will throw exeption if not changed
				boolean presetExists = false;
				
				// Iterate through PRESETS
				for (int preset = 0; preset < nPresets.getLength(); preset++) {
					
					Node presetNode = nPresets.item(preset);
					
					// conditional eleminates white space
					if (presetNode.getNodeType() == Node.ELEMENT_NODE) {
						
						// represents a PRESET node of element type
						// (i.e. rock, funk, etc)
						Element ePresetElement = (Element) presetNode;
					
						// Get the name of the current PRESET in the iteration (i.e. rock)
						System.out.println("\n\tPreset: " + ePresetElement.getNodeName() + "\n");
						String currentName = ePresetElement.getNodeName();
						
						// match to desired PRESET
						if (currentName.equals(targetName)) {
							
							presetExists = true;
							
							// Represents list of bars withing a given target
							NodeList nBarList = presetNode.getChildNodes();
								
							try {
								boolean targetBarExists = false;
								
								// Iterate through Bars of the target
								// We have to use this loop to check attributes against desired ones (i.e. num="1")
								for (int barNum = 0; barNum < nBarList.getLength(); barNum++) {
								
									Node barNode = nBarList.item(barNum);
									
									// conditional eliminates white space
									if (barNode.getNodeType() == Node.ELEMENT_NODE) {
										
										Element eBar = (Element) barNode;
										
										// Checks num attribute of the barNode against target bar
										if (eBar.hasAttribute("num") && eBar.getAttribute("num").equals(String.valueOf(targetBar))) {
											
											System.out.println("\t\tBar " + targetBar);
											targetBarExists = true;
								
											// Node List of DRUMS within that preset
											NodeList nDrumList = barNode.getChildNodes();
											
											// Iterate through the DRUMS of each preset (i.e. kick, snare, etc)
											for (int drum = 0; drum < nDrumList.getLength(); drum++) {

												// Represents single DRUM with drum node list
												Node nDrumNode = nDrumList.item(drum);

												if (nDrumNode.getNodeType() == Node.ELEMENT_NODE) {
													
													Element eElement = (Element) nDrumNode;
													
													String drumName = eElement.getNodeName();
													String sDrumData = eElement.getTextContent();
													System.out.println(
															"\t\t\t" + drumName + ": " + sDrumData);
													
													String[] stringArrDrumData = sDrumData.split(",");
													int length = stringArrDrumData.length;
													double[] drumData = new double[length];
													for (int num = 0; num < length; num++) {
														drumData[num] = Double.valueOf(stringArrDrumData[num]);
													}
													data.put(drumName, drumData);
												}
											}
										}
									}
								}
								if (!targetBarExists) {
									throw new IndexOutOfBoundsException("Target bar does not exist in " + mapType + 
											" preset: " + targetName);
								}
							} catch (IndexOutOfBoundsException e) {
								System.out.println("*** Target bar " + targetBar + " does not exist. \n"
										+ "*** Returning default bar for " + targetName + " " + mapType + " preset\n");
								data = getData(mapType, targetName);
								//e.printStackTrace();
							}
						}
					}			
				}
				if (!presetExists) {
					throw new IllegalArgumentException("Target Preset does not exists in " + mapType);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return data;
		
	}
	

	public static void main(String[] args) {
		//getData("basis", "rock", 3);
		System.out.println(Tools.printArray(PresetReader.getData("basis", "rock", 2).get("hhClosed")));
	}
	
}
