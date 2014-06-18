package src.co.jp.test.montbell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import src.co.jp.test.CommonUtils;

import com.orangesignal.csv.Csv;
import com.orangesignal.csv.CsvConfig;
import com.orangesignal.csv.handlers.BeanListHandler;

public class MontbellScrap {

//	private static Set<String> ALREADY_SCRAPED_CAT = new TreeSet<String>();
	private static String START_PREFIX = "http://webshop.montbell.jp";
	private static String START = START_PREFIX + "/goods/?c=1";
	
	public static void main(String[] args) throws IOException{
		Document doc = CommonUtils.getDoc(START);
		Elements elems = doc.select("#ln").select("ul.type01>li").select(".level01On,.level01Off").select("a");
		for(Element elm : elems){
			String aHref = elm.attr("href"); // level01カテゴリのhref
			
			if(aHref.equals("http://webshop.montbell.jp/material/"))
				continue;
			
			Document doc1 = null;
			if(!aHref.equals(START))
				doc1 = CommonUtils.getDoc(aHref, START);
			else
				doc1 = doc;
			
			Elements elems1 = doc1.select("#ln").select("li.level02Off>a");
			for(Element elm1 : elems1){
				String aHref1 = START_PREFIX + elm1.attr("href"); // level02カテゴリのhref(カテゴリ)
				if(!aHref1.startsWith(START_PREFIX)){
					aHref1 = START_PREFIX + aHref1;
				}
//				if(ALREADY_SCRAPED_CAT.contains(CommonUtils.getCatCodeFromUriMontbell(aHref1))) {
//					System.out.println("すでに処理済。" + aHref1);
//					continue;
//				}
				
				Document doc2 = CommonUtils.getDoc(aHref1, START);
				if(doc2==null){
					System.out.println("最大リトライ回数に足したため、処理を終了。");
					System.exit(0);
				}
				
				Elements elems2 = doc2.select("#ln").select("li.level03Off>a"); // level02以降カテゴリのhref(商品)
				if(elems2!=null && !elems2.isEmpty()){
					for(Element elm2 : elems2){
						String aHref2 = START_PREFIX + elm2.attr("href");
//					if(ALREADY_SCRAPED_CAT.contains(CommonUtils.getCatCodeFromUriMontbell(aHref2))) {
//						System.out.println("すでに処理済。" + aHref);
//						continue;
//					}
						
						Document doc3 = CommonUtils.getDoc(aHref2, START);
						if(doc3==null){
							System.out.println("最大リトライ回数に足したため、処理を終了。");
							System.exit(0);
						}
						getCatOrItems(doc3);
					}
				}else{
					// 取り扱いブランド
					getCatOrItems(doc2);
				}
			}
//			// 収集したデータをファイルに書き込む
//			(new File("output")).mkdirs();
//			String fileName = "output/"+String.valueOf((new Date()).getTime()) + "monebell_cat.csv";
//			CsvConfig csvCfg = new CsvConfig(',', '"', '"');
//			Csv.save(catList, new File(fileName), csvCfg, new BeanListHandler<MontbellCat>(MontbellCat.class));
//			System.out.println("[fileName] "+ fileName);
//			catList = null;
		}
	}
	
	private static void getCatOrItems(Document doc) throws IOException{
		List<MontbellItem> itemList = new ArrayList<MontbellItem>();
		Element resultAreaElm = doc.select("#contents").select("div.resultArea").first();
		if(resultAreaElm==null) return;
		Element thisPageElm = resultAreaElm.select("div.opeArea>div.leftArea>p").get(2);
		int thisPageNo = 1;
		while(thisPageElm!=null) {
			// 一番下位のカテゴリにたどり着いた。商品一覧ページからscrape
			itemList.addAll(scrapItemsByPage(doc));
			
			Element titlePath = doc.select("#topicPath").first();
			if(titlePath==null) break;
			System.out.println(titlePath.text()+", [thisPageNo] "+ thisPageNo);
			
			if(thisPageNo % 50 == 0){
				// 収集したデータをファイルに書き込む
				(new File("output")).mkdirs();
				String fileName = "output/"+String.valueOf((new Date()).getTime()) + "montbell_item.csv";
				CsvConfig csvCfg = new CsvConfig(',', '"', '"');
				Csv.save(itemList, new File(fileName), csvCfg, new BeanListHandler<MontbellItem>(MontbellItem.class));
				System.out.println("[fileName] "+ fileName);
				itemList = new ArrayList<MontbellItem>();
			}
			// 次ページ
			Element nextPageElm = null;
			Element e = doc.select("#contents").select("div.resultArea").first();
			if(e!=null){
				e = e.select("div.opeArea>div.leftArea>p.here").last();
				if(e!=null){
					nextPageElm = e;
				}
			}
			if(nextPageElm!=null){
				doc = CommonUtils.getDoc(START_PREFIX + nextPageElm.select("a").attr("href"));
				if(doc==null){
					System.out.println("最大リトライ回数に足したため、処理を終了。");
					System.exit(0);
				}
				
				thisPageElm = nextPageElm;
				thisPageNo++;
			}else{
				break;
			}
		}
		
		if(!itemList.isEmpty()){
			// 収集したデータをファイルに書き込む
			(new File("output")).mkdirs();
			String fileName = "output/"+String.valueOf((new Date()).getTime()) + "montbell_item.csv";
			CsvConfig csvCfg = new CsvConfig(',', '"', '"');
			Csv.save(itemList, new File(fileName), csvCfg, new BeanListHandler<MontbellItem>(MontbellItem.class));
			System.out.println("[fileName] "+ fileName);
			itemList = new ArrayList<MontbellItem>();
		}
	}
	
	private static List<MontbellItem> scrapItemsByPage(Document doc) throws IOException{
		List<MontbellItem> list = new ArrayList<MontbellItem>();
		Elements unitDivs = doc.select("#goodsList").select("div.innerCont");
		for(Element elm : unitDivs){
			Elements itemTxts = elm.select("h3.ttlType03>a");
			Elements itemInfos = elm.select("div.description");
			if(itemTxts==null || itemTxts.size()==0) continue;
			MontbellItem rItem = new MontbellItem();
			for(Element elm1 : itemTxts){
				rItem.href = elm1.attr("href");
				rItem.name = elm1.ownText();
			}
			for(Element elm2 : itemInfos){
				rItem.description = elm2.text();
			}
			list.add(rItem);
		}
		return list;
	}
}
