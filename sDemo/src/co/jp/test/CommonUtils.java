package src.co.jp.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class CommonUtils {

	private CommonUtils(){}

	private final static int ERROR_RETRY_COUNT = 5;

	public static String[] SKIP_LIST_RAKUTEN = {
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
		return getDoc(href, "http://www.google.com");
	}
	
	public static Document getDoc(String href, String referrer){
		int errCnt = 0;
		Document doc = null;
		while(errCnt<=ERROR_RETRY_COUNT){
			try {
				if(errCnt>0){
					Thread.sleep(5000);
					System.out.println("[リトライ] " + errCnt);
				}
				doc = Jsoup.connect(href)
						.userAgent("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")
						.referrer(referrer)
						.timeout(20*1000)
						.get();
				if(doc!=null) break;
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				errCnt++;
			}
		}
		return doc;
	}

	public static final Pattern REGEX_CAT_RAKUTEN = Pattern.compile(".*search/mall/-/(.*)-([0-9]*)/.*p.*");
	public static String getCatCodeFromUriRakuten(String uri){
		Matcher m = REGEX_CAT_RAKUTEN.matcher(uri);
		if(m.find()){
			return m.group(2);
		}
		return "";
	}
	
	public static final Pattern REGEX_CAT_MONTBELL = Pattern.compile(".*/goods/category.php?category=([0-9]*)");
	public static String getCatCodeFromUriMontbell(String uri){
		Matcher m = REGEX_CAT_MONTBELL.matcher(uri);
		if(m.find()){
			return m.group(2);
		}
		return "";
	}
}
