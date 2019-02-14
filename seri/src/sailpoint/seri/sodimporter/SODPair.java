package sailpoint.seri.sodimporter;

public class SODPair {
	
	public SODPair(String a, String b, String flag) {
		this.left=a;
		this.right=b;
		this.flag=flag;
	}
	public String left;
	public String right;
	public String flag;
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new SODPair(left, right, flag); 
	}
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof SODPair)) return false;
		SODPair obj=(SODPair)o;
		if( // Don't think I need this, check with JUnit
				(left==null && obj.left!=null) ||
				(left!=null && obj.left==null) ||
				(right==null && obj.right!=null) ||
				(right!=null && obj.right==null) ||
				(flag==null && obj.flag!=null) ||
				(flag!=null && obj.flag==null)
		  ) return false;
		if(left!=null && !(left.equals(obj.left))) return false;
		if(obj.left!=null && !(obj.left.equals(left))) return false;
		if(right!=null && !(right.equals(obj.right))) return false;
		if(obj.right!=null && !(obj.right.equals(right))) return false;
		if(flag!=null && !(flag.equalsIgnoreCase(obj.flag))) return false;
		if(obj.flag!=null && !(obj.flag.equalsIgnoreCase(flag))) return false;
		return true;
	}
	@Override
	public int hashCode() {
		StringBuilder sb=new StringBuilder(left);
		sb.append(",");
		sb.append(right);
		sb.append(",");
		sb.append(flag);
		return sb.hashCode();
	}
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder("[");
		sb.append(left);
		sb.append(",");
		sb.append(right);
		sb.append(",");
		sb.append(flag);
		sb.append("]");
		return sb.toString();
	}
	
	
}