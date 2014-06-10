package src.co.jp.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.orangesignal.csv.Csv;
import com.orangesignal.csv.CsvConfig;
import com.orangesignal.csv.handlers.StringArrayListHandler;

public class DemoTest {

	public static void main(String[] args) throws IOException{
//		System.setProperty("http.proxyHost", "172.16.64.10");
//		System.setProperty("http.proxyPort", "12080");
		
		Document doc = Jsoup.connect("http://www.yahoo.co.jp")
				.userAgent("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")
				.referrer("http://www.google.com")
				.get();
		Elements elems = doc.select("#topicsfb").select("ul.emphasis>li>a");
		List<String[]> list = new ArrayList<String[]>();
		for(Element elm : elems){
			String aHref = elm.attr("href");
			String aTxt = elm.text();
			
			list.add(new String[]{aHref, aTxt});
			System.out.println("href : " + aHref + ",text : " + aTxt);
		}
		(new File("output")).mkdirs();
		Csv.save(list, new File("output/"+String.valueOf((new Date()).getTime()) + "example.csv"), new CsvConfig(), new StringArrayListHandler());
	}
}
