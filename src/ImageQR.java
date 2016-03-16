import java.util.Arrays;

public class ImageQR {		// pixels[col][row]

	byte[][] pixels;	// 0=white, 1=black, 2=white data, 3=black data
	 					// 4=reserved, 5=open
	int version;
	int size;
	
	public ImageQR(byte[] message, int vers, int errorLevel) {
		version = vers;
		size = version*4+17;
		pixels = new byte[size][size];
		Arrays.fill(pixels, 5);
		
		// Add patterns and bits
		patterns();
		data(message);
		
		//Choose mask
		int mask=0;
		int minPenalty = maskPenalty(tryMask(0));
		for(int i=1; i<8; i++) {
			int temp = maskPenalty(tryMask(i));
			if(temp < minPenalty) {
				mask = i;
				minPenalty = temp;
			}
		}
		pixels = tryMask(mask);
		
		// Add formatting info
		addFormat(errorLevel, mask);
		addVersion();
		addQuiet();
	}
	
	// Put in place all patterns
	void patterns() {
		pixels[8][size-8]=4	;
		for(int i=1; i<8; i++)			// Reserve format and version spots
			pixels[8][size-i] = 4;
		for(int i=1; i<=8; i++)
			pixels[size-i][8] = 4;
		for(int i=0; i<9; i++) {
			for(int j=0; j<9; j++) {
				pixels[i][j] = 4;
			}
		}
		
		if(version >= 7) {
			for(int i=0; i<6; i++) {
				for(int j=0; j<3; j++) {
					pixels[i][size-9-j] = 4;
					pixels[size-9-i][j] = 4;
				}
			}
		}
		
		finder();
		timing();
		alignment();
	}
	
	void finder() {
		for(int i=0; i<8; i++) {
			for(int j=0; j<8; j++) {
				pixels[i][j] = 0;
				pixels[i][size-1-j] = 0;
				pixels[size-1-i][j] = 0;
			}
		}
		
		for(int i=0; i<7; i++) {
			for(int j=0; j<7; j++) {
				pixels[i][j] = 1;
				pixels[i][size-1-j] = 1;
				pixels[size-1-i][j] = 1;
			}
		}
		
		for(int i=1; i<6; i++) {
			for(int j=1; j<6; j++) {
				pixels[i][j] = 0;
				pixels[i][size-1-j] = 0;
				pixels[size-1-i][j] = 0;
			}
		}
		
		for(int i=2; i<5; i++) {
			for(int j=2; j<5; j++) {
				pixels[i][j] = 1;
				pixels[i][size-1-j] = 1;
				pixels[size-1-i][j] = 1;
			}
		}
	}
	
	void timing() {
		for(int i=8; i<size-8; i++) {
			pixels[i][6] = (byte)((i+1)%2);
			pixels[6][i] = (byte)((i+1)%2);
		}
	}
	
	void alignment() {
		int[] aligns = TableLookup.getAlignmentCoords(version);
		for(int i=0; i<aligns.length; i++) {
			for(int j=0; j<aligns.length; j++) {
				if(!((i==0 && (j==0 || j==aligns.length-1)) || (i==aligns.length-1 && j==0)))
					alignSquare(aligns[i], aligns[j]);
			}
		}
	}
	
	void alignSquare(int x, int y) {
		for(int i=-2; i<=2; i++)
			for(int j=-2; j<=2; j++)
				pixels[x+i][y+j] = 1;
		
		for(int i=-1; i<=1; i++)
			for(int j=-1; j<=1; j++)
				pixels[x+i][y+j] = 0;
		
		pixels[x][y] = 1;
	}

	// insert data into grid, avoiding patterns
	void data(byte[] input) {
		int pointer = 0;
		boolean up = true;
		
		for(int col = size-1; col > 0; col -= 2) {
			if(col == 6)
				col--;		// Avoid timing pattern
			
			for(int i=0; i<size; i++) {
				int row = (up ? i : size-i-1);
					
				if(pixels[col][row] == 5) {
					pixels[col][row] = input[pointer];
					pointer++;
				}
				if(pixels[col-1][row] == 5) {
					pixels[col-1][row] = input[pointer];
					pointer++;
				}
			}
		}

		for(int i=0; i<size; i++)
			for(int j=0; j<size; j++)
				if(pixels[i][j] == 4)
					pixels[i][j] = 0;
	}
	
	// Tyr out a mask
	byte[][] tryMask(int mask) {
		byte[][] out = new byte[size][size];
		for(int i=0; i<size; i++) {
			for(int j=0; j<size; j++) {
				byte current = pixels[i][j];
				if(current == 2 || current == 3) {
					out[i][j] = ((maskFormula(i,j,mask)) ? (byte)(3-current) : (byte)(current-2));
				} else {
					out[i][j] = current;
				}
			}
		}
		return out;
	}
	
	// Get the cost of the mask
	private boolean maskFormula(int i, int j, int mask) {
		switch(mask) {
			case 0: return (i + j) % 2 == 0;
			case 1:	return (i) % 2 == 0;
			case 2:	return (j) % 3 == 0;
			case 3:	return (i + j) % 3 == 0;
			case 4:	return ( (i / 2) + (j / 3) ) % 2 == 0;
			case 5:	return ((i * j) % 2) + ((i * j) % 3) == 0;
			case 6:	return ( ((i * j) % 2) + ((i * j) % 3) ) % 2 == 0;
			case 7:	return ( ((i + j) % 2) + ((i * j) % 3) ) % 2 == 0;
		}
		return false;
	}

	int maskPenalty(byte[][] in) {
		int total = 0;
		
		// Condition 1
		for(int i=0; i<size; i++) {
			boolean color = false;
			int count = 0;
			for(int j=0; j<size; j++) {
				if((in[i][j] == 0) == color) {
					count++;
				} else {
					color = !color;
					if(count >= 5)
						total += (count - 2);
					count = 1;
				}					
			}
			if(count >= 5)
				total += (count - 2);
		}
		
		for(int i=0; i<size; i++) {
			boolean color = false;
			int count = 0;
			for(int j=0; j<size; j++) {
				if((in[j][i] == 0) == color) {
					count++;
				} else {
					color = !color;
					if(count >= 5)
						total += (count - 2);
					count = 1;
				}
				if(count >= 5)
					total += (count - 2);
			}
		}
		
		
		// Condition 2
		for(int i=0; i<size-1; i++) {
			for(int j=0; j<size-1; j++) {
				if(in[i][j] == in[i][j+1] && in[i][j] == in[i+1][j] && in[i][j] == in[i+1][j+1])
					total += 3;
			}
		}
		
		
		// Condition 3,
		byte[] pattern = { 1,0,1,1,1,0,1 };
		for(int i=0; i<size; i++) {
			for(int j=0; j<size-7; j++) {
				boolean temp = true;
				for(int k=0; k<7; k++)
					temp &= in[i][j+k] == pattern[k];
				if(temp) {
					boolean temp1 = true;
					boolean temp2 = true;
					
					if(j<size-11) {
						for(int k=7; k<=10; k++)
							temp1 &= in[i][j+k] == 0;
					}
					
					if(j>3) {
						for(int k=1; k<=4; k++)
							temp2 &= in[i][j-k] == 0;
					}
					
					if(temp1 || temp2)
						total += 40;
				}
			}
		}
		
		for(int i=0; i<size; i++) {
			for(int j=0; j<size-7; j++) {
				boolean temp = true;
				for(int k=0; k<7; k++)
					temp &= in[j+k][i] == pattern[k];
				if(temp) {
					boolean temp1 = true;
					boolean temp2 = true;
					
					if(j<size-11) {
						for(int k=7; k<=10; k++)
							temp1 &= in[j+k][i] == 0;
					}
					
					if(j>3) {
						for(int k=1; k<=4; k++)
							temp2 &= in[j-k][i] == 0;
					}
					
					if(temp1 || temp2)
						total += 40;
				}
			}
		}
		
		
		// Condition 4
		int sum = 0;
		for(int i=0; i<size; i++)
			for(int j=0; j<size; j++)
				sum += in[i][j];
		
		int calc = sum / (size^2) * 100;
		calc /= 5;
		if(calc < 10) {
			calc++;
		}
		total += calc*10;
		
		return total;
	}
	
	// Add formatting bits
	void addFormat(int errorLevel, int mask) {
		byte[] formatStr = ReedSolomonCodes.getFormatString(errorLevel, mask);
		
		pixels[8][size-8]=1;
		pixels[size-8][8]=formatStr[7];
		
		for(int i=1; i<8; i++) {
			pixels[size-i][8] = formatStr[i-1];
			pixels[8][size-i] = formatStr[15-i];
		}
		
		//left corner:
		for(int i=0; i<6; i++) {
			pixels[8][i] = formatStr[i];
			pixels[i][8] = formatStr[14-i];
		}
		pixels[8][7] = formatStr[6];
		pixels[8][8] = formatStr[7];
		pixels[7][8] = formatStr[8];
	}
	
	// Add versioning bits
	void addVersion() {
		if(version < 7)
			return;
		byte[] verStr = ReedSolomonCodes.getVersionString(version);
		
		int pointer = 0;
		for(int i=0; i<6; i++) {
			for(int j=0; j<3; j++) {
				pixels[i][size-11+j] = verStr[pointer];
				pixels[size-11+i][j] = verStr[pointer];
				pointer++;
			}
		}
	}
	
	// Add the quiet space on the outside of the qr code
	void addQuiet() {
		size += 8;
		byte[][] out = new byte[size][size];
		Arrays.fill(out, 0);			// Initialize the entire space as 0s.
		
		for(int i=4; i<size-4; i++) {		// Copy previous image to middle.
			for(int j=0; j<size-4; j++) {
				out[i][j] = pixels[i-4][j-4];
			}
		}
		
		pixels = out;
	}
}