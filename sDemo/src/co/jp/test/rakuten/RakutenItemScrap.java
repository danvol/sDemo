package src.co.jp.test.rakuten;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import src.co.jp.test.CommonUtils;

import com.orangesignal.csv.Csv;
import com.orangesignal.csv.CsvConfig;
import com.orangesignal.csv.handlers.BeanListHandler;

public class RakutenItemScrap {
	
	private static Set<String> alreadyScrapedCat = new TreeSet<String>();
	
	public static void main(String[] args) throws IOException{
//		System.setProperty("http.proxyHost", "172.16.64.10");
//		System.setProperty("http.proxyPort", "12080");
		
		Document doc = Jsoup.connect("http://directory.rakuten.co.jp/")
				.userAgent("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")
				.referrer("http://www.google.com")
				.get();
		Elements elems = doc.select("td[bgcolor=#BF0000]").select("b>a");
		for(Element elm : elems){
			String aHref = elm.attr("href"); // level0カテゴリのhref(ジャンル)
			
			Document doc1 = CommonUtils.getDoc(aHref);
			Elements elems1 = doc1.select("#sc_lidAdd_xc").select("li.riFtBd>a");
			for(Element elm1 : elems1){
				String aHref1 = elm1.attr("href"); // level1カテゴリのhref(商品)
				
				if(alreadyScrapedCat.contains(CommonUtils.getCatCodeFromUri(aHref1))) {
					System.out.println("すでに処理済。" + aHref1);
					continue;
				}
				getCatOrItems(aHref1);
			}
		}
	}
	
	private static void getCatOrItems(String href) throws IOException{
		Document doc = CommonUtils.getDoc(href);
		if(doc==null){
			System.out.println("最大リトライ回数に足したため、処理を終了。");
			System.exit(0);
		}
		
		Elements elems = doc.select("#rsrGenre").select("li.down.bold>ul>li>a"); // level1以降カテゴリのhref(商品のサイズ・形)
		if(elems.size()>0){
			for(Element elm : elems){
				String aHref = elm.attr("href");
				if(alreadyScrapedCat.contains(CommonUtils.getCatCodeFromUri(aHref))) {
					System.out.println("すでに処理済。" + aHref);
					continue;
				}
				if(elm.select(".rsrRegNum").text().equals("（⇒）")){
					System.out.println("（⇒）は処理しないため、スキップ。" + aHref);
					continue;
				}
				getCatOrItems(aHref);
			}
		}else{
			alreadyScrapedCat.add(CommonUtils.getCatCodeFromUri(doc.baseUri()));
			List<RakutenItem> itemList = new ArrayList<RakutenItem>();
			Element thisPageElm = doc.select("#rsrPagerSect").select("div.rsrPagination>span.thisPage").first();
			while(thisPageElm!=null) {
				// 一番下位のカテゴリにたどり着いた。商品一覧ページからscrape
				itemList.addAll(scrapItemsByPage(doc));
				
				int thisPageNo = Integer.parseInt(thisPageElm.text());
				Element titlePath = doc.select("#topicPathBox").first();
				System.out.println(titlePath.text()+", [thisPageNo] "+ thisPageNo);
				
				if(thisPageNo % 50 == 0){
					// 収集したデータをファイルに書き込む
					(new File("output")).mkdirs();
					String fileName = "output/"+String.valueOf((new Date()).getTime()) + "rakuten_item.csv";
					CsvConfig csvCfg = new CsvConfig(',', '"', '"');
					Csv.save(itemList, new File(fileName), csvCfg, new BeanListHandler<RakutenItem>(RakutenItem.class));
					System.out.println("[fileName] "+ fileName);
					itemList = new ArrayList<RakutenItem>();
				}
				// 次ページ
				Element nextPageElm = thisPageElm.nextElementSibling();
				if(nextPageElm!=null){
					doc = CommonUtils.getDoc(nextPageElm.attr("href"));
					if(doc==null){
						System.out.println("最大リトライ回数に足したため、処理を終了。");
						System.exit(0);
					}
					thisPageElm = doc.select("#rsrPagerSect").select("div.rsrPagination>span.thisPage").first();
				}else{
					break;
				}
			}
			
		}
	}
	
	private static List<RakutenItem> scrapItemsByPage(Document doc) throws IOException{
		List<RakutenItem> list = new ArrayList<RakutenItem>();
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
			list.add(rItem);
		}
		return list;
	}
}
