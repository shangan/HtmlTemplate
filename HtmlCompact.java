/**
 * 
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author shangan
 *
 */
public class HtmlCompact {

	public static String compact(String html){
		if(html == null){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		int len = html.length();
		for(int i = 0; i < len; i++){
			char curChar = html.charAt(i);
			if(curChar == '<'){
				char lastChar = curChar;				
				while(curChar != '>'){
					if(isBlank(curChar) && isBlank(lastChar)){
						
					}else{
						sb.append(curChar);
						lastChar = curChar;
					}
					i++;
					if(i < len){
						curChar = html.charAt(i);
					}else{
						break;
					}
				}
				sb.append(curChar);
			}else if(isBlank(curChar)){
				continue;
			}else{
				sb.append(curChar);
			}
		}
		
		return sb.toString();

	}
	
	public static boolean isBlank(char ch){
		
		if(ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t'){
			return true;
		}
		
		return false;
	}
	
	public static String readFile(String path){
		StringBuilder sb = new StringBuilder();
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis, "utf-8");
			char[] buffer = new char[1000];
			int len = 0;
			while((len = isr.read(buffer, 0, 1000)) != -1){
				sb.append(buffer, 0, len);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sb.toString();
		
	}
	
	public static void main(String[] args){
		
		String html = "   <html   > <font> abc</  html >";
		String fileName = "C:/Users/Administrator/Desktop/email_templete/emailcontent.html";
		System.out.println(HtmlCompact.compact(readFile(fileName)));
	}
}
