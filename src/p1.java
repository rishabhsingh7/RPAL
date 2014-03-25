import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/*
 * **************************************************
 * LEXICAL ANALYZER
 * read input file and construct tokens
 * Read file for each token when parser needs one
 * **************************************************
 */

class Lexical {
	private String line = null, type = null;
	private BufferedReader br;
	private String token;
	private ArrayList<String> letters = new ArrayList<String>();	//Collection of allowed letters
	private ArrayList<String> digits = new ArrayList<String>();	//Collection of allowed digits
	private ArrayList<String> operators = new ArrayList<String>();	//Collection of allowed operators
	private ArrayList<String> spaces = new ArrayList<String>();	//Collection of space and horizontal tab
	private ArrayList<String> punctions = new ArrayList<String>();	//Punctuations
	public ArrayList<String> reserved = new ArrayList<String>();	//Reserved tokens of grammar
	
	//Constructor to open file for reading
	
	Lexical(String fileName) {
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//Initialize the allowed LETTERS, DIGITS and OPERATOS
		char ch = 'a';
		for (int i = 0 ; i < 26 ; i++)
			letters.add(""+ch++);
		ch = 'A';
		for (int i = 0 ; i < 26 ; i++)
			letters.add(""+ch++);
		for (int i = 0 ; i < 10 ; i++)
			digits.add(""+i);
		String[] s = new String[] {"+", "-", "*", "<", ">", "&", ".", "@", "/", ":", "=", "~", "|", "$", "!", "#", "%", 
				"^", "_", "[", "]", "{", "}", "\"", "`", "?"};
		operators.addAll(Arrays.asList(s));
		s = new String[] {" ", "\t", "\n"};
		spaces.addAll(Arrays.asList(s));
		s = new String[] {"(", ")", ";", ","};
		punctions.addAll(Arrays.asList(s));
		s = new String[] {"let", "in", "fn", "where", "aug", "or", "not", "gr", "ge", "ls", "le", "eq", "ne", "within", "and", "rec"};
		reserved.addAll(Arrays.asList(s));
	}
	
	//Read a line from FILE
	
	private String readFile() {
		try {
			return br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	//Parse the current line and select a token to return. If EndOfLine reached, make line = null for next line reading.
	
	private String parse() {
		StringBuilder sb = new StringBuilder();	
		boolean cont;
		do {
			cont = false;
			if (line == null)
				return "";
			if (line.equals("")) {
				line = readFile();
				cont = true;
			}
			//IDENTIFIER
			else if (letters.contains(line.charAt(0)+"")) {
				type = "<IDENTIFIER>";	//TYPE can be needed by the parser corresponding to a token
				while (true) {
					sb.append(line.charAt(0)+"");	//Append that character to the current token
					line = line.substring(1);	//Trim the first character from the LINE for next reading
					if (line.length() == 0) {
						line = null;
						break;
					}
					if (letters.contains(line.charAt(0)+""))
						continue;
					else if (digits.contains(line.charAt(0)+""))
						continue;
					else if (line.charAt(0) == '_')
						continue;
					else
						break;
				}
			}
			//INTEGER
			else if (digits.contains(line.charAt(0)+"")) {
				type = "<INTEGER>";
				while (true) {
					sb.append(line.charAt(0)+"");
					line = line.substring(1);
					if (line.length() == 0) {
						line = null;
						break;
					}
					if (digits.contains(line.charAt(0)+""))
						continue;
					else
						break;
				}
			}
			//COMMENT
			else if (line.charAt(0) == '/' && line.charAt(1) == '/') {
				type = "<DELETE>";
				line = readFile();
				if (line == null)
					return "";
				cont = true;
			}
			//OPERATOR
			else if (operators.contains(line.charAt(0)+"")) {
				type = "<OPERATOR>";
				while (true) {
					sb.append(line.charAt(0)+"");
					line = line.substring(1);
					if (line.length() == 0) {
						line = null;
						break;
					}
					if (operators.contains(line.charAt(0)+""))
						continue;
					else
						break;
				}
			}
			//STRING
			else if (line.charAt(0) == '\'') {
				type = "<STRING>";
				while (true) {
					sb.append(line.charAt(0));
					line = line.substring(1);
					if (line.length() == 0) {
						line = null;
						break;
					}
					if (line.charAt(0) == '\\') {	//If escape character, don't consider the next character
						sb.append(line.charAt(0));
						line = line.substring(1);
						sb.append(line.charAt(0));
						line = line.substring(1);
					}
					if (line.charAt(0) == '\'') {
						sb.append(line.charAt(0));
						line = line.substring(1);
						if (line.length() == 0)
							line = null;
						break;
					}
				}
			}
			//SPACES
			else if (spaces.contains(line.charAt(0)+"")) {
				type = "<DELETE>";
				line = line.substring(1);
				if (line.length() == 0)
					line = readFile();
				if (line == null)
					return "";
				cont = true;
			}
			//PUNCTUATIONS
			else if (punctions.contains(line.charAt(0)+"")) {
				type = "<PUNCTIONS>";
				sb.append(line.charAt(0));
				line = line.substring(1);
				if (line.length() == 0)
					line = null;
			}
		} while (cont == true);
		
		return sb.toString();
	}
	
	//Get a new token for parsing. Read a new line if the previous one is finished.
	
	public String getToken() {
		token = "";
		if (line == null) {
			line = readFile();
		}
		if (line != null) {
			token = parse();
			if (token.equals(""))
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		else {	//No more contents left in file to read
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			token = "";
		}
		return token;
	}
	
	//Get type of the last returned token
	
	public String getType() {
		if (token.equals(""))
			type = "<EOF>";
		return type;
	}
}

/*
 * ABSTRACT SYNTAX TREE
 * 
 * Tree is in FIRST CHILD NEXT SIBLING form,
 * child = first child, sibling = next sibling
 */

class Node {
	private String token;
	private Node child;
	private Node sibling;
	public void setToken(String token) {
		this.token = token;
	}
	public void setChild(Node child) {
		this.child = child;
	}
	public void setSibling(Node sibling) {
		this.sibling = sibling;
	}
	public String getToken() {
		return token;
	}
	public Node getChild() {
		return child;
	}
	public Node getSibling() {
		return sibling;
	}
}

/*
 * **************************************************
 * PARSER
 * Request a token from Lexical and parse it accordingly
 * **************************************************
 */

class Parser {
	Lexical l;
	String token;
	Stack<Node> stack = new Stack<Node>();
	
	//Constructor to initialize Lexical with fileName
	
	Parser(String fileName) {
		l = new Lexical(fileName);
	}
	
	//Read and compare the token with the expected value
	
	private void read(String s) {
		if (!s.equals(token)) {	//SYNTAX ERROR !! exit.
			System.err.println("Expected '"+s+"' but found '"+token+"'");
			System.exit(0);
		}
		if ((!l.reserved.contains(token)) && (l.getType().equals("<IDENTIFIER>") || l.getType().equals("<INTEGER>") || l.getType().equals("<STRING>")))
			buildTree(token, 0);	//Build a node in case of identifiers, integers and strings
		token = l.getToken();
	}
	
	//Build Tree by popping the specified number of trees from stack and join with root node specified
	
	private void buildTree(String root, int n) {
		int i = 0;	//To pop the required number of trees
		Node temp = null;
		String s;
		if (n == 0 && l.getType().equals("<IDENTIFIER>") && !l.reserved.contains(root))
			s = "<ID:"+root+">";
		else if (n == 0 && l.getType().equals("<INTEGER>"))
			s = "<INT:"+root+">";
		else if (n == 0 && l.getType().equals("<STRING>") && !l.reserved.contains(root))
			s = "<STR:"+root+">";
		else s = root;
		while (i < n) {
			Node sib = stack.pop();
			sib.setSibling(temp);	//First popped node has sibling set as null, second popped has sibling as 'first popped'
			temp = sib;
			i++;
		}
		Node r = new Node();
		r.setToken(s);
		r.setChild(temp);
		r.setSibling(null);
		stack.push(r);	//Push the newly constructed tree which has root as token='root' from argument
	}
	
	/*
	 * E -> 'let' D 'in' E		=> 'let'
	 *   -> 'fn' Vb+ '.' 'E'	=> 'lambda'
	 *   -> Ew;
	 */
	private void E() {
		if (token.equals("let")) {
			read("let");
			D();
			read("in");
			E();
			buildTree("let", 2);
		}
		else if (token.equals("fn")) {
			read("fn");
			int n = 0;
			do {
				Vb();
				n++;
			} while (l.getType().equals("<IDENTIFIER>") | token.equals("("));
			read(".");
			E();
			buildTree("lambda", n+1);
		}
		else {
			Ew();
		}
	}
	
	/*
	 * Ew -> T 'where' Dr	=> 'where'
	 *    -> T;
	 */
	private void Ew() {
		T();
		if (token.equals("where")) {
			read("where");
			Dr();
			buildTree("where", 2);
		}
	}
	
	/*
	 * T -> Ta ( ',' Ta )+	=> 'tau'
	 *   -> Ta;
	 */
	private void T() {
		Ta();
		int n = 0;
		while (token.equals(",")) {
			read(",");
			Ta();
			n++;
		}
		if (n > 0) {
			buildTree("tau", n+1);
		}
	}
	
	/*
	 * Ta -> Ta 'aug' Tc	=> 'aug'
	 *    -> Tc;
	 */
	private void Ta() {
		Tc();
		while (token.equals("aug")) {
			read("aug");
			Tc();
			buildTree("aug", 2);
		}
	}
	
	/*
	 * Tc -> B '->' Tc '|' Tc	=> '->'
	 *    -> B;
	 */
	private void Tc() {
		B();
		if (token.equals("->")) {
			read("->");
			Tc();
			read("|");
			Tc();
			buildTree("->", 3);
		}
	}
	
	/*
	 * B -> B 'or' Bt	=> 'or'
	 *   -> Bt;
	 */
	private void B() {
		Bt();
		while (token.equals("or")) {
			read("or");
			Bt();
			buildTree("or", 2);
		}
	}
	
	/*
	 * Bt -> Bt '&' Bs	=> '&'
	 *    -> Bs;
	 */
	private void Bt() {
		Bs();
		while (token.equals("&")) {
			read("&");
			Bs();
			buildTree("&", 2);
		}
	}
	
	/*
	 * Bs -> 'not' Bp	=> 'not'
	 *    -> Bp;
	 */
	private void Bs() {
		if (token.equals("not")) {
			read("not");
			Bp();
			buildTree("not", 1);
		}
		else {
			Bp();
		}
	}
	
	/*
	 * Bp -> A ( 'gr' | '>' ) A		=> 'gr'
	 *    -> A ( 'ge' | '>=' ) A	=> 'ge'
	 *    -> A ( 'ls' | '<' ) A		=> 'ls'
	 *    -> A ( 'le' | '<=' ) A	=> 'le'
	 *    -> A 'eq' A				=> 'eq'
	 *    -> A 'ne' A				=> 'ne'
	 *    -> A;
	 */
	private void Bp() {
		A();
		if (token.equals("gr") || token.equals(">")) {
			read(token);
			A();
			buildTree("gr", 2);
		}
		else if (token.equals("ge") || token.equals(">=")) {
			read(token);
			A();
			buildTree("ge", 2);
		}
		else if (token.equals("ls") || token.equals("<")) {
			read(token);
			A();
			buildTree("ls", 2);
		}
		else if (token.equals("le") || token.equals("<=")) {
			read(token);
			A();
			buildTree("le", 2);
		}
		else if (token.equals("eq")) {
			read("eq");
			A();
			buildTree("eq", 2);
		}
		else if (token.equals("ne")) {
			read("ne");
			A();
			buildTree("ne", 2);
		}
	}
	
	/*
	 * A -> A '+' At	=> '+'
	 *   -> A '-' At	=> '-'
	 *   -> '+' At
	 *   -> '-' At		=> 'neg'
	 *   -> At;
	 */
	private void A() {
		if (token.equals("+")) {
			read("+");
			At();
		}
		else if (token.equals("-")) {
			read("-");
			At();
			buildTree("neg", 1);
		}
		else {
			At();
		}
		String temp;
		while (token.equals("+") || token.equals("-")) {
			temp = token;
			read(temp);
			At();
			buildTree(temp, 2);
		}
	}
	
	/*
	 * At -> At '*' Af	=> '*'
	 *    -> At '/' Af	=> '/'
	 *    -> Af;
	 */
	private void At() {
		Af();
		String temp;
		while (token.equals("*") || token.equals("/")) {
			temp = token;
			read(temp);
			Af();
			buildTree(temp, 2);
		}
	}
	
	/*
	 * Af -> Ap '**' Af		=> '**'
	 *    -> Ap;
	 */
	private void Af() {
		Ap();
		if (token.equals("**")) {
			read("**");
			Af();
			buildTree("**", 2);
		}
	}
	
	/*
	 * Ap -> Ap '@' '<identifier>' R	=> '@'
	 *    -> R;
	 */
	private void Ap() {
		R();
		while (token.equals("@")) {
			read("@");
			read(token);
			R();
			buildTree("@", 3);
		}
	}
	
	/*
	 * R -> R Rn	=> 'gamma'
	 *   -> Rn;
	 */
	private void R() {
		Rn();
		while ((!l.reserved.contains(token)) && (l.getType().equals("<IDENTIFIER>") || l.getType().equals("<INTEGER>") || l.getType().equals("<STRING>") || 
				token.equals("true") || token.equals("false") || token.equals("nil") || token.equals("(") || token.equals("dummy"))) {
			Rn();
			buildTree("gamma", 2);
		}
	}
	
	/*
	 * Rn -> '<identifier>'
	 *    -> '<integer>'
	 *    -> '<string>'
	 *    -> 'true'			=> 'true'
	 *    -> 'false'		=> 'false'
	 *    -> 'nil'			=> 'nil'
	 *    -> '(' E ')'
	 *    -> 'dummy'		=> 'dummy'
	 */
	private void Rn() {
		if (l.getType().equals("<IDENTIFIER>") || l.getType().equals("<INTEGER>") || l.getType().equals("<STRING>")) {
			read(token);
		}
		else if (token.equals("true")) {
			read("true");
			buildTree("true", 0);
		}
		else if (token.equals("false")) {
			read("false");
			buildTree("false", 0);
		}
		else if (token.equals("nil")) {
			read("nil");
			buildTree("nil", 0);
		}
		else if (token.equals("(")) {
			read("(");
			E();
			read(")");
		}
		else if (token.equals("dummy")) {
			read("dummy");
			buildTree("dummy", 0);
		}
	}
	
	/*
	 * D -> Da 'within' D	=> 'within'
	 *   -> Da;
	 */
	private void D() {
		Da();
		if (token.equals("within")) {
			read("within");
			D();
			buildTree("within", 2);
		}
	}
	
	/*
	 * Da -> Dr ( 'and' Dr )+	=> 'and'
	 *    -> Dr;
	 */
	private void Da() {
		Dr();
		int n = 0;
		while (token.equals("and")) {
			read("and");
			Dr();
			n++;
		}
		if (n > 0)
			buildTree("and", n+1);
	}
	
	/*
	 * Dr -> 'rec' Db	=> 'rec'
	 *    -> Db;
	 */
	private void Dr() {
		if (token.equals("rec")) {
			read("rec");
			Db();
			buildTree("rec", 1);
		}
		else
			Db();
	}
	
	/*
	 * Db -> Vl '=' E					=> '='
	 *    -> '<identifier>' Vb+ '=' E	=> 'fcn_form'
	 *    -> '(' D ')';
	 */
	private void Db() {
		if (l.getType().equals("<IDENTIFIER>")) {
			Vl();
			if (token.equals("=")) {
				read("=");
				E();
				buildTree("=", 2);
			}
			else {
				int n = 0;
				while (l.getType().equals("<IDENTIFIER>") || token.equals("(")) {
					Vb();
					n++;
				}
				read("=");
				E();
				buildTree("function_form", n+2);
			}
		}
		else if (token.equals("(")) {
			read("(");
			D();
			read(")");
		}
	}
	
	/*
	 * Vb -> '<identifier>'
	 *    -> '(' Vl ')'
	 *    -> '(' ')'		=> '()';
	 */
	private void Vb() {
		if (l.getType().equals("<IDENTIFIER>"))
			read(token);
		else if (token.equals("(")) {
			read("(");
			if (token.equals(")")) {
				read(")");
				buildTree("()", 2);
			}
			else {
				Vl();
				read(")");
			}
		}
	}
	
	/*
	 * Vl -> '<identifier>' list ','	=> ','?;
	 */
	private void Vl() {
		if (l.getType().equals("<IDENTIFIER>")) {
			read(token);
		}
		int n = 0;
		while (token.equals(",")) {
			read(",");
			read(token);
			n++;
		}
		if (n > 0)
			buildTree(",", n+1);
	}
	
	//Traverse the AST in pre-order and print the tokens
	
	private void preOrder(Node n, int level) {
		String dot = "";
		for (int i = 0 ; i < level ; i++)
			dot = dot + ".";
		System.out.println(dot+""+n.getToken());
		if (n.getChild() != null) {
			preOrder(n.getChild(), level+1);
		}
		if (n.getSibling() != null) {
			preOrder(n.getSibling(), level);
		}
	}
	//Print the generated Abstract Syntax Tree
	
	private void printAST() {
		Node ast = stack.pop();
		preOrder(ast, 0);
	}
	
	//Start Parsing and call PRINT upon completion
	
	public void startParsing() {
		token = l.getToken();
		E();
		if (l.getType().equals("<EOF>")) {	//EOF reached
			printAST();
		}
	}
}

/*
 * **************************************************
 * MAIN CLASS to run
 * **************************************************
 */

public class P1 {

	public static void main(String args[]){
		String fileName;
		if (args.length == 0) {	//No switches => no output
			System.exit(0);
		}
		else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("-l")) {	//Listing
				// TO DO : listing
			}
			else if (args[0].equalsIgnoreCase("-ast")) {
				System.err.println("Error: FILE_NAME expected");
				System.exit(0);
			}
			else {
				System.err.println("Error: Unidentified Switch");
				System.exit(0);
			}
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("-ast")) {	//Generate AST by reading FILE
			fileName = args[1];
			Parser p = new Parser(fileName);
			p.startParsing();
		}
		else {
			System.err.println("Error: Illegal parameters");
			System.exit(0);
		}
	}
}
