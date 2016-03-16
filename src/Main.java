import java.util.regex.Pattern;

public class Main {

	
	public static void main(String[] args) {
		int errorLvl;
		switch(args[0]) {
		case "-l":
			errorLvl = 0;
			break;
		case "-q":
			errorLvl = 2;
			break;
		case "-h":
			errorLvl = 3;
			break;
		default:
			errorLvl = 1;	
		}
		
		String message = args.length == 1 ? args[0] : args[1];
		int type;
		if(Pattern.matches("[1-9]+", message)) {
		    type = 0; 
		} else if(Pattern.matches("($%*+-./: [a-z][A-Z][1-9])+", message)) {
		    type = 1; 
		} else {
			type = 2;
		}
		
		QRCode qr = new QRCode(message, errorLvl, type);
		Backend.toPNG("qr.png", qr.getImageArray);
		Backend.toAscii("qr.txt", qr.getImageArray);
	}

}
