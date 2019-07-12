import java.io.*;
import java.util.Scanner;

/**
 *  A class for parsing commented code from a java file
 */
public class SimonParser {


	private static class SimonParserLinkedListElement{
		private String value;
		private SimonParserLinkedListElement next=null;

		public SimonParserLinkedListElement(String valueOfElem) {
			this.value = valueOfElem;
			return;
		}

		public SimonParserLinkedListElement(String valueOfElem, SimonParserLinkedListElement nextElem) {
			this.value = valueOfElem;
			this.next = nextElem;
			return;
		}

		public String getValue() {
			return this.value;
		}
		public void setValue(String valueOfElem) {
			this.value = valueOfElem;
			return;
		}

		public SimonParserLinkedListElement getNext() {
			return this.next;
		}
		public void setNext(SimonParserLinkedListElement nextElem) {
			this.next = nextElem;
			return;
		}
	}



	private static class SimonParserLinkedList {
		private int lengthOfList;
		private SimonParserLinkedListElement headOfList;

		public SimonParserLinkedList () {
			this.lengthOfList = 0;
			this.headOfList = null;
		}




		public void addElementToList(String newString) {
			SimonParserLinkedListElement newElement= new SimonParserLinkedListElement(newString);
			this.setFirstElement(newElement);
			this.lengthOfList = this.lengthOfList+1;


			SimonParserLinkedListElement currentElement=this.headOfList;

			for(int i=0; i<this.getLengthOfList(); i++) {
				System.out.println("currentValue: "+currentElement.value);
				currentElement=currentElement.getNext();
			}


		}



		public int getSize() {
			return this.getSizeOfList();
		}
		public int getSizeOfList() {
			return this.getLengthOfList();
		}
		public int getLengthOfList() {
			return this.lengthOfList;
		}

		public SimonParserLinkedListElement getFirstElement() {
			return this.headOfList;
		}
		public void setFirstElement(SimonParserLinkedListElement newFirstElement) {
			newFirstElement.setNext(this.getFirstElement());
			this.headOfList = newFirstElement;
			return;
		}
	}



	public static String[] SimonParserStart() {
		String[] error= {"ERROR"};


		try {
		SimonParserLinkedList linkedStringListOfAllCommentsInAJavaFile = new SimonParserLinkedList();
		SimonParserLinkedList linkedStringListOfAllCommentsThatCouldBeStatementsInAJavaFile = new SimonParserLinkedList();





		Scanner userInputOfJavaFilePath = new Scanner(System.in);
		System.out.println("Input the PATH of the Java-file: ");
		String pathOfJavaFile = userInputOfJavaFilePath.nextLine();


		BufferedReader readerOfTheJavaFile = new BufferedReader(new FileReader(pathOfJavaFile));








		String lineOfInterrest=null;

		try {
			lineOfInterrest = readerOfTheJavaFile.readLine();
		} catch (Exception e) {
			System.out.println("ERROR_1");
			lineOfInterrest=null;
		}

		//reads all lines and saves all lines that are comments in a linked list
		while(lineOfInterrest != null) {
			System.out.println("lineOfInterrest: "+lineOfInterrest);
			if(isACommentInTheJavaFile(lineOfInterrest)) {
				linkedStringListOfAllCommentsInAJavaFile.addElementToList(cutUnnecessaryHeadOfCommentsOff(lineOfInterrest));
			}

			try {
				lineOfInterrest = readerOfTheJavaFile.readLine();
			} catch (Exception e) {
				System.out.println("ERROR_2");
			}
		}

		System.out.println("Number of comments: "+linkedStringListOfAllCommentsInAJavaFile.getSize());
		System.out.println(linkedStringListOfAllCommentsInAJavaFile.getFirstElement().getValue());

		//saves currently seen Element (from the list of all comments)
		SimonParserLinkedListElement currentlySeenCommentElement = linkedStringListOfAllCommentsInAJavaFile.getFirstElement();
		//goes through all saved comments and checks, if they could be statements
		//if they are, they are saved in a separate List
		for(int i=0; i<linkedStringListOfAllCommentsInAJavaFile.getSize(); i++) {
			if(commentCouldBeAStatement(currentlySeenCommentElement)) {
				linkedStringListOfAllCommentsThatCouldBeStatementsInAJavaFile.addElementToList(cutUnnecessaryTailOfCommentsOff(currentlySeenCommentElement.getValue()));
				currentlySeenCommentElement = currentlySeenCommentElement.getNext();
			}
		}

		System.out.println("x: "+linkedStringListOfAllCommentsThatCouldBeStatementsInAJavaFile.getLengthOfList());
		String[] retStringArray = convertTheLinkedStringListToAStringArrayList(linkedStringListOfAllCommentsThatCouldBeStatementsInAJavaFile);

		try {
			readerOfTheJavaFile.close();
		} catch (Exception e) {
			System.out.println("ERROR_3");
		}


		userInputOfJavaFilePath.close();
		return retStringArray;

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return error;
	}

	private static boolean isACommentInTheJavaFile(String lineOfInterrest) {

		return firstTwoRealCharactersAreBothSlashes(lineOfInterrest);

	}

	private static boolean firstTwoRealCharactersAreBothSlashes(String lineOfInterrest) {
		boolean commentHasntStartetYet = true;
		int seenSlashesInARow = 0;
		char charOfInterrestInLineOfInterrest;

		for(int i=0; i<lineOfInterrest.length(); i++) {
			charOfInterrestInLineOfInterrest = lineOfInterrest.charAt(i);
			commentHasntStartetYet = seenSlashesInARow==0&&commentHasntStartetYet&&(charOfInterrestInLineOfInterrest=='{'||charOfInterrestInLineOfInterrest=='}'||charOfInterrestInLineOfInterrest==' '||charOfInterrestInLineOfInterrest=='	');

			if(charOfInterrestInLineOfInterrest=='/') {
				seenSlashesInARow = seenSlashesInARow +1;
				commentHasntStartetYet = false;
			}else if(!commentHasntStartetYet) {
				System.out.println(false);
				return false;
			}
			if(seenSlashesInARow==2) {
				// if the Line starts with 2 slashes, its a comment
				System.out.println(true);
				return true;
			}
		}
		System.out.println(false+" 2");
		return false;
	}

	private static boolean commentCouldBeAStatement(SimonParserLinkedListElement currentlySeenCommentElement) {
		return stringValueOfCurrentlySeenCommentElementEndsLikeAStatement(currentlySeenCommentElement.getValue());
	}


	private static boolean stringValueOfCurrentlySeenCommentElementEndsLikeAStatement(String s) {

		char charOfInterrestInTheComment;


		for(int i= s.length()-1; i>0; i++) {
			charOfInterrestInTheComment = s.charAt(i);

			if(charOfInterrestInTheComment==';') {
				return true;
			}else if(! (charOfInterrestInTheComment=='}'||charOfInterrestInTheComment==' ') ) {
				return false;
			}

		}
		return false;
	}


	private static String cutUnnecessaryHeadOfCommentsOff(String lineOfInterrest) {
		int endOfString = lineOfInterrest.length();
		int startOftheImportantRelevantPartOfTheString = 0;
		int seenSlashes=0;

		//System.out.println(lineOfInterrest);

		while(seenSlashes<2&&startOftheImportantRelevantPartOfTheString<lineOfInterrest.length()) {

			//System.out.println(startOftheImportantRelevantPartOfTheString);

			if(lineOfInterrest.charAt(startOftheImportantRelevantPartOfTheString)=='/') {
				seenSlashes = seenSlashes+1;
			}

			System.out.println(seenSlashes+" x "+startOftheImportantRelevantPartOfTheString);
			System.out.println(lineOfInterrest.charAt(startOftheImportantRelevantPartOfTheString));
			startOftheImportantRelevantPartOfTheString = startOftheImportantRelevantPartOfTheString +1;
		}
		System.out.println(startOftheImportantRelevantPartOfTheString+" xxx "+ endOfString);
		System.out.println(lineOfInterrest.substring(startOftheImportantRelevantPartOfTheString, endOfString));
		return lineOfInterrest.substring(startOftheImportantRelevantPartOfTheString, endOfString);

	}



	private static String cutUnnecessaryTailOfCommentsOff(String lineOfInterrest) {
		int endOfString = lineOfInterrest.length()-1;
		int startOftheImportantRelevantPartOfTheString = 0;

		while(lineOfInterrest.charAt(endOfString)!=';'&&endOfString>=0) {

			endOfString = endOfString -1;
		}
		return lineOfInterrest.substring(startOftheImportantRelevantPartOfTheString, endOfString+1);
	}




	private static String[] convertTheLinkedStringListToAStringArrayList(SimonParserLinkedList linkedStringList) {
		String[] ret = new String[linkedStringList.getSize()];
		System.out.println(ret.length);

		//remembers which element we are looking at right now
		SimonParserLinkedListElement currentlySeenElement = linkedStringList.getFirstElement();

		for(int i=0; i<ret.length; i++) {
			ret[i] = currentlySeenElement.getValue();
			currentlySeenElement = currentlySeenElement.getNext();
		}

		return ret;
	}













	public static void main(String[] args) {


		String[] x= {""};
		try {
			x = SimonParserStart();

		}catch (Exception e){
			System.out.println(e);
			System.out.println("ERROR_4");
			e.printStackTrace();
		}

		System.out.println("xxxxxxxxxxx: \n");


		for(int i=0; i<x.length; i++) {
			System.out.println(x[i]);
		}


		/*
		try {
		Scanner userInputOfJavaFilePath = new Scanner(System.in);
		System.out.println("Input the PATH of the Java-file: ");
		String pathOfJavaFile = userInputOfJavaFilePath.nextLine();


		BufferedReader readerOfTheJavaFile = new BufferedReader(new FileReader(pathOfJavaFile));

		String read= readerOfTheJavaFile.readLine();

		while(read!=null) {
			System.out.println(read);
			read= readerOfTheJavaFile.readLine();
		}



		userInputOfJavaFilePath.close();
		readerOfTheJavaFile.close();
		}catch (Exception e){
			System.out.println(e);
			System.out.println("ERROR_4");
			e.printStackTrace();
		}*/

	}




}
