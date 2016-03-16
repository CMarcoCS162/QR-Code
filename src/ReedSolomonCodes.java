
public class ReedSolomonCodes {

	/* So, as it turns out, Reed-Solomon codes are too complicated for me to understand.
	 * I just don't get them. This is sort of a disjointed piece from the rest of the
	 * project, so I will submit what I have.
	 * 
	 * 
	 * "I never want projects to be finished; I have always believed in unfinished work.
	 * I got that from Schubert, you know, the 'Unfinished Symphony.'"
	 * 												--Yoko Ono
	 */
	
	
	
	
	/*
	public static byte[] getFormatString(int errorLevel, int mask) {
		// TODO Auto-generated method stub
		return null;
	}

	public static byte[] getVersionString(int version) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	static public int[] makeGenPoly(int n) {
		int[] temp = {0};
		if(n==0)
			return temp;
		
		
		int[] below = makeGenPoly(n-1);
		int[] current = new int[n+1];
		current[0] = (n-1)+below[0];
		
		for(int i=1; i<=n; i++)
			current[i] = below[i-1];
		for(int i=1; i<n; i++) 
			 current[i] = alphaplus(current[i], ((n-1)+below[i]) % 255);
		return current;
	}
	
	static private int alphaplus(int a, int b) {
		int sum = (TableLookup.fromAlpha(a)+TableLookup.fromAlpha(b));
		if(sum > 255)
			sum ^= 285;
		return TableLookup.toAlpha(sum);
	}*/

}
