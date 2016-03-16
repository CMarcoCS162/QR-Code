public class QRCode {

	
	int version;
	int error;
	int type;
	String message;
	byte[] bits;
	ImageQR image;
	
	
	public QRCode(String s, int e, int t) {
		error = e;
		message = s;
		type = t;
		version = getVersion(message.length(), error, type);
		
		// Encode the info into a bit array
		int bitLength = TableLookup.getMessageLength(version, type)*8;
		byte[] typeCode = getTypeCode(type);
		byte[] countInd = countIndicator(message.length(), version, type);
		byte[] main = toBinary(message);
		
		byte[] end = fillGap(bitLength - typeCode.length - countInd.length - main.length);
		
		// Generate array of bits to encode
		bits = new byte[bitLength];
		System.arraycopy(typeCode, 0, bits, 0, typeCode.length);
		System.arraycopy(countInd, 0, bits, typeCode.length, countInd.length);
		System.arraycopy(main, 0, bits, countInd.length, main.length);
		System.arraycopy(end, 0, bits, main.length, end.length);
		
		bits = ReedSolomonCodes.insertErrorCodes(bits);
		
		image = new ImageQR(bits, version, error);
	}

	public byte[][] getImageArray = image.pixels;
	
	private byte[] fillGap(int i) {
		byte[] result = new byte[i];
		
		int zeros = i<=4 ? i : (i-4)%8;
		for(int j=0; j<zeros; j++) {
			result[j] = 0;
		}
		
		byte[] endBytes = {1,1,1,0,1,1,0,0,0,0,0,1,0,0,0,1};
		for(int j=zeros; j<i; j++) {
			result[j] = endBytes[(j-zeros)%16];
		}
	
		return result;
	}



	private byte[] getTypeCode(int t) {
		byte[] result = {0, 0, 0, 0};
		result[3-t] = 1;
		return result;
	}


	private byte[] countIndicator(int len, int v, int t) {
		int vers = v < 10 ? 0 : 1;
		vers = v > 26 ? 2 : vers;
		int countSize = 0;
		
		switch(vers) {
		case 0:
			int[] look = {10, 9, 8, 8};
			countSize = look[t];
			break;
		case 1:
			int[] look1 = {12, 11, 16, 10};
			countSize = look1[t];
			break;
		case 2:
			int[] look2 = {14, 13, 16, 12};
			countSize = look2[t];
			break;
		}
		
		byte[] result = new byte[countSize];
		for(int i=0; i<countSize; i++)
			result[i] = 0;
		
		int counter = 1;
		while(len > 0) {
			result[countSize - counter] = (byte) (len%2);
			counter++;
			len /= 2;
		}
		
		return result;
	}



	public int getVersion(int len, int lvl, int enc) {
		for(int i=0; i<40; i++) {
			if(len <= TableLookup.maxCapacity(i, lvl, enc))
				return i;
		}
		
		return 0;
	}
	
	public byte[] toBinary(String input) {
		byte[] result = new byte[input.length()*8];
		
		for(int i=0; i<input.length(); i++) {
			boolean[] bits = byteToBooleans((byte)input.charAt(i));
			for(int j=0; j<8; j++) {
				result[i*8+j] = (byte)(bits[j] ? 1 : 0);
			}
		}
		
		return result;
	}
	
	public static boolean[] byteToBooleans(byte x) {
	    boolean bs[] = new boolean[8];
	    bs[0] = ((x & 0x01) != 0);
	    bs[1] = ((x & 0x02) != 0);
	    bs[2] = ((x & 0x04) != 0);
	    bs[3] = ((x & 0x08) != 0);
	    bs[4] = ((x & 0x10) != 0);
	    bs[5] = ((x & 0x20) != 0);
	    bs[6] = ((x & 0x40) != 0);
	    bs[7] = ((x & 0x80) != 0);
	    return bs;
	}
	
}
