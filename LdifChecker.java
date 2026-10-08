import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.ArrayList;

public class LdifChecker{
	private HashMap checkedLdifItems=new HashMap<String,JsonObject>();
	private HashMap etalonLdifItems=new HashMap<String,JsonObject>();

	private JsonArray getLdif(String filename) throws Exception{
		File initialFile = new File(filename);
		InputStream targetStream = new FileInputStream(initialFile);
		return new JsonParser().parse(new JsonReader(new InputStreamReader(targetStream, "UTF-8"))).getAsJsonArray();
	}

	private void check(JsonArray ldifToCheck, HashMap ldifItems){
		System.out.println("-------- Check ldif ---------");
		HashMap firstLevelKeys=new HashMap<String,Integer>();
		for(JsonElement ldifElem:ldifToCheck){
			JsonObject obj=ldifElem.getAsJsonObject();
			for (String key:obj.keySet()){
				//System.out.println(key);
				if (firstLevelKeys.containsKey(key)){
					int curVal=(Integer)firstLevelKeys.get(key);
					firstLevelKeys.put(key,curVal+1);
				} else {
					firstLevelKeys.put(key,1);
				}
				ldifItems.put(key,obj.getAsJsonArray(key));
			}
			//System.out.println(ldifElem);
		}

		for (Object keys:firstLevelKeys.keySet()){
			if ((Integer)firstLevelKeys.get(keys)>1){
				System.out.println(keys+" meet "+firstLevelKeys.get(keys)+" times");
			}
		}
	}

	private ArrayList<String> jsonArrayToList(JsonArray jsonArray){
		ArrayList<String> retArray = new ArrayList<String>();
		for(JsonElement item:jsonArray){
			retArray.add(item.toString());
		}
		return retArray;
	}

	private void compareLists(String key,ArrayList<String> ldifList,ArrayList<String> etalonList){

		boolean keyChanged=false;
		
		for(int i=0;i<ldifList.size();i++){
			String currentElement=ldifList.get(i);
			boolean isNew=true;

			for(int j=0;j<etalonList.size();j++){
				String elementToCompare=etalonList.get(j);
				if (currentElement.equals(elementToCompare)){
					isNew=false;
					break;
				}
			}
			if (isNew){
				if (!keyChanged) {
					keyChanged=true;
					System.out.println("\n"+key);
					
				}
				System.out.println("+ "+currentElement);
			}
		}

		for(int i=0;i<etalonList.size();i++){
			String currentElement=etalonList.get(i);
			boolean isNew=true;

			for(int j=0;j<ldifList.size();j++){
				String elementToCompare=ldifList.get(j);
				if (currentElement.equals(elementToCompare)){
					isNew=false;
					break;
				}
			}
			if (isNew){
				if (!keyChanged) {
					keyChanged=true;
					System.out.println("\n"+key);
					
				}
				System.out.println("- "+currentElement);
			}
		}
		if (keyChanged) System.out.println("\n");
	}

	private void compare(){
		System.out.println(checkedLdifItems.size());
		System.out.println(etalonLdifItems.size());

		for(Object key:checkedLdifItems.keySet()){
			if (!etalonLdifItems.containsKey(key)){
				System.out.println("New key: "+key);
			} else {
				ArrayList<String> ldifJsonElementList=jsonArrayToList((JsonArray)checkedLdifItems.get(key));
				ArrayList<String> etalonJsonElementList=jsonArrayToList((JsonArray)etalonLdifItems.get(key));
				compareLists(key.toString(),ldifJsonElementList,etalonJsonElementList);
			}
		}

		for(Object key:etalonLdifItems.keySet()){
			if (!checkedLdifItems.containsKey(key)){
				System.out.println("Removed key: "+key);
			}
		}
	}

	private void startLdifCheck(String ldifFile) throws Exception{
		JsonArray ldifToCheck=getLdif(ldifFile);		
		check(ldifToCheck,checkedLdifItems);
	}

	private void startEtalonLdifCheck(String ldifFile) throws Exception{
		JsonArray ldifToCheck=getLdif(ldifFile);		
		check(ldifToCheck,etalonLdifItems);
		compare();
	}

	static public void main(String args[])  throws Exception{
		LdifChecker ldifChecker=new LdifChecker();
		ldifChecker.startLdifCheck(args[0]);
		if (args.length>1) ldifChecker.startEtalonLdifCheck(args[1]);

	}
}
