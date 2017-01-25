import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class storagePractice {

	//this is going to be FIFO
	List<String> keyCache = new ArrayList<String>();
	static int limit = 10;
	
	public static void main(String[] args) {
		storagePractice test = new storagePractice();
		System.out.println(test.get("lol you suck"));
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
						String[] kv = line.split(",");
						if(kv[0].equals(key)){
							//replace this line
							replaced = true;
							printTemp.println(key+","+value);
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
					
					printWrite.println(key+","+value);
					printWrite.close();
					
					//add key to cache if not already there
					if(!keyCache.contains(key)){
						//not there, add to list
						//check if array is full
						if(keyCache.size() == limit){
							//remove the first element by virtue of FIFO
							
						}
					}
					
					status = "PUT SUCCESSFUL";
				} else {
					//delete original storage.txt and rename the temp file
					System.out.println("Original Deletion: " + inputFile.delete());
					System.out.println("Renaming of Temp: " + temp.renameTo(inputFile));
					status = "PUT UPDATE SUCCESSFUL";
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
						String[] kv = line.split(",");
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
			
			while((line = br.readLine()) != null){
				if(line.length() != 0){
					String[] kv = line.split(",");
					if(kv[0].equals(key)){
						return kv[1];
					}
				}
			}
			
			return "GET ERROR: KEY NOT FOUND";
		} catch (IOException e) {
			e.printStackTrace();
			return "GET ERROR: FILE NOT FOUND OR COULD NOT BE CREATED";
		}
		
		
	}

}
