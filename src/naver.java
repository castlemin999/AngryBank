import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

public class naver {
	public int[] exchangeMoney(){
		int[] exchange = new int[4];

		String line = "";
		int check_line = 0;
		URL url;
		try {
			url = new URL("https://finance.naver.com/");
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "euc-kr"));
			int i = 0;
			while ((line = reader.readLine()) != null) {
				if (line.contains("<a href=\"/marketindex/exchangeDetail.nhn?marketindexCd"))
					check_line = 1;
				if (line.contains("<a href=\"/marketindex/?tableSel=exchange#tab_section\""))
					check_line = 0;

				if (check_line == 1) {

					if (line.contains("<td>") && !line.contains("em")) {
						String temp = line.split(">")[1].split("<")[0];

						if (temp.contains(",")) {
							String result1 = temp.split(",")[0];
							String result2 = temp.split(",")[1];
							String result3 = (result1 + result2).substring(0, (result1 + result2).lastIndexOf("."));
							exchange[i] = Integer.parseInt(result3);
							System.out.println(result3);
						} else {
							String result1 = temp.split(",")[0];
							String result3 = (result1).substring(0, (result1).lastIndexOf("."));
							exchange[i] = Integer.parseInt(result3);
						}
						i++;
						if (i == 4) {
							break;
						}
					}
//		            }
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return exchange;
	}
}
