
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * 
 * @author shangan	
 * @version 0.1
 * contact me : chenshangan521@163.com
 * 
 */
public class HtmlTemplate {
	private static final Logger logger = Logger.getLogger(HtmlTemplate.class);
	
	private class Block
	{
		public String parent = null;
		public List<Block> children;
		private Map<String, String> _varMap;
		private String _template;
		private String template;
		private String blockName; 	// name in the html page
		private String alias;		// logical name
		public StringBuilder value;
		
		public Block(String parent, String blockName, String alias, String _template)
		{
			this.parent = parent;
			this.blockName = blockName;	
			this.alias = alias;		
			this._template = _template;
			this.template = _template;
			this._varMap = new HashMap<String, String>();
			this.children = new ArrayList<Block>();
			value = new StringBuilder();
			
		}
		
		private void replace(String patternStr, String value)
		{
			if(value != null)
			{
				template = template.replaceAll(patternStr, value);
			}
			else
			{
				logger.warn("null for pattern : " + patternStr);
			}
		}
		
		public void parse(boolean append)
		{	
			
			Set<String> varNameSet = _varMap.keySet();
			for(String varName : varNameSet)
			{
				String patternStr = getVarNamePattern(varName);
				replace(patternStr, _varMap.get(varName));				
			}
			for(Block block : children)
			{
				String patternStr = getBlockPattern(block.blockName);
				replace(patternStr, block.value.toString());
			}
			
			if(append)
			{
				value.append(template);
			}
			else				
			{
				value.delete(0, value.length());
				value.append(template);
			}
			
			reset();
			
		}
		
		private void reset()
		{
			_varMap.clear();
			children.clear();
			template = _template;
		}
		
		public void setVar(String name, String value)
		{
			_varMap.put(name, value);
		}
		
		public void setVar(Map<String, String> varMap)
		{
			_varMap.putAll(varMap);
		}
		
		public void clean()
		{
			_varMap.clear();
			value = value.delete(0, value.length());			
//			for(Block child : children)
//			{
//				child.clean();
//			}
			children.clear();
		}
	}
	
	private Block root;
	private Map<String, Block> _blockMap;
	private static String _template;	// used to store original html string
	private String template;
	private String leftDelimiter = "<!--";
	private String rightDelimiter = "-->";
	
	public HtmlTemplate()
	{
		_blockMap = new HashMap<String, Block>();		
	}
	
	public void loadTemplate(String path) throws IOException
	{
		InputStream in = new FileInputStream(path);			
		InputStreamReader isr = new InputStreamReader(in, Charset.forName("utf-8"));
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = br.readLine()) != null)
		{
			sb.append(line);				
		}		
		this._template = sb.toString();
		this.template = _template;
		root = new Block(null, "root", ".", _template);		
		_blockMap.put(".", root);
	}
	
	public void setLeftDelimiter(String leftDelimiter)
	{
		this.leftDelimiter = leftDelimiter;
	}
	
	public void setRightDelimiter(String rightDelimiter)
	{
		this.rightDelimiter = rightDelimiter;
	}	
	
	private String getVarNamePattern(String varName)
	{		
		return leftDelimiter + "\\s*" + varName + "\\s*" + rightDelimiter;
	}
	
	private String getBlockPattern(String blockName)
	{
		String begin = leftDelimiter + "\\s*begin_block_" + blockName + "\\s*" + rightDelimiter;
		String end = leftDelimiter + "\\s*end_block_" + blockName + "\\s*" + rightDelimiter;
		return begin + "(.*)" + end;
	}
	
	private String getBlockTemplate(String blockName)
	{
		String patternStr = getBlockPattern(blockName);
		Pattern p = Pattern.compile(patternStr);
		Matcher matcher = p.matcher(template);
		if(matcher.find())
		{
			return matcher.group(1);			
		}
		else
		{
			logger.error("can't find pattern for block name : {" + blockName + "}, pattern format : " + patternStr);
			System.exit(-1);
		}
		return null;

	}
	
	/**
	 * 
	 * @param name : variable name in html page
	 * @param value : value for the variable to be replaced
	 */
	public void setVar(String name, String value)
	{
		root.setVar(name, value);
	}
	
	public void setVar(Map<String, String> varMap)
	{
		root.setVar(varMap);
	}
	
	/**
	 * 	@param parent : default ".", means its parent is the root
	 *  @param blockName : variable name in html page
	 * 	@param alias : logic variable name for the programmer to use
	 * 	
	 */
	public void setBlock(String parent, String blockName, String alias)
	{
		if(_blockMap.containsKey(parent))
		{
			Block block = new Block(parent, blockName, alias, getBlockTemplate(blockName));
			
			Block parentBlock = _blockMap.get(parent);
			parentBlock.children.add(block);	
			_blockMap.put(alias, block);
		}
		else
		{
			logger.error("parent doesn't exist, exit now, parent : {" + parent + "}");
			System.exit(-1);
		}
	}
	
	public void setBlockVar(String alias, Map<String, String> varMap)
	{
		_blockMap.get(alias).setVar(varMap);
	}
	
	public void setBlockVar(String blockVarName, String varName, String value)
	{
		_blockMap.get(blockVarName).setVar(varName, value);
	}
	
	public void parseBlock(String alias, boolean append)
	{
		_blockMap.get(alias).parse(append);
	}
	
	public String parseTemplate()
	{
		root.parse(true);
		return root.value.toString();
	}
	
	// unset all data
	public void clean()
	{
		template = _template;
		root.clean();
		Set<String> aliasSet = new HashSet<String>(); 
		aliasSet.addAll(_blockMap.keySet());
		for(String alias : aliasSet)
		{
			if(!alias.equals("."))
			{
				_blockMap.remove(alias);
			}
		}
	}
	
	
	public static void main(String[] args) throws IOException
	{
		HtmlTemplate htmlTemplate = new HtmlTemplate();
		htmlTemplate.loadTemplate("E:/chenshangan/test/yj101.html");
		for(int j = 0; j < 3; j++)
		{				
			Map<String, String> varMap = new HashMap<String, String>();
			varMap.put("IMG", "zt/201112/22/132456338289112000.jpg");
			varMap.put("SUBDOMAIN", "beijing");
			varMap.put("GOODSID", "719238");
			varMap.put("DESCRIPTION", "goods description");
			varMap.put("PRICE", "20.8");
			varMap.put("DISCOUNT", "2.6");
			varMap.put("VALUE", "120");
			htmlTemplate.setVar(varMap);
			htmlTemplate.setBlock(".", "goodslist", "goodslist");
			for(int i = 0; i < 5; i++)
			{
				varMap.put("DESCRIPTION", "goods description" + i);
				htmlTemplate.setBlockVar("goodslist", varMap);
				htmlTemplate.setBlock("goodslist", "comment", "commentlist");
				for(int k = 0; k < 4; k++)
				{
					htmlTemplate.setBlockVar("commentlist", "comment", "comment" + (k * i));
					htmlTemplate.parseBlock("commentlist", true);
				}
				
				htmlTemplate.parseBlock("goodslist", true);				
			}
			
			String content = htmlTemplate.parseTemplate();
			htmlTemplate.clean();
			
			try {
				String filePath = "E:/chenshangan/test";
				String filename = filePath + "/test" + j + ".html";
				File file = new File(filePath);
				if(!file.exists())
				{
					boolean state = file.mkdir();
					if(state)
					{
						System.out.println("create file path succeed");
					}
					else
					{
						System.out.println("create file path failed");
					}
				}
				FileWriter fw = new FileWriter(filename);
				fw.write(content);
				fw.close();
				System.out.println("finish");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	

}
