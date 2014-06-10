package src.co.jp.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RakutenCategory {
	public static void main(String[] args) throws IOException{
//		System.setProperty("http.proxyHost", "172.16.64.10");
//		System.setProperty("http.proxyPort", "12080");
		
		Document doc = Jsoup.connect("http://directory.rakuten.co.jp/")
				.userAgent("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")
				.referrer("http://www.google.com")
				.get();
		Elements elems = doc.select("td[bgcolor=#BF0000]").select("b>a");
		Map<String, String> cat1 = new HashMap<String, String>();
		for(Element elm : elems){
			String aHref = elm.attr("href");
			String aTxt = elm.select("font").text();
			
			cat1.put(aTxt, aHref);
			
			Document doc1 = Jsoup.connect(aHref).get();
			Elements elems1 = doc1.select("#sc_lidAdd_xc").select("li.riFtBd>a");
			for(Element elm1 : elems1){
				String aHref1 = elm1.attr("href");
				String aTxt1 = elm1.text();
				
//				System.out.println("href : " + aHref1 + ",text : " + aTxt1);

				getCat(aHref1);
			}
		}
		
		
		
		
	}
	
	private static void getCat(String href) throws IOException{
		Document doc = Jsoup.connect(href).get();
		Elements elems = doc.select("#rsrGenre").select("li.down.bold>ul>li>a");
		if(elems.size()>0){
			for(Element elm : elems){
				String aHref = elm.attr("href");
				
				getCat(aHref);
			}
		}else{
			getItems(doc);
		}
	}
	
	private static void getItems(Document doc) throws IOException{
		Elements unitDivs = doc.select("#ratArea").select("div[data-ratunit=true]");
		for(Element elm : unitDivs){
			Elements itemTxts = elm.select("div.rsrSResultItemTxt>h2>a");
			Elements itemInfos = elm.select("div.rsrSResultItemInfo>p.price>a");
			RakutenItem rItem = new RakutenItem();
			for(Element elm1 : itemTxts){
				rItem.href = elm1.attr("href");
				rItem.name = elm1.ownText();
			}
			for(Element elm2 : itemInfos){
				rItem.price = elm2.ownText();
			}
		}
	}
}

class RakutenItem{
//		List<String[]> list1 = new ArrayList<String[]>();
//			list1.add(new String[]{aHref, aTxt});
//		(new File("output")).mkdirs();
//		Csv.save(list, new File("output/"+String.valueOf((new Date()).getTime()) + "rakuten_cat.csv"), new CsvConfig(), new StringArrayListHandler());
	String name = "";
	String price = "";
	String href = "";
}