package src.co.jp.test.montbell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import src.co.jp.test.CommonUtils;
import src.co.jp.test.Item;

public class MontbellScrap {

	private static Set<String> ALREADY_SCRAPED = new TreeSet<String>();
	private static String START_PREFIX = "http://webshop.montbell.jp";
	private static String START = START_PREFIX + "/goods/?c=1";
	
	public static void main(String[] args) throws IOException{
		Document doc = CommonUtils.getDoc(START);
		Elements elems = doc.select("#ln").select("ul.type01>li").select(".level01On,.level01Off").select("a");
		for(Element elm : elems){
			String aHref = elm.attr("href"); // level01カテゴリのhref
			String aTitle = elm.attr("title");
			
			if(aHref.equals("http://webshop.montbell.jp/material/"))
				continue;
			if(!aHref.equals("http://webshop.montbell.jp/goods/?c=1")
					&& !aHref.equals("http://webshop.montbell.jp/goods/?c=2"))
				continue;
			
			Document doc1 = null;
			if(!aHref.equals(START))
				doc1 = CommonUtils.getDoc(aHref, START);
			else
				doc1 = doc;
			
			Elements elems1 = doc1.select("#ln").select("li.level02Off>a");
			for(Element elm1 : elems1){
				String aHref1 = START_PREFIX + elm1.attr("href"); // level02カテゴリのhref(カテゴリ)
				String aTitle1 = elm1.attr("title");
				if(!aHref1.startsWith(START_PREFIX)){
					aHref1 = START_PREFIX + aHref1;
				}
				
				Document doc2 = CommonUtils.getDoc(aHref1, START);
				if(doc2==null){
					System.out.println("最大リトライ回数に足したため、処理を終了。");
					System.exit(0);
				}
				
				Elements elems2 = doc2.select("#ln").select("li.level03Off>a"); // level02以降カテゴリのhref(商品)
				if(elems2!=null && !elems2.isEmpty()){
					for(Element elm2 : elems2){
						String aHref2 = START_PREFIX + elm2.attr("href");
						String aTitle2 = elm2.attr("title");
//						if(!aHref2.equals("http://webshop.montbell.jp/goods/list.php?category=230000"))
//							continue;
						Document doc3 = CommonUtils.getDoc(aHref2, START);
						if(doc3==null){
							System.out.println("最大リトライ回数に足したため、処理を終了。");
							System.exit(0);
						}
						getCatOrItems(doc3, aTitle, aTitle2);
					}
				}else{
					// 取り扱いブランド
					getCatOrItems(doc2, aTitle, aTitle1);
				}
			}
		}
	}
	
	private static void getCatOrItems(Document doc, String folderName, String innerFileName) throws IOException{
		List<Item> itemList = new ArrayList<Item>();
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
			(new File("output/"+folderName+"/")).mkdirs();
			String fileName = "output/"+folderName+"/"+String.valueOf((new Date()).getTime()) + "_" + CommonUtils.areWoSore(innerFileName) +"_montbell_item.csv";
			CommonUtils.writeWithTmpl("template/mont-bell.txt", "UTF-16LE", itemList, fileName);
			
//			CsvConfig csvCfg = new CsvConfig(',', '"', '"');
//			Csv.save(itemList, new File(fileName), csvCfg, new BeanListHandler<MontbellItem>(MontbellItem.class));
			System.out.println("[fileName] "+ fileName);
		}
	}
	
	private static Pattern PRICE_PARRTEN = Pattern.compile(".*価格&yen;(.*?)\\+税.*");
	private static Pattern WEIGHT_PARRTEN = Pattern.compile(".*[【総重量】|【重量】]([[0-9]|,|\\.]*?\\d)(g).*");
	private static Pattern WEIGHT_KG_PARRTEN = Pattern.compile(".*[【総重量】約|【総重量】|【重量】]([[0-9]|,|\\.]*?\\d)(kg).*");
	private static List<Item> scrapItemsByPage(Document doc) throws IOException{
		List<Item> list = new ArrayList<Item>();
		Elements unitDivs = doc.select("#goodsList").select("div.innerCont");
		for(Element elm : unitDivs){
			Elements itemTxts = elm.select("h3.ttlType03>a");
			Elements itemInfos = elm.select("div.description");
			if(itemTxts==null || itemTxts.size()==0) continue;
			MontbellItem item = new MontbellItem();
			for(Element elm1 : itemTxts){
				item.setHref(elm1.attr("href"));
				item.setName(elm1.ownText());
			}
			if(ALREADY_SCRAPED.contains(item.getName())){
				continue;
			}
			ALREADY_SCRAPED.add(item.getName());
			for(Element elm2 : itemInfos){
				Element des = elm2.select(".marginTop5").first();
				for(Node node : elm2.childNodes()){
					Matcher pm = PRICE_PARRTEN.matcher(node.outerHtml());
					Matcher wm = WEIGHT_PARRTEN.matcher(node.outerHtml());
					Matcher wmkg = WEIGHT_KG_PARRTEN.matcher(node.outerHtml());
					if(pm.find()){
						item.setPrice(Double.valueOf(pm.group(1).trim().replaceAll(",", "")));
					}else if(wm.find()){
						String weightUnit = wm.group(2).trim().replaceAll(",", "");
						double weight = Double.valueOf(wm.group(1).trim().replaceAll(",", ""));
						double weightG = "g".equalsIgnoreCase(weightUnit)?weight:weight*1000;
						double weightKg = "g".equalsIgnoreCase(weightUnit)?weight/1000:weight;
						item.setWeight(weightG);
						item.setWeighKg(weightKg);
					}else if(wmkg.find()){
						Double weight = null;
						Double weightG = null;
						Double weightKg = null;
						try {
							weight = Double.valueOf(wmkg.group(1).trim().replaceAll(",", ""));
							weightG = weight*1000;
							weightKg = weight;
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							weight = null;
						}
						item.setWeight(weightG);
						item.setWeighKg(weightKg);
					}
				}
				if(des!=null){
					item.setDescription(des.text());
				}
			}
			
			// 通貨変換と送料計算
			Double newPrice = CommonUtils.getPriceWithTaxAndMargin(item);
			if(newPrice==null){
				item.setDispPrice("要手動計算");
			}else{
				newPrice = CommonUtils.convertToChn(newPrice);
				item.setDispPrice(String.valueOf(newPrice));
			}
			list.add(item);
		}
		return list;
	}
}
