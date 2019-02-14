package sailpoint.seri.sodimporter;

public interface Importer {
	public enum Filetype {
		INVALID,
		CSV,
		XLS
	};

	public enum Source {
		INVALID,
		COLUMN,
		ROW
	};

	public enum Roletype {
		INVALID,
		IT,
		BUSINESS
	};

}
