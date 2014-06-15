package src.co.jp.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class CommonUtils {

	private CommonUtils(){}

	private final static int ERROR_RETRY_COUNT = 3;

	public static final Pattern regexCat = Pattern.compile(".*search/mall/-/(.*)-([0-9]*)/.*p.*");

	public static String[] SKIP_LIST = {
		"http://www.rakuten.co.jp/category/ladiesfashion/",
		"http://www.rakuten.co.jp/category/mensfashion/",
		"http://www.rakuten.co.jp/category/shoes/",
		"http://www.rakuten.co.jp/category/fashiongoods/",
		"http://www.rakuten.co.jp/category/accessories/",
		"http://www.rakuten.co.jp/category/watch/",
		"http://www.rakuten.co.jp/category/inner/",
		"http://www.rakuten.co.jp/category/food/",
		"http://www.rakuten.co.jp/category/sweets/"
	};
	
	public static Document getDoc(String href){
		int errCnt = 0;
		Document doc = null;
		while(errCnt<=ERROR_RETRY_COUNT){
			try {
				if(errCnt>0){
					Thread.sleep(5000);
					System.out.println("[リトライ] " + errCnt);
				}
				doc = Jsoup.connect(href).get();
				if(doc!=null) break;
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				errCnt++;
			}
		}
		return doc;
	}
	
	public static String getCatCodeFromUri(String uri){
		Matcher m = regexCat.matcher(uri);
		if(m.find()){
			return m.group(2);
		}
		return "";
	}
}
