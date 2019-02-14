package sailpoint.seri.sodimporter;

import junit.framework.Assert;

import org.junit.Test;

public class SODPairTest {

	@Test
	public void test() {
		
		SODPair pr1=new SODPair("abc", "def", "x");
		SODPair pr2=new SODPair("abc", "def", "x");
		SODPair pr3=new SODPair("abcd", "def", "x");
		SODPair pr4=new SODPair("abcd", "def", "y");
		
		Assert.assertEquals(pr1, pr2);
		Assert.assertNotSame(pr1, pr3);
		Assert.assertNotSame(pr3, pr4);
	}

}
