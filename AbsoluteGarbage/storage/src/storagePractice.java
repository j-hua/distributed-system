import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class storagePractice {

	//this is going to be used for all replacement policies
	List<String> keyCache = new ArrayList<String>();
	List<String> valueCache = new ArrayList<String>();
	
	static int limit = 10;
	static String replacementPolicy = "LRU";
	
	public static void main(String[] args) {
		storagePractice test = new storagePractice();
		System.out.println(test.put("one","52"));
		System.out.println(test.put("two","52"));
		System.out.println(test.put("three","52"));
		System.out.println(test.put("four","52"));
		System.out.println(test.put("five","52"));
		System.out.println(test.put("six","52"));
		System.out.println(test.put("seven","52"));
		System.out.println(test.put("eight","52"));
		System.out.println(test.put("nine","52"));
		System.out.println(test.put("ten","52"));
		System.out.println(test.get("one"));
	}

	public String put(String key, String value){
		try {
			//writer for the original file
			FileWriter write = new FileWriter("./storage.txt", true);
			PrintWriter printWrite = new PrintWriter(write);
			//writer for a temp file that could replace the original file
			FileWriter writeTemp = new FileWriter("./temp.txt", true);
			PrintWriter printTemp = new PrintWriter(writeTemp);
			
			String status = null;
			
			if(value != null){
				//read file for this key, maybe its an update
				File inputFile = new File("./storage.txt");
				BufferedReader br = new BufferedReader(new FileReader(inputFile));
				
				String line;
				boolean replaced = false;
				
				while((line = br.readLine()) != null){
					if(line.length() != 0){
						String[] kv = line.split(" ");
						if(kv[0].equals(key)){
							//replace this line
							replaced = true;
							printTemp.println(key+" "+value);
						}else{
							printTemp.println(line);
						}
					}
				}
				
				printTemp.close();
				File temp = new File("./temp.txt");
				
				if(!replaced){
					//delete temp file
					System.out.println("Temp Deletion: " + temp.delete());
					
					printWrite.println(key+" "+value);
					printWrite.close();
					
					status = "PUT SUCCESSFUL";
				} else {
					//delete original storage.txt and rename the temp file
					System.out.println("Original Deletion: " + inputFile.delete());
					System.out.println("Renaming of Temp: " + temp.renameTo(inputFile));
					status = "PUT UPDATE SUCCESSFUL";
				}
				
				if(replacementPolicy.equals("FIFO")){
					//FIFO CACHE----------------------------------------------------------------------------
					//add key to cache if not already there
					if(!keyCache.contains(key)){
						//not there, add to list
						//check if array is full
						if(keyCache.size() == limit){
							//remove the first element by virtue of FIFO
							System.out.println("Key: " + keyCache.get(0) + " Value: " + valueCache.get(0) + " REMOVED FROM CACHE");
							keyCache.remove(0);
							valueCache.remove(0);
						}
						System.out.println("Key: " + key + " Value: " + value + " ADDED TO CACHE");
						keyCache.add(key);
						valueCache.add(value);
					}
					//---------------------------------------------------------------------------------------
				} else if (replacementPolicy.equals("LRU")){
					//LRU CACHE----------------------------------------------------------------------------
					//add key to cache if not already there
					if(!keyCache.contains(key)){
						//not there, add to end of list, indicates most recently used
						//check if array is full
						if(keyCache.size() == limit){
							//remove the first element by virtue of LRU (first is the least recently used)
							System.out.println("Key: " + keyCache.get(0) + " Value: " + valueCache.get(0) + " REMOVED FROM CACHE");
							keyCache.remove(0);
							valueCache.remove(0);
						}
						System.out.println("Key: " + key + " Value: " + value + " ADDED TO CACHE");
						keyCache.add(key);
						valueCache.add(value);
					} else {
						//the key cache contains the key, thus a put update happened, remove key and value from cache
						System.out.println("Removing " + keyCache.get(keyCache.indexOf(key)) + " and " + valueCache.get(keyCache.indexOf(key)));
						valueCache.remove(keyCache.indexOf(key));
						keyCache.remove(keyCache.indexOf(key));
						
						//add to end of list, that means it was the most recently used
						keyCache.add(key);
						valueCache.add(value);
						
						System.out.println(key + " " + value + " added to end of list");
						System.out.println(keyCache.toString());
						System.out.println(valueCache.toString());
					}
					//---------------------------------------------------------------------------------------
				}
				br.close();
				
			} else {
				//delete the corresponding key value pair in the file
				File inputFile = new File("./storage.txt");
				BufferedReader br = new BufferedReader(new FileReader(inputFile));
				
				String line;
				boolean deleted = false;
				
				while((line = br.readLine()) != null){
					if(line.length() != 0){
						String[] kv = line.split(" ");
						if(kv[0].equals(key)){
							//delete this line
							deleted = true;
						}else{
							printTemp.println(line);
						}
					}
				}
				
				printTemp.close();
				File temp = new File("./temp.txt");
				
				if(!deleted){
					//delete temp file
					System.out.println("Temp Deletion: " + temp.delete());
					
					printWrite.close();
					status = "PUT ERROR: DELETE FAILED, NO SUCH KEY";
				} else {
					//delete original storage.txt and rename the temp file
					System.out.println("Original Deletion: " + inputFile.delete());
					System.out.println("Renaming of Temp: " + temp.renameTo(inputFile));
					status = "KEY DELETE SUCCESSFUL";
					
					if(replacementPolicy.equals("FIFO") || replacementPolicy.equals("LRU")){
						//FIFO OR LRU--------------------------------------------------------
						//If in cache, remove it
						if(keyCache.contains(key)){
							System.out.println("Removing " + keyCache.get(keyCache.indexOf(key)) + " and " + valueCache.get(keyCache.indexOf(key)));
							valueCache.remove(keyCache.indexOf(key));
							keyCache.remove(keyCache.indexOf(key));
							
							System.out.println(keyCache.toString());
							System.out.println(valueCache.toString());
						}
						//-------------------------------------------------------------------
					}
				}
				
				br.close();
			}
			
			return status;
		} catch (IOException e) {
			e.printStackTrace();
			return "PUT ERROR: FILE NOT FOUND OR COULD NOT BE CREATED";
		}
		
	}
	
	public String get(String key){
		//get the value for the corresponding key if the key exists in file
		try {
			File inputFile = new File("./storage.txt");
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			
			String line;
			
			if(replacementPolicy.equals("FIFO")){
				//FIFO--------------------------------------------------------------------
				//First check if it is in the cache
				if(keyCache.contains(key)){
					System.out.println("KEY FOUND IN CACHE");
					br.close();
					return valueCache.get(keyCache.indexOf(key));
				}
				//------------------------------------------------------------------------
			} else if (replacementPolicy.equals("LRU")){
				//LRU---------------------------------------------------------------------
				//First check if it is in the cache
				if(keyCache.contains(key)){
					System.out.println("KEY FOUND IN CACHE");
					String value = valueCache.get(keyCache.indexOf(key));
					
					//the key cache contains the key, thus a put update happened, remove key and value from cache
					System.out.println("Removing " + keyCache.get(keyCache.indexOf(key)) + " and " + valueCache.get(keyCache.indexOf(key)));
					valueCache.remove(keyCache.indexOf(key));
					keyCache.remove(keyCache.indexOf(key));
					
					//add to end of list, that means it was the most recently used
					keyCache.add(key);
					valueCache.add(value);
					
					System.out.println(key + " " + value + " added to end of list");
					System.out.println(keyCache.toString());
					System.out.println(valueCache.toString());
					
					br.close();
					return value;
				}
				//------------------------------------------------------------------------
			}
			
			while((line = br.readLine()) != null){
				if(line.length() != 0){
					String[] kv = line.split(" ");
					if(kv[0].equals(key)){
						//skip the key and the first part of the value because its already been stored in value
						boolean skipFirst = false;
						boolean skipSecond = false;
						String value = kv[1];
						
						//concatenate all the other parts of the value
						for (String part : kv){
							if(!skipFirst){
								skipFirst = true;
							} else if(!skipSecond){
								skipSecond = true;
							} else{
								value = value + " " + part;
							}
						}
						br.close();
						
						if(replacementPolicy.equals("FIFO") || replacementPolicy.equals("LRU")){
							//FIFO or LRU--------------------------------------------------
							//Add to cache because it is not there
							//check if array is full
							if(keyCache.size() == limit){
								//remove the first element by virtue of FIFO
								//or remove by virtue of being the least recently used (LRU)
								System.out.println("Key: " + keyCache.get(0) + " Value: " + valueCache.get(0) + " REMOVED FROM CACHE");
								keyCache.remove(0);
								valueCache.remove(0);
							}
							System.out.println("Key: " + key + " Value: " + value + " ADDED TO CACHE");
							keyCache.add(key);
							valueCache.add(value);
							//------------------------------------------------------------
						}
						
						return value;
					}
				}
			}
			br.close();
			return "GET ERROR: KEY NOT FOUND";
		} catch (IOException e) {
			e.printStackTrace();
			return "GET ERROR: FILE NOT FOUND OR COULD NOT BE CREATED";
		}
		
		
	}

}
