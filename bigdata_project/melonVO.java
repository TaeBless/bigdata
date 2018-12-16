package stubs;

public class melonVO {
	private String count;
	private String title;
	private String artist;
	private String lyric;
	
	melonVO(String count, String title, String artist, String lyric){
		this.count = count;
		this.title = title;
		this.artist = artist;
		this.lyric = lyric;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public void setLyric(String lyric) {
		this.lyric = lyric;
	}
	
	public String getTitle() {
		return title;
	}
	public String getArtist() {
		return artist;
	}
	public String getCount() {
		return count;
	}
	public String getLyric() {
		return lyric;
	}

}
