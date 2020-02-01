package pengguang.replayserver;

public class Util {
	static String ROOT_PATH = "replay";
	static String page 
	= "<html>\n"
	+ "<head>\n"
	+ "<meta http-equiv=Content-Type content='text/html;charset=utf-8'>\n"
	+ "<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n"
	+ "<title>%s</title>\n" /* title */
	+ "</head>\n"
	+ "<body>\n"
	+ "<table width='100%%' align='center' cellspacing='0' cellpadding='5'><tr bgcolor='#ffc0cb'><td width='30%%'><form action='%s' style='display: inline;'><input type='submit' value='%s' /></form></td><td width='40%%' align='center'>%s</td><td></td></tr></table>\n" /* link1, button1, title */
	+ "<form action='%s'>\n" /* link2 */
	+ "<table align='center' width='100%%'>\n"
	+ "<tr><td>%s</td></tr>\n" /* info */
	+ "<tr><td>%s</td></tr>\n" /* extra */
	+ "<tr align='center'><td><input type='submit' value='%s' /></td></tr>\n" /* button2 */
	+ "</form>\n"
	+ "</table>\n"
	+ "</body>\n"
	+ "</html>\n";

	public static String getPage(String button, String link, String title, String info) {
		return getPage(button, link, title, info, "");
	}

	public static String getPage(String button, String link, String title, String info, String extra) {
		return getPage(button, link, button, link, title, info, extra);
	}

	public static String getPage(String button1, String link1, String button2, String link2, String title, String info, String extra) {
		return String.format(page, title, link1, button1, title, link2, info, extra, button2);
	}

	public static String getErrorPage(String button, String link, String info) {
		return getPage(button, link, "失败", info);
	}

	public static String getSuccessPage(String button, String link, String info) {
		return getPage(button, link, "成功", info);
	}

	public static void main(String[] args) {
		System.out.println("您好");
		System.out.println(getSuccessPage("back", "index.html", "success"));
	}
}
