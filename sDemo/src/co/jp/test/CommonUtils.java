package src.co.jp.test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
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
	
	public static void writeWithTmpl(String tmplFile, String code, List<Item> items, String outputFile){
		// 初期化
		Velocity.init();
		Properties p = new Properties();
		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		p.setProperty("input.encoding", code);
		Velocity.init(p);
		
		// vmファイルに出力する値を設定
		VelocityContext context = new VelocityContext();
		context.put("items", items);
		
		//テンプレートの作成
		Template template = Velocity.getTemplate(tmplFile, code);
		
		//テンプレートへ値を出力します。
		try {
			FileWriter fw = new FileWriter(outputFile);
			template.merge(context, fw);
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static double convertToChn(double jpyPrice){
		double raito = 0.065;
		return Math.ceil(jpyPrice*raito);
	}
	
	public static Double getDeliveryFeeByWeight(Item item){
		if(item.getWeight()==0){
			item.setWeighKg(null);
			System.out.println("★★★送料未計算. ["+item.getName()+"]");
			return null;
		}
		Double weight = item.getWeight();
		Double fee = 2000d;
		if(weight<=300){
			fee = 900d;
		}else if(weight<=500){
			fee = 1100d;
		}else if(weight<=600){
			fee = 1240d;
		}else if(weight<=700){
			fee = 1380d;
		}else if(weight<=800){
			fee = 1520d;
		}else if(weight<=900){
			fee = 1660d;
		}else if(weight<=1000){
			fee = 1800d;
		}else if(weight<=1250){
			fee = 2100d;
		}else if(weight<=1500){
			fee = 2400d;
		}else if(weight<=17500){
			fee = 2700d;
		}else if(weight<=2000){
			fee = 3000d;
		}else if(weight<=2500){
			fee = 3500d;
		}else if(weight<=3000){
			fee = 4000d;
		}else if(weight<=3500){
			fee = 4500d;
		}else if(weight<=4000){
			fee = 5000d;
		}else if(weight<=4500){
			fee = 5500d;
		}else if(weight<=5000){
			fee = 6000d;
		}else if(weight<=5500){
			fee = 6500d;
		}else if(weight<=6000){
			fee = 7000d;
		}else if(weight<=7000){
			fee = 7800d;
		}else{
			fee = null;
			System.out.println("★★★送料未計算. ["+item.getName()+"]");
		}
		return fee;
	}
	
	public static Double getPriceWithTaxAndMargin(Item item){
		Double totlePrice = 0d;
		try{
			totlePrice = Math.ceil(item.getPrice()*1.08) + Math.ceil(item.getPrice()*0.3)/* + getDeliveryFeeByWeight(item)*/;
		}catch(Exception e){
			totlePrice = null;
		}
		return totlePrice;
	}
	
	public static String areWoSore(String str){
		return str.replaceAll("\\/", "_").replaceAll("\\\\", "_").replaceAll("<", "_").replaceAll(">", "_");
	}
}
