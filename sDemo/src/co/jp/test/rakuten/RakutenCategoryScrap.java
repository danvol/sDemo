package src.co.jp.test.rakuten;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import src.co.jp.test.CommonUtils;

import com.orangesignal.csv.Csv;
import com.orangesignal.csv.CsvConfig;
import com.orangesignal.csv.handlers.BeanListHandler;

public class RakutenCategoryScrap {
	
	private static Set<String> ALREADY_SCRAPED_CAT = new TreeSet<String>();
	private static String START = "http://directory.rakuten.co.jp/";
	
	public static void main(String[] args) throws IOException{
//		System.setProperty("http.proxyHost", "172.16.64.10");
//		System.setProperty("http.proxyPort", "12080");
		
		Document doc = CommonUtils.getDoc(START);
		Elements elems = doc.select("td[bgcolor=#BF0000]").select("b>a");
		for(Element elm : elems){
			String aHref = elm.attr("href"); // level0カテゴリのhref(ジャンル)
			
			if(ArrayUtils.contains(CommonUtils.SKIP_LIST_RAKUTEN, aHref)){
				continue;
			}
			
			Document doc1 = CommonUtils.getDoc(aHref, START);
			Elements elems1 = doc1.select("#sc_lidAdd_xc").select("li.riFtBd>a");
			List<RakutenCat> catList = new ArrayList<RakutenCat>();
			for(Element elm1 : elems1){
				String aHref1 = elm1.attr("href"); // level1カテゴリのhref(商品)
				
				if(ALREADY_SCRAPED_CAT.contains(CommonUtils.getCatCodeFromUriRakuten(aHref1))) {
					System.out.println("すでに処理済。" + aHref1);
					continue;
				}
				
				List<RakutenCat> catList_ = getCatOrItems(aHref1);
				if(catList_!=null && !catList_.isEmpty())
					catList.addAll(catList_);
			}
			// 収集したデータをファイルに書き込む
			(new File("output")).mkdirs();
			String fileName = "output/"+String.valueOf((new Date()).getTime()) + "rakuten_cat.csv";
			CsvConfig csvCfg = new CsvConfig(',', '"', '"');
			Csv.save(catList, new File(fileName), csvCfg, new BeanListHandler<RakutenCat>(RakutenCat.class));
			System.out.println("[fileName] "+ fileName);
			catList = null;
		}
	}
	
	private static List<RakutenCat> getCatOrItems(String href) throws IOException{
		List<RakutenCat> catList = new ArrayList<RakutenCat>();
		Document doc = CommonUtils.getDoc(href, START);
		if(doc==null){
			System.out.println("最大リトライ回数に足したため、処理を終了。");
			System.exit(0);
		}
		
		Elements elems = doc.select("#rsrGenre").select("li.down.bold>ul>li>a"); // level1以降カテゴリのhref(商品のサイズ・形)
		if(elems.size()>0){
			for(Element elm : elems){
				String aHref = elm.attr("href");
				if(ALREADY_SCRAPED_CAT.contains(CommonUtils.getCatCodeFromUriRakuten(aHref))) {
					System.out.println("すでに処理済。" + aHref);
					continue;
				}
				if(elm.select(".rsrRegNum").text().equals("（⇒）")){
					System.out.println("（⇒）は処理しないため、スキップ。" + aHref);
					continue;
				}
				List<RakutenCat> catList_ = getCatOrItems(aHref);
				if(catList_==null || catList_.isEmpty()){
					continue;
				}
				for(RakutenCat c_ : catList_){
					boolean isExists = false;
					for(RakutenCat c : catList){
						if(c.code.equals(c_.code) && c.name.equalsIgnoreCase(c_.name)){
							isExists = true;
							break;
						}
					}
					if(!isExists)
						catList.add(c_);
				}
			}
		}else{
			catList.addAll(getCats(doc));
		}
		return catList;
	}
	
	private static List<RakutenCat> getCats(Document doc) throws IOException{
		List<RakutenCat> catList = new ArrayList<RakutenCat>();
		Element titlePath = doc.select("#topicPathBox").first();
		if(titlePath==null) return catList;
		System.out.println(titlePath.text());
		Elements cats = titlePath.select("li>a");
		if(cats==null) return catList;
		int level = 0;
		for(Element cat : cats){
			String catHref = cat.attr("href");
			Matcher m = CommonUtils.REGEX_CAT_RAKUTEN.matcher(catHref);
			if(m.find()){
				RakutenCat rCat = new RakutenCat();
				rCat.name = URLDecoder.decode(m.group(1), "UTF-8");
				rCat.code = m.group(2);
				rCat.level = level++;
				rCat.titlePath = titlePath.text();
				rCat.href = catHref;
				catList.add(rCat);
			}
		}
		if(catList.size()>0){
			Matcher m = CommonUtils.REGEX_CAT_RAKUTEN.matcher(doc.baseUri());
			if(m.find()){
				// 現在表示中のカテゴリを取得
				RakutenCat rCat = new RakutenCat();
				rCat.name = URLDecoder.decode(m.group(1), "UTF-8");
				rCat.code = m.group(2);
				rCat.level = level++;
				rCat.titlePath = titlePath.text();
				rCat.href = doc.baseUri();
				catList.add(rCat);
				ALREADY_SCRAPED_CAT.add(rCat.code);
			}
		}
		return catList;
	}
}
